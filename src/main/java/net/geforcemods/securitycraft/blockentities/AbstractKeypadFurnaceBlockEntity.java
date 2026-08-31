package net.geforcemods.securitycraft.blockentities;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.geforcemods.securitycraft.api.ICustomizable;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Option.SendAllowlistMessageOption;
import net.geforcemods.securitycraft.api.Option.SendDenylistMessageOption;
import net.geforcemods.securitycraft.api.Option.SmartModuleCooldownOption;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.api.PasscodeProtected;
import net.geforcemods.securitycraft.blocks.AbstractKeypadFurnaceBlock;
import net.geforcemods.securitycraft.inventory.AbstractKeypadFurnaceMenu;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Common base for the keypad furnace family (furnace, smoker, blast furnace): a vanilla furnace guarded by a
 * salted passcode, with the customizable module/option system (allowlist, denylist, disguise, smart, harming)
 * layered on top, and a door-like {@code OPEN} state driven by a {@link ContainerOpenersCounter} like a chest.
 *
 * <p>Ported from upstream's {@code AbstractKeypadFurnaceBlockEntity}. Dropped: the NeoForge
 * {@code IItemHandlerModifiable}/insert-only-sides capability wrapper (Fabric has no capability system here;
 * automation just uses the vanilla {@link net.minecraft.world.WorldlyContainer} slots that
 * {@link AbstractFurnaceBlockEntity} already exposes), the Codebreaker hack check (Codebreaker isn't ported),
 * and the shape-changing disguise system (no {@code IDisguisable} in this port). Disguise module support is
 * kept at the same render-only level as {@link KeypadBlockEntity}: an enabled disguise module swaps the client
 * model via {@link RenderDataBlockEntity}, but the hitbox/shape stays the furnace's own.
 */
public abstract class AbstractKeypadFurnaceBlockEntity extends AbstractFurnaceBlockEntity implements PasscodeProtected, IOwnable, IModuleInventory, ICustomizable, ExtendedScreenHandlerFactory<BlockPos>, RenderDataBlockEntity {
	private Owner owner = new Owner();
	private String salt = UUID.randomUUID().toString();
	private String passcodeHash = null;
	private UUID pendingOpener;
	private NonNullList<ItemStack> modules = NonNullList.<ItemStack>withSize(getMaxNumberOfModules(), ItemStack.EMPTY);
	private BooleanOption sendAllowlistMessage = new SendAllowlistMessageOption(false);
	private BooleanOption sendDenylistMessage = new SendDenylistMessageOption(true);
	private DisabledOption disabled = new DisabledOption(false);
	private SmartModuleCooldownOption smartModuleCooldown = new SmartModuleCooldownOption();
	private long cooldownEnd = 0;
	private Map<ModuleType, Boolean> moduleStates = new EnumMap<>(ModuleType.class);
	private ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
		@Override
		protected void onOpen(Level level, BlockPos pos, BlockState state) {
			if (level.isClientSide)
				return;

			level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
			level.setBlockAndUpdate(pos, state.setValue(AbstractKeypadFurnaceBlock.OPEN, true));
		}

		@Override
		protected void onClose(Level level, BlockPos pos, BlockState state) {
			if (level.isClientSide)
				return;

			level.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
			level.setBlockAndUpdate(pos, state.setValue(AbstractKeypadFurnaceBlock.OPEN, false));
		}

		@Override
		protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {}

		@Override
		protected boolean isOwnContainer(Player player) {
			return player.containerMenu instanceof AbstractKeypadFurnaceMenu menu && menu.be == AbstractKeypadFurnaceBlockEntity.this;
		}
	};

	protected AbstractKeypadFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, RecipeType<? extends AbstractCookingRecipe> recipeType) {
		super(type, pos, state, recipeType);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, AbstractKeypadFurnaceBlockEntity be) {
		if (!be.isDisabled() && level instanceof net.minecraft.server.level.ServerLevel serverLevel)
			AbstractFurnaceBlockEntity.serverTick(serverLevel, pos, state, be);
	}

	public void startOpen(Player player) {
		if (!isRemoved() && !player.isSpectator())
			openersCounter.incrementOpeners(player, level, worldPosition, getBlockState());
	}

	public void stopOpen(Player player) {
		if (!isRemoved() && !player.isSpectator())
			openersCounter.decrementOpeners(player, level, worldPosition, getBlockState());
	}

	public void recheckOpen() {
		if (!isRemoved())
			openersCounter.recheckOpeners(level, worldPosition, getBlockState());
	}

	@Override
	public boolean hasPasscode() {
		return passcodeHash != null;
	}

	@Override
	public void setPasscode(String passcode) {
		salt = UUID.randomUUID().toString();
		passcodeHash = PasscodeUtils.hash(passcode, salt);
		setChanged();
	}

	@Override
	public boolean checkPasscode(String attempt) {
		return hasPasscode() && PasscodeUtils.matches(passcodeHash, PasscodeUtils.hash(attempt, salt));
	}

	/** Remembers who is mid-passcode-entry so {@link #activate(ServerLevel)} knows whose screen to open. */
	public void setPendingOpener(UUID playerId) {
		pendingOpener = playerId;
	}

	@Override
	public void activate(ServerLevel level) {
		UUID playerId = pendingOpener;

		pendingOpener = null;

		if (playerId == null)
			return;

		ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);

		if (player != null && getBlockState().getBlock() instanceof AbstractKeypadFurnaceBlock block)
			block.activate(this, level, worldPosition, player);
	}

	@Override
	public void startCooldown() {
		if (!isOnCooldown()) {
			cooldownEnd = System.currentTimeMillis() + smartModuleCooldown.get() * 50L;
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
			setChanged();
		}
	}

	@Override
	public long getCooldownEnd() {
		return cooldownEnd;
	}

	@Override
	public boolean isOnCooldown() {
		return System.currentTimeMillis() < getCooldownEnd();
	}

	@Override
	public Owner getOwner() {
		return owner;
	}

	@Override
	public void setOwner(String name, String uuid) {
		owner.set(name, uuid);
		setChanged();
	}

	@Override
	public ItemStack getItem(int slot) {
		return slot >= 100 ? getModuleInSlot(slot) : items.get(slot);
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		return modules;
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST, ModuleType.DENYLIST, ModuleType.DISGUISE, ModuleType.SMART, ModuleType.HARMING
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				sendAllowlistMessage, sendDenylistMessage, disabled, smartModuleCooldown
		};
	}

	@Override
	public boolean isModuleEnabled(ModuleType module) {
		return hasModule(module) && moduleStates.get(module) == Boolean.TRUE; //prevent NPE
	}

	@Override
	public void toggleModuleState(ModuleType module, boolean shouldBeEnabled) {
		moduleStates.put(module, shouldBeEnabled);
	}

	@Override
	public Level myLevel() {
		return level;
	}

	@Override
	public BlockPos myPos() {
		return worldPosition;
	}

	@Override
	public boolean enableHack() {
		return true;
	}

	/** The block state this furnace is disguised as (from an enabled disguise module), or null. */
	public BlockState getDisguisedState() {
		if (isModuleEnabled(ModuleType.DISGUISE)) {
			Block addon = ModuleItem.getBlockAddon(getModule(ModuleType.DISGUISE));

			if (addon != null && addon != getBlockState().getBlock())
				return addon.defaultBlockState();
		}

		return null;
	}

	@Override
	public Object getRenderData() {
		return getDisguisedState();
	}

	public boolean sendsAllowlistMessage() {
		return sendAllowlistMessage.get();
	}

	public boolean sendsDenylistMessage() {
		return sendDenylistMessage.get();
	}

	public boolean isDisabled() {
		return disabled.get();
	}

	public ContainerData getFurnaceData() {
		return dataAccess;
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable(getBlockState().getBlock().getDescriptionId());
	}

	@Override
	public BlockPos getScreenOpeningData(ServerPlayer player) {
		return worldPosition;
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		writeModuleInventory(output);
		writeModuleStates(output);
		writeOptions(output);

		long cooldownLeft = getCooldownEnd() - System.currentTimeMillis();

		output.putLong("cooldownLeft", cooldownLeft <= 0 ? -1 : cooldownLeft);
		output.putString("salt", salt);
		owner.save(output);

		if (passcodeHash != null)
			output.putString("passcodeHash", passcodeHash);
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);

		modules = readModuleInventory(input);
		moduleStates = readModuleStates(input);
		readOptions(input);
		cooldownEnd = System.currentTimeMillis() + input.getLongOr("cooldownLeft", 0L);
		owner = Owner.load(input);
		salt = input.getStringOr("salt", salt);
		passcodeHash = input.getString("passcodeHash").orElse(null);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = saveWithoutMetadata(registries);

		tag.remove("passcodeHash");
		tag.remove("salt");
		return tag;
	}
}
