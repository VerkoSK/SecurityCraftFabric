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
import net.geforcemods.securitycraft.blocks.KeypadDoorBlock;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The keypad door's block entity: owner + salted passcode + the module/option system (allowlist, denylist,
 * smart, harming, disguise). Placed on both halves for ownership; only the lower half's copy holds a passcode.
 * A correct code toggles the door open instead of pulsing redstone.
 */
public class KeypadDoorBlockEntity extends CustomizableBlockEntity implements PasscodeProtected {
	private DisabledOption disabled = new DisabledOption(false);
	private SendAllowlistMessageOption sendAllowlistMessage = new SendAllowlistMessageOption(false);
	private SendDenylistMessageOption sendDenylistMessage = new SendDenylistMessageOption(true);
	private SignalLengthOption signalLength = new SignalLengthOption(60);
	private SmartModuleCooldownOption smartModuleCooldown = new SmartModuleCooldownOption();
	private long cooldownEnd = 0;
	private String salt = UUID.randomUUID().toString();
	private String passcodeHash = null;

	public KeypadDoorBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.KEYPAD_DOOR_BLOCK_ENTITY, pos, state);
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
		if (getBlockState().getBlock() instanceof KeypadDoorBlock block)
			block.activate(level, worldPosition, getSignalLength());
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

	public boolean isDisabled() {
		return disabled.get();
	}

	public boolean sendsAllowlistMessage() {
		return sendAllowlistMessage.get();
	}

	public boolean sendsDenylistMessage() {
		return sendDenylistMessage.get();
	}

	public int getSignalLength() {
		return signalLength.get();
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST, ModuleType.DENYLIST, ModuleType.SMART, ModuleType.HARMING, ModuleType.DISGUISE
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				sendAllowlistMessage, sendDenylistMessage, signalLength, disabled, smartModuleCooldown
		};
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);

		long cooldownLeft = getCooldownEnd() - System.currentTimeMillis();

		tag.putString("salt", salt);
		tag.putLong("cooldownLeft", cooldownLeft <= 0 ? -1 : cooldownLeft);

		if (passcodeHash != null)
			tag.putString("passcodeHash", passcodeHash);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		if (tag.contains("salt"))
			salt = tag.getString("salt");

		cooldownEnd = System.currentTimeMillis() + tag.getLong("cooldownLeft");
		passcodeHash = tag.contains("passcodeHash") ? tag.getString("passcodeHash") : null;
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();

		tag.remove("passcodeHash");
		tag.remove("salt");
		return tag;
	}
}
