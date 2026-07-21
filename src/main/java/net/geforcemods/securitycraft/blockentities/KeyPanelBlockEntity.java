package net.geforcemods.securitycraft.blockentities;

import java.util.UUID;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.api.PasscodeProtected;
import net.geforcemods.securitycraft.blocks.AbstractPanelBlock;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/** Passcode-protected key panel block entity. Shares the keypad's passcode logic; activates the panel's redstone pulse on a correct code. */
public class KeyPanelBlockEntity extends OwnableBlockEntity implements PasscodeProtected {
	public static final int SIGNAL_LENGTH = 60;

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
			panel.activate(state, level, worldPosition, SIGNAL_LENGTH);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putString("salt", salt);

		if (passcodeHash != null)
			tag.putString("passcodeHash", passcodeHash);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);

		if (tag.contains("salt"))
			salt = tag.getString("salt");

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
