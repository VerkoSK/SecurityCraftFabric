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
import net.geforcemods.securitycraft.blocks.AbstractPanelBlock;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/** Passcode-protected key panel block entity with the customizable module/option system, like the keypad. */
public class KeyPanelBlockEntity extends CustomizableBlockEntity implements PasscodeProtected {
	private DisabledOption disabled = new DisabledOption(false);
	private SignalLengthOption signalLength = new SignalLengthOption(60);
	private SendAllowlistMessageOption sendAllowlistMessage = new SendAllowlistMessageOption(false);
	private SendDenylistMessageOption sendDenylistMessage = new SendDenylistMessageOption(true);
	private SmartModuleCooldownOption smartModuleCooldown = new SmartModuleCooldownOption();
	private long cooldownEnd = 0;
	private String salt = UUID.randomUUID().toString();
	private String passcodeHash = null;

	public KeyPanelBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.KEY_PANEL_BLOCK_ENTITY, pos, state);
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

		if (state.getBlock() instanceof AbstractPanelBlock panel && !state.getValue(AbstractPanelBlock.POWERED))
			panel.activate(state, level, worldPosition, getSignalLength());
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
				ModuleType.ALLOWLIST, ModuleType.DENYLIST, ModuleType.SMART, ModuleType.HARMING
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				sendAllowlistMessage, sendDenylistMessage, signalLength, disabled, smartModuleCooldown
		};
	}

	@Override
	public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);

		long cooldownLeft = getCooldownEnd() - System.currentTimeMillis();

		tag.putString("salt", salt);
		tag.putLong("cooldownLeft", cooldownLeft <= 0 ? -1 : cooldownLeft);

		if (passcodeHash != null)
			tag.putString("passcodeHash", passcodeHash);
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);

		if (tag.contains("salt"))
			salt = tag.getString("salt");

		cooldownEnd = System.currentTimeMillis() + tag.getLong("cooldownLeft");
		passcodeHash = tag.contains("passcodeHash") ? tag.getString("passcodeHash") : null;
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);

		tag.remove("passcodeHash");
		tag.remove("salt");
		return tag;
	}
}
