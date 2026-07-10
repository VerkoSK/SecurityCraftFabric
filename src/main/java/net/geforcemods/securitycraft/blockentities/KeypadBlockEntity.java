package net.geforcemods.securitycraft.blockentities;

import java.util.UUID;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.blocks.KeypadBlock;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stores the keypad's owner, its salted passcode hash and drives the redstone pulse when the
 * correct code is entered. The signal length (how long it stays powered) is fixed here; the
 * upstream mod exposes it as a configurable module option, which is on the roadmap.
 */
public class KeypadBlockEntity extends OwnableBlockEntity {
	public static final int SIGNAL_LENGTH = 60; // ticks the keypad stays powered after a correct code

	private String salt = UUID.randomUUID().toString();
	private String passcodeHash = null;

	public KeypadBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.KEYPAD_BLOCK_ENTITY, pos, state);
	}

	public boolean hasPasscode() {
		return passcodeHash != null;
	}

	/** Sets (or replaces) the passcode. Salt is rotated on every set. */
	public void setPasscode(String passcode) {
		salt = UUID.randomUUID().toString();
		passcodeHash = PasscodeUtils.hash(passcode, salt);
		setChanged();
		sync();
	}

	public boolean checkPasscode(String attempt) {
		return hasPasscode() && PasscodeUtils.matches(passcodeHash, PasscodeUtils.hash(attempt, salt));
	}

	/** Powers the block for {@link #SIGNAL_LENGTH} ticks and schedules the reset. */
	public void activate(ServerLevel level) {
		BlockState state = getBlockState();

		if (state.getBlock() instanceof KeypadBlock && !state.getValue(KeypadBlock.POWERED)) {
			level.setBlock(worldPosition, state.setValue(KeypadBlock.POWERED, true), 3);
			level.updateNeighborsAt(worldPosition, state.getBlock());
			level.scheduleTick(worldPosition, state.getBlock(), SIGNAL_LENGTH);
		}
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
		// Since 1.21.5 CompoundTag#getString returns Optional<String>.
		salt = tag.getString("salt").orElse(salt);
		passcodeHash = tag.getString("passcodeHash").orElse(null);
	}

	/**
	 * The passcode hash is intentionally NOT written to the update tag sent to clients, so the
	 * secret never leaves the server. {@link #hasPasscode()} state that the client needs is
	 * carried in the open-screen packet instead.
	 */
	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		tag.remove("passcodeHash");
		tag.remove("salt");
		return tag;
	}
}
