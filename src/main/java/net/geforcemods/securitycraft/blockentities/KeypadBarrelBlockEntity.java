package net.geforcemods.securitycraft.blockentities;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.ICustomizable;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.SendAllowlistMessageOption;
import net.geforcemods.securitycraft.api.Option.SendDenylistMessageOption;
import net.geforcemods.securitycraft.api.Option.SmartModuleCooldownOption;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.api.PasscodeProtected;
import net.geforcemods.securitycraft.blocks.KeypadBarrelBlock;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ported from upstream's {@code KeypadBarrelBlockEntity}, extending vanilla's {@link RandomizableContainerBlockEntity}
 * directly for the same single-inheritance reason {@link KeypadChestBlockEntity} does.
 *
 * <p>Dropped vs. upstream: the Codebreaker item's bypass in the block, the Disguise module (and
 * {@code DisguisableBlockEntity} hooks it drove), {@code ILockable}/{@code ISentryBulletContainer} (sentries not
 * ported), and the {@code SaltData} salt registry, exactly as in {@link KeypadChestBlockEntity} — see that class'
 * javadoc for why. NeoForge item-handler capabilities are dropped per the port's Container-only rule.
 */
public class KeypadBarrelBlockEntity extends RandomizableContainerBlockEntity implements PasscodeProtected, IOwnable, IModuleInventory, ICustomizable {
	private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
	private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
		@Override
		protected void onOpen(Level level, BlockPos pos, BlockState state) {
			KeypadBarrelBlockEntity.this.playSound(state, state.getValue(KeypadBarrelBlock.FROG) ? SoundEvents.FROG_AMBIENT : SoundEvents.BARREL_OPEN);
			KeypadBarrelBlockEntity.this.updateBlockState(state, true);
		}

		@Override
		protected void onClose(Level level, BlockPos pos, BlockState state) {
			KeypadBarrelBlockEntity.this.playSound(state, state.getValue(KeypadBarrelBlock.FROG) ? SoundEvents.FROG_DEATH : SoundEvents.BARREL_CLOSE);
			KeypadBarrelBlockEntity.this.updateBlockState(state, false);
		}

