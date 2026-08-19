package net.geforcemods.securitycraft.blockentities;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.ICustomizable;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.api.Option.SendAllowlistMessageOption;
import net.geforcemods.securitycraft.api.Option.SendDenylistMessageOption;
import net.geforcemods.securitycraft.api.Option.SmartModuleCooldownOption;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.api.PasscodeProtected;
import net.geforcemods.securitycraft.blocks.KeypadChestBlock;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

/**
 * Ported from upstream's {@code KeypadChestBlockEntity}. Cannot extend the port's {@link net.geforcemods.securitycraft.api.CustomizableBlockEntity}
 * (Java has single inheritance and this must extend vanilla's {@link ChestBlockEntity}), so the module/option/owner
 * plumbing that class provides is duplicated here by hand, exactly like upstream duplicates it against Forge's
 * {@code ChestBlockEntity}.
 *
 * <p>Dropped vs. upstream: the Codebreaker item, the Disguise module (and {@code IDisguisable}/{@code IOverlayDisplay}
 * appearance overrides it drives), {@code ILockable}/{@code ISentryBulletContainer} (sentries not ported), and the
 * {@code SaltData} cross-block-entity salt registry — this port's {@link PasscodeProtected} already hashes with its
 * own per-block-entity salt, so passcodes are copied directly between the two chest halves' salt+hash fields instead
 * (see {@link #copyPasscodeFrom}). NeoForge item-handler capabilities are dropped per the port's Container-only rule.
 */
public class KeypadChestBlockEntity extends ChestBlockEntity implements PasscodeProtected, IOwnable, IModuleInventory, ICustomizable {
	private String salt = UUID.randomUUID().toString();
	private String passcodeHash = null;
	private Owner owner = new Owner();
	private NonNullList<ItemStack> modules = NonNullList.<ItemStack>withSize(getMaxNumberOfModules(), ItemStack.EMPTY);
	private BooleanOption sendAllowlistMessage = new SendAllowlistMessageOption(false);
	private BooleanOption sendDenylistMessage = new SendDenylistMessageOption(true);
	private SmartModuleCooldownOption smartModuleCooldown = new SmartModuleCooldownOption();
	private long cooldownEnd = 0;
	private Map<ModuleType, Boolean> moduleStates = new EnumMap<>(ModuleType.class);
	private ResourceLocation previousChest;
	/** The player whose passcode attempt is currently being verified, so {@link #activate(ServerLevel)} knows who to open the menu for. */
	private UUID pendingOpener;

