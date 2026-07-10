package net.geforcemods.securitycraft.blockentities;

import java.util.UUID;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.blocks.KeypadBlock;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

/**
 * Stores the keypad owner, its salted passcode hash and drives the redstone pulse when the correct
 * code is entered.
 */
public class KeypadBlockEntity extends OwnableBlockEntity {
	public static final int SIGNAL_LENGTH = 60;

	private String salt = UUID.randomUUID().toString();
	private String passcodeHash = null;

	public KeypadBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.KEYPAD_BLOCK_ENTITY, pos, state);
	}

	public boolean hasPasscode() {
		return passcodeHash != null;
	}

	public void setPasscode(String passcode) {
		salt = UUID.randomUUID().toString();
		passcodeHash = PasscodeUtils.hash(passcode, salt);
		markDirty();
		sync();
	}

	public boolean checkPasscode(String attempt) {
		return hasPasscode() && PasscodeUtils.matches(passcodeHash, PasscodeUtils.hash(attempt, salt));
	}

	public void activate(ServerWorld world) {
		BlockState state = getCachedState();

		if (state.getBlock() instanceof KeypadBlock && !state.get(KeypadBlock.POWERED)) {
			world.setBlockState(getPos(), state.with(KeypadBlock.POWERED, true), 3);
			world.updateNeighborsAlways(getPos(), state.getBlock(), null);
			world.scheduleBlockTick(getPos(), state.getBlock(), SIGNAL_LENGTH);
		}
	}

	@Override
	protected void writeData(WriteView view) {
		super.writeData(view);
		view.putString("salt", salt);

		if (passcodeHash != null)
			view.putString("passcodeHash", passcodeHash);
	}

	@Override
	protected void readData(ReadView view) {
		super.readData(view);
		salt = view.getString("salt", salt);

		String hash = view.getString("passcodeHash", "");
		passcodeHash = hash.isEmpty() ? null : hash;
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
		NbtCompound tag = super.toInitialChunkDataNbt(registries);
		tag.remove("passcodeHash");
		tag.remove("salt");
		return tag;
	}
}
