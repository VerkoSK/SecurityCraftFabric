package net.geforcemods.securitycraft.blockentities;

import java.util.UUID;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Option.SendAllowlistMessageOption;
import net.geforcemods.securitycraft.api.Option.SendDenylistMessageOption;
import net.geforcemods.securitycraft.api.Option.SignalLengthOption;
import net.geforcemods.securitycraft.api.Option.SmartModuleCooldownOption;
import net.geforcemods.securitycraft.api.PasscodeProtected;
import net.geforcemods.securitycraft.blocks.KeypadBlock;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The keypad block entity: owner + salted passcode + the customizable module/option system (allowlist,
 * denylist, disguise, smart, harming). Drives the redstone pulse on a correct code.
 */
public class KeypadBlockEntity extends CustomizableBlockEntity implements PasscodeProtected, net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity {
	private DisabledOption disabled = new DisabledOption(false);
	private SignalLengthOption signalLength = new SignalLengthOption(60);
	private SendAllowlistMessageOption sendAllowlistMessage = new SendAllowlistMessageOption(false);
	private SendDenylistMessageOption sendDenylistMessage = new SendDenylistMessageOption(true);
	private SmartModuleCooldownOption smartModuleCooldown = new SmartModuleCooldownOption();
	private long cooldownEnd = 0;
	private String salt = UUID.randomUUID().toString();
	private String passcodeHash = null;

	public KeypadBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.KEYPAD_BLOCK_ENTITY, pos, state);
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
		sync();
	}

	@Override
	public boolean checkPasscode(String attempt) {
		return hasPasscode() && PasscodeUtils.matches(passcodeHash, PasscodeUtils.hash(attempt, salt));
	}

	@Override
	public void activate(ServerLevel level) {
		BlockState state = getBlockState();

		if (state.getBlock() instanceof KeypadBlock && !state.getValue(KeypadBlock.POWERED)) {
			level.setBlockAndUpdate(worldPosition, state.setValue(KeypadBlock.POWERED, true));
			BlockUtils.updateIndirectNeighbors(level, worldPosition, state.getBlock());
			level.scheduleTick(worldPosition, state.getBlock(), getSignalLength());
		}
	}

	/** The block state this keypad is disguised as (from an enabled disguise module), or null. */
	public BlockState getDisguisedState() {
		if (isModuleEnabled(ModuleType.DISGUISE)) {
			net.minecraft.world.level.block.Block addon = net.geforcemods.securitycraft.items.ModuleItem.getBlockAddon(getModule(ModuleType.DISGUISE));

			if (addon != null && addon != SCContent.KEYPAD)
				return addon.defaultBlockState();
		}

		return null;
	}

	@Override
	public Object getRenderData() {
		return getDisguisedState();
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
	public <T> void onOptionChanged(Option<T> option) {
		if (option == disabled && disabled.get() || option == signalLength) {
			level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.POWERED, false));
			BlockUtils.updateIndirectNeighbors(level, worldPosition, getBlockState().getBlock());
		}

		super.onOptionChanged(option);
	}

	public boolean isDisabled() {
		return disabled.get();
	}

	public int getSignalLength() {
		return signalLength.get();
	}

	public boolean sendsAllowlistMessage() {
		return sendAllowlistMessage.get();
	}

	public boolean sendsDenylistMessage() {
		return sendDenylistMessage.get();
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
				sendAllowlistMessage, sendDenylistMessage, signalLength, disabled, smartModuleCooldown
		};
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);

		long cooldownLeft = getCooldownEnd() - System.currentTimeMillis();

		output.putString("salt", salt);
		output.putLong("cooldownLeft", cooldownLeft <= 0 ? -1 : cooldownLeft);

		if (passcodeHash != null)
			output.putString("passcodeHash", passcodeHash);
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		salt = input.getStringOr("salt", salt);
		cooldownEnd = System.currentTimeMillis() + input.getLongOr("cooldownLeft", 0L);
		passcodeHash = input.getString("passcodeHash").orElse(null);
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		dropAllModules();
		super.preRemoveSideEffects(pos, state);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);

		tag.remove("passcodeHash");
		tag.remove("salt");
		return tag;
	}
}