	public KeypadChestBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.KEYPAD_CHEST_BLOCK_ENTITY, pos, state);
	}

	@Override
	public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		long cooldownLeft;

		super.saveAdditional(tag, registries);
		writeModuleInventory(tag, registries);
		writeModuleStates(tag);
		writeOptions(tag);
		cooldownLeft = getCooldownEnd() - System.currentTimeMillis();
		tag.putLong("cooldownLeft", cooldownLeft <= 0 ? -1 : cooldownLeft);
		tag.putString("salt", salt);

		if (passcodeHash != null)
			tag.putString("passcodeHash", passcodeHash);

		owner.save(tag);

		if (previousChest != null)
			tag.putString("previous_chest", previousChest.toString());
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);

		modules = readModuleInventory(tag, registries);
		moduleStates = readModuleStates(tag);
		readOptions(tag);
		cooldownEnd = System.currentTimeMillis() + tag.getLongOr("cooldownLeft", 0L);

		if (tag.contains("salt"))
			salt = tag.getStringOr("salt", "");

		passcodeHash = tag.contains("passcodeHash") ? tag.getStringOr("passcodeHash", null) : null;
		owner.load(tag);

		if (tag.contains("previous_chest")) {
			String savedPreviousChest = tag.getStringOr("previous_chest", "");

			if (!savedPreviousChest.isBlank()) {
				ResourceLocation parsedPreviousChest = ResourceLocation.parse(savedPreviousChest);

				if (parsedPreviousChest.getPath() != null && !parsedPreviousChest.getPath().isBlank())
					previousChest = parsedPreviousChest;
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
		return Utils.localize(SCContent.KEYPAD_CHEST.getDescriptionId());
	}

	@Override
	protected void signalOpenCount(Level level, BlockPos pos, BlockState state, int i, int j) {
		super.signalOpenCount(level, pos, state, i, j);

		if (isModuleEnabled(ModuleType.REDSTONE))
			BlockUtils.updateIndirectNeighbors(level, pos, state.getBlock(), Direction.DOWN);
	}

	public int getNumPlayersUsing() {
		return openersCounter.getOpenerCount();
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

		if (player instanceof ServerPlayer && getBlockState().getBlock() instanceof KeypadChestBlock block)
			block.activate(getBlockState(), level, worldPosition, player);
	}

	@Override
	public boolean hasPasscode() {
		return passcodeHash != null;
	}

	@Override
	public void setPasscode(String passcode) {
		setPasscode(passcode, true);
	}

	private void setPasscode(String passcode, boolean propagate) {
		salt = UUID.randomUUID().toString();
		passcodeHash = PasscodeUtils.hash(passcode, salt);
		setChanged();

		KeypadChestBlockEntity otherHalf = findOther();

		if (propagate && otherHalf != null)
			otherHalf.setPasscode(passcode, false);
	}

	/** Copies the salt+hash straight across, so both halves accept the same code without ever exposing the plaintext. */
	private void copyPasscodeFrom(KeypadChestBlockEntity other) {
		salt = other.salt;
		passcodeHash = other.passcodeHash;
		setChanged();
	}

	@Override
	public boolean checkPasscode(String attempt) {
		return hasPasscode() && PasscodeUtils.matches(passcodeHash, PasscodeUtils.hash(attempt, salt));
	}

	@Override
	public void startCooldown() {
		KeypadChestBlockEntity otherHalf = findOther();
		long start = System.currentTimeMillis();

		startCooldown(start);

		if (otherHalf != null)
			otherHalf.startCooldown(start);
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
	public void onModuleInserted(ItemStack stack, ModuleType module, boolean toggled) {
		IModuleInventory.super.onModuleInserted(stack, module, toggled);
		addOrRemoveModuleFromAttached(stack, false, toggled);
	}

	@Override
	public void onModuleRemoved(ItemStack stack, ModuleType module, boolean toggled) {
		IModuleInventory.super.onModuleRemoved(stack, module, toggled);
		addOrRemoveModuleFromAttached(stack, true, toggled);
	}

	@Override
	public <T> void onOptionChanged(Option<T> option) {
		KeypadChestBlockEntity otherBe = findOther();

		if (otherBe != null) {
			if (option instanceof BooleanOption bo) {
				if (option == sendAllowlistMessage)
					otherBe.setSendsAllowlistMessage(bo.get());
				else if (option == sendDenylistMessage)
					//kept as upstream wrote it (this looks like a copy/paste of the allowlist branch above, ported verbatim per the porting rules)
					otherBe.setSendsAllowlistMessage(bo.get());
				else
					throw new UnsupportedOperationException("Unhandled option synchronization in keypad chest! " + option.getName());
			}
			else if (option == smartModuleCooldown)
				otherBe.smartModuleCooldown.copy(option);
			else
				throw new UnsupportedOperationException("Unhandled option synchronization in keypad chest! " + option.getName());
		}

		ICustomizable.super.onOptionChanged(option);
	}

	@Override
	public void dropAllModules() {
		KeypadChestBlockEntity offsetBe = findOther();

		for (ItemStack module : getInventory()) {
			if (!(module.getItem() instanceof ModuleItem item))
				continue;

			if (offsetBe != null)
				offsetBe.removeModule(item.getModuleType(), false);

			Block.popResource(level, worldPosition, module);
		}

		getInventory().clear();
	}

	public void addOrRemoveModuleFromAttached(ItemStack module, boolean remove, boolean toggled) {
		if (module.isEmpty() || !(module.getItem() instanceof ModuleItem moduleItem))
			return;

		KeypadChestBlockEntity offsetBe = findOther();

		if (offsetBe != null) {
			if (toggled ? offsetBe.isModuleEnabled(moduleItem.getModuleType()) != remove : offsetBe.hasModule(moduleItem.getModuleType()) != remove)
				return;

			if (remove)
				offsetBe.removeModule(moduleItem.getModuleType(), toggled);
			else
				offsetBe.insertModule(module, toggled);
		}
	}

	public KeypadChestBlockEntity findOther() {
		BlockState state = getBlockState();
		ChestType type = state.getValue(ChestBlock.TYPE);

		if (type != ChestType.SINGLE) {
			BlockPos offsetPos = worldPosition.relative(ChestBlock.getConnectedDirection(state));
			BlockState offsetState = level.getBlockState(offsetPos);

			if (state.getBlock() == offsetState.getBlock()) {
				ChestType offsetType = offsetState.getValue(ChestBlock.TYPE);

				if (offsetType != ChestType.SINGLE && type != offsetType && state.getValue(ChestBlock.FACING) == offsetState.getValue(ChestBlock.FACING) && level.getBlockEntity(offsetPos) instanceof KeypadChestBlockEntity be)
					return be;
			}
		}

		return null;
	}

	/** Placement-time merge: a freshly placed chest half that connects to an already-owned, same-owner half inherits its modules, options and passcode. */
	public void mergeFrom(KeypadChestBlockEntity other) {
		for (ModuleType type : other.getInsertedModules()) {
			insertModule(other.getModule(type), false);
		}

		readOptions(other.writeOptions(new CompoundTag()));
		copyPasscodeFrom(other);
	}

	@Override
	public Owner getOwner() {
		return owner;
	}

	@Override
	public void setOwner(String name, String uuid) {
		if (owner.getName().equals(name) && owner.getUUID().equals(uuid))
			return; //already applied; also breaks the recursion below

		owner.set(name, uuid);
		setChanged();

		if (level != null)
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

		KeypadChestBlockEntity otherHalf = findOther();

		if (otherHalf != null)
			otherHalf.setOwner(name, uuid);
	}

	public boolean isBlocked() {
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos pos = getBlockPos().relative(dir);

			if (level.getBlockState(pos).getBlock() instanceof KeypadChestBlock && KeypadChestBlock.isBlocked(level, pos))
				return true;
		}

		return isSingleBlocked();
	}

	public boolean isSingleBlocked() {
		return KeypadChestBlock.isBlocked(getLevel(), getBlockPos());
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		return modules;
	}

	@Override
	public boolean shouldDropModules() {
		return getBlockState().getValue(ChestBlock.TYPE) == ChestType.SINGLE;
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST, ModuleType.DENYLIST, ModuleType.REDSTONE, ModuleType.SMART, ModuleType.HARMING
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

	public void setSendsAllowlistMessage(boolean value) {
		sendAllowlistMessage.setValue(value);
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); //sync option change to client
		setChanged();
	}

	public boolean sendsDenylistMessage() {
		return sendDenylistMessage.get();
	}

	public void setSendsDenylistMessage(boolean value) {
		sendDenylistMessage.setValue(value);
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); //sync option change to client
		setChanged();
	}

	public void setPreviousChest(Block previousChest) {
		this.previousChest = BuiltInRegistries.BLOCK.getKey(previousChest);
	}

	public ResourceLocation getPreviousChest() {
		return previousChest;
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
