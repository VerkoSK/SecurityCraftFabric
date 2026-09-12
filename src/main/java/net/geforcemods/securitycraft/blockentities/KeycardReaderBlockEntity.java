package net.geforcemods.securitycraft.blockentities;

import java.util.Optional;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.Codebreakable;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Option.SendDenylistMessageOption;
import net.geforcemods.securitycraft.api.Option.SignalLengthOption;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.items.KeycardItem;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.TeamUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Programmable reader: right-click with a keycard to open its link screen (owner) or to try using an already-linked
 * card, right-click with a Keycard Holder to try every card inside it, right-click with a Codebreaker to try to hack
 * it. A correct card/hack pulses redstone. Ported from upstream's {@code KeycardReaderBlockEntity}, minus the
 * disguise module and the Sonic Security System lock (not ported).
 */
public class KeycardReaderBlockEntity extends CustomizableBlockEntity implements MenuProvider, ExtendedScreenHandlerFactory, Codebreakable {
	protected boolean[] acceptedLevels = {
			true, false, false, false, false
	};
	protected int signature = 0;
	protected SendDenylistMessageOption sendDenylistMessage = new SendDenylistMessageOption(true);
	protected SignalLengthOption signalLength = new SignalLengthOption(60);
	protected DisabledOption disabled = new DisabledOption(false);

	public KeycardReaderBlockEntity(BlockPos pos, BlockState state) {
		this(SCContent.KEYCARD_READER_BLOCK_ENTITY, pos, state);
	}

	public KeycardReaderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);

		CompoundTag levels = new CompoundTag();

		for (int i = 0; i < 5; i++) {
			levels.putBoolean("lvl" + (i + 1), acceptedLevels[i]);
		}

		tag.put("acceptedLevels", levels);
		tag.putInt("signature", signature);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		if (tag.contains("acceptedLevels")) {
			CompoundTag levels = tag.getCompound("acceptedLevels");

			for (int i = 0; i < 5; i++) {
				acceptedLevels[i] = levels.getBoolean("lvl" + (i + 1));
			}
		}

		signature = tag.getInt("signature");
	}

	@Override
	public boolean shouldAttemptCodebreak(Player player) {
		if (isDisabled()) {
			player.displayClientMessage(net.geforcemods.securitycraft.util.Utils.localize("gui.securitycraft:scManual.disabled"), true);
			return false;
		}

		return !getBlockState().getValue(BlockStateProperties.POWERED);
	}

	@Override
	public void useCodebreaker(Player player) {
		if (level != null && !level.isClientSide)
			activate();
	}

	/** Tries every keycard in {@code holder}, stopping at the first that works. */
	public net.minecraft.network.chat.MutableComponent insertHolder(ItemStack holder, Player player) {
		net.minecraft.network.chat.MutableComponent feedback = null;

		for (ItemStack keycard : net.geforcemods.securitycraft.items.KeycardHolderItem.getContents(holder)) {
			if (keycard.getItem() instanceof KeycardItem && KeycardItem.isLinked(keycard)) {
				feedback = insertCard(keycard, player);

				if (feedback == null)
					return null;
			}
		}

		return feedback == null ? Component.translatable("messages.securitycraft:keycard_holder.no_keycards") : feedback;
	}

	/** @return null on success, or a translated failure message */
	public net.minecraft.network.chat.MutableComponent insertCard(ItemStack stack, Player player) {
		Owner keycardOwner = new Owner(KeycardItem.getOwnerName(stack), KeycardItem.getOwnerUUID(stack));

		if (!TeamUtils.areOnSameTeam(getOwner(), keycardOwner) || !getOwner().getUUID().equals(keycardOwner.getUUID()))
			return Component.translatable("messages.securitycraft:keycard_acceptor.different_owner");

		Optional<String> usableBy = KeycardItem.getUsableBy(stack);

		if (usableBy.isPresent() && !usableBy.get().equals(player.getGameProfile().getName()))
			return Component.translatable("messages.securitycraft:keycard_acceptor.cant_use");

		if (getSignature() != KeycardItem.getSignature(stack))
			return Component.translatable("messages.securitycraft:keycard_acceptor.wrong_signature");

		int keycardLevel = ((KeycardItem) stack.getItem()).getLevel();

		if (!getAcceptedLevels()[keycardLevel])
			return Component.translatable("messages.securitycraft:keycard_acceptor.wrong_level", keycardLevel + 1);

		//don't consider the reader powered if the signal length is 0: it needs to be toggle-able
		boolean powered = getBlockState().getValue(BlockStateProperties.POWERED) && getSignalLength() > 0;

		if (!powered) {
			if (((KeycardItem) stack.getItem()).isLimited()) {
				int usesLeft = KeycardItem.getUsesLeft(stack);

				if (usesLeft <= 0)
					return Component.translatable("messages.securitycraft:keycard_acceptor.no_uses");

				if (!player.isCreative())
					KeycardItem.setUsesLeft(stack, usesLeft - 1);
			}

			activate();
		}

		return null;
	}

	public void activate() {
		int length = getSignalLength();

		level.setBlockAndUpdate(worldPosition, getBlockState().cycle(BlockStateProperties.POWERED));
		BlockUtils.updateIndirectNeighbors(level, worldPosition, getBlockState().getBlock());

		if (length > 0)
			level.scheduleTick(worldPosition, getBlockState().getBlock(), length);
	}

	@Override
	public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
		buf.writeBlockPos(worldPosition);
	}

	@Override
	public <T> void onOptionChanged(Option<T> option) {
		if (option == signalLength && level != null) {
			level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.POWERED, false));
			BlockUtils.updateIndirectNeighbors(level, worldPosition, getBlockState().getBlock());
		}

		super.onOptionChanged(option);
	}

	public void setAcceptedLevels(boolean[] acceptedLevels) {
		this.acceptedLevels = acceptedLevels;
		setChanged();
	}

	public boolean[] getAcceptedLevels() {
		return acceptedLevels;
	}

	public void setSignature(int signature) {
		this.signature = signature;
		setChanged();
	}

	public int getSignature() {
		return signature;
	}

	public void reset() {
		acceptedLevels = new boolean[] {
				true, false, false, false, false
		};
		signature = 0;
		setChanged();
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST, ModuleType.DENYLIST, ModuleType.SMART
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				sendDenylistMessage, signalLength, disabled
		};
	}

	public boolean sendsDenylistMessage() {
		return sendDenylistMessage.get();
	}

	public int getSignalLength() {
		return signalLength.get();
	}

	public boolean isDisabled() {
		return disabled.get();
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new net.geforcemods.securitycraft.inventory.KeycardReaderMenu(windowId, inv, level, worldPosition);
	}

	@Override
	public Component getDisplayName() {
		return getBlockState().getBlock().getName();
	}
}