		@Override
		protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int count, int openCount) {}

		@Override
		protected boolean isOwnContainer(Player player) {
			if (player.containerMenu instanceof ChestMenu menu)
				return menu.getContainer() == KeypadBarrelBlockEntity.this;

			return false;
		}
	};
	private String salt = UUID.randomUUID().toString();
	private String passcodeHash = null;
	private Owner owner = new Owner();
	private NonNullList<ItemStack> modules = NonNullList.<ItemStack>withSize(getMaxNumberOfModules(), ItemStack.EMPTY);
	private SendAllowlistMessageOption sendAllowlistMessage = new SendAllowlistMessageOption(false);
	private SendDenylistMessageOption sendDenylistMessage = new SendDenylistMessageOption(true);
	private SmartModuleCooldownOption smartModuleCooldown = new SmartModuleCooldownOption();
	private long cooldownEnd = 0;
	private Map<ModuleType, Boolean> moduleStates = new EnumMap<>(ModuleType.class);
	private ResourceLocation previousBarrel;
	/** The player whose passcode attempt is currently being verified, so {@link #activate(ServerLevel)} knows who to open the menu for. */
	private UUID pendingOpener;

	public KeypadBarrelBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.KEYPAD_BARREL_BLOCK_ENTITY, pos, state);
	}

	@Override
	public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		long cooldownLeft;

		super.saveAdditional(tag, registries);

		if (!trySaveLootTable(tag))
			ContainerHelper.saveAllItems(tag, items, registries);

		writeModuleInventory(tag, registries);
		writeModuleStates(tag);
		writeOptions(tag);
		cooldownLeft = getCooldownEnd() - System.currentTimeMillis();
		tag.putLong("cooldownLeft", cooldownLeft <= 0 ? -1 : cooldownLeft);
		tag.putString("salt", salt);

		if (passcodeHash != null)
			tag.putString("passcodeHash", passcodeHash);

		owner.save(tag);

		if (previousBarrel != null)
			tag.putString("previous_barrel", previousBarrel.toString());
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);

		items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);

		if (!tryLoadLootTable(tag))
			ContainerHelper.loadAllItems(tag, items, registries);

		modules = readModuleInventory(tag, registries);
		moduleStates = readModuleStates(tag);
		readOptions(tag);
		cooldownEnd = System.currentTimeMillis() + tag.getLong("cooldownLeft");

		if (tag.contains("salt"))
			salt = tag.getString("salt");

		passcodeHash = tag.contains("passcodeHash") ? tag.getString("passcodeHash") : null;
		owner.load(tag);

		if (tag.contains("previous_barrel")) {
			String savedPreviousBarrel = tag.getString("previous_barrel");

			if (!savedPreviousBarrel.isBlank()) {
				ResourceLocation parsedPreviousBarrel = ResourceLocation.parse(savedPreviousBarrel);

				if (parsedPreviousBarrel.getPath() != null && !parsedPreviousBarrel.getPath().isBlank())
					previousBarrel = parsedPreviousBarrel;
			}
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = saveWithoutMetadata(registries);

		tag.remove("passcodeHash");
		tag.remove("salt");
		return tag;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public Component getDefaultName() {
		return Utils.localize(SCContent.KEYPAD_BARREL.getDescriptionId());
	}

	@Override
	public int getContainerSize() {
		return 27;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	public boolean enableHack() {
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return slot >= 100 ? getModuleInSlot(slot) : super.getItem(slot);
	}

	/** Remembers who is currently attempting the passcode, so a correct attempt opens the menu for them (see {@link #activate(ServerLevel)}). */
	public void setPendingOpener(Player player) {
		pendingOpener = player.getUUID();
	}

	@Override
	public void activate(ServerLevel level) {
		if (pendingOpener == null)
			return;

		Player player = level.getPlayerByUUID(pendingOpener);

		pendingOpener = null;

		if (player instanceof ServerPlayer && getBlockState().getBlock() instanceof KeypadBarrelBlock block)
			block.activate(getBlockState(), level, worldPosition, player);
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
		return ChestMenu.threeRows(id, playerInventory, this);
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

	@Override
	public void startCooldown() {
		startCooldown(System.currentTimeMillis());
	}

	public void startCooldown(long start) {
		if (!isOnCooldown()) {
			cooldownEnd = start + smartModuleCooldown.get() * 50;
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
	public void dropAllModules() {
		for (ItemStack module : getInventory()) {
			if (module.getItem() instanceof ModuleItem)
				Block.popResource(level, worldPosition, module);
		}

		getInventory().clear();
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
	public NonNullList<ItemStack> getInventory() {
		return modules;
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST, ModuleType.DENYLIST, ModuleType.SMART, ModuleType.HARMING
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				sendAllowlistMessage, sendDenylistMessage, smartModuleCooldown
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

	public boolean sendsAllowlistMessage() {
		return sendAllowlistMessage.get();
	}

	public boolean sendsDenylistMessage() {
		return sendDenylistMessage.get();
	}

	@Override
	public void startOpen(Player player) {
		if (!remove && !player.isSpectator())
			openersCounter.incrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
	}

	@Override
	public void stopOpen(Player player) {
		if (!remove && !player.isSpectator())
			openersCounter.decrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
	}

	public void recheckOpen() {
		if (!remove)
			openersCounter.recheckOpeners(getLevel(), getBlockPos(), getBlockState());
	}

	public void updateBlockState(BlockState state, boolean open) {
		level.setBlock(getBlockPos(), state.setValue(KeypadBarrelBlock.OPEN, open), 3);
	}

	public void playSound(BlockState state, SoundEvent sound) {
		Direction normalFacing = switch (state.getValue(KeypadBarrelBlock.LID_FACING)) {
			case UP -> Direction.UP;
			case SIDEWAYS -> state.getValue(KeypadBarrelBlock.HORIZONTAL_FACING);
			case DOWN -> Direction.DOWN;
		};
		Vec3i facingNormal = normalFacing.getUnitVec3i();
		double x = worldPosition.getX() + 0.5D + facingNormal.getX() / 2.0D;
		double y = worldPosition.getY() + 0.5D + facingNormal.getY() / 2.0D;
		double z = worldPosition.getZ() + 0.5D + facingNormal.getZ() / 2.0D;

		level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
	}

	public void setPreviousBarrel(Block previousBarrel) {
		this.previousBarrel = BuiltInRegistries.BLOCK.getKey(previousBarrel);
	}

	public ResourceLocation getPreviousBarrel() {
		return previousBarrel;
	}

	@Override
	public Level myLevel() {
		return level;
	}

	@Override
	public BlockPos myPos() {
		return worldPosition;
	}
}
