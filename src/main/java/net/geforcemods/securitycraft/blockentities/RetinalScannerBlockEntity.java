package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.IViewActivated;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Option.DoubleOption;
import net.geforcemods.securitycraft.api.Option.RespectInvisibilityOption;
import net.geforcemods.securitycraft.api.Option.SignalLengthOption;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blocks.RetinalScannerBlock;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ITickingBlockEntity;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class RetinalScannerBlockEntity extends CustomizableBlockEntity implements IViewActivated, ITickingBlockEntity {
	private BooleanOption activatedByEntities = new BooleanOption("activatedByEntities", false);
	private BooleanOption sendMessage = new BooleanOption("sendMessage", true);
	private SignalLengthOption signalLength = new SignalLengthOption(60);
	private DoubleOption maximumDistance = new DoubleOption("maximumDistance", 5.0D, 0.1D, 25.0D, 0.1D) {
		@Override
		public String getKey(String denotation) {
			return "option.generic.viewActivated.maximumDistance";
		}
	};
	private DisabledOption disabled = new DisabledOption(false);
	private RespectInvisibilityOption respectInvisibility = new RespectInvisibilityOption();
	private int viewCooldown = 0;
	/** Ticks left until the signal is turned back off; driven by this block entity's own tick, not a scheduled block tick. */
	private int powerTicksLeft = 0;
	private com.mojang.authlib.GameProfile ownerProfile;

	public RetinalScannerBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.RETINAL_SCANNER_BLOCK_ENTITY, pos, state);
	}

	@Override
	public void tick(Level level, BlockPos pos, BlockState state) {
		checkView(level, pos);

		if (powerTicksLeft > 0 && --powerTicksLeft == 0 && state.getValue(RetinalScannerBlock.POWERED)) {
			level.setBlockAndUpdate(pos, state.setValue(RetinalScannerBlock.POWERED, false));
			BlockUtils.updateIndirectNeighbors(level, pos, state.getBlock());
		}
	}

	@Override
	public boolean onEntityViewed(LivingEntity entity, BlockHitResult hitResult) {
		if (isDisabled())
			return false;

		BlockState state = getBlockState();

		if (state.getValue(RetinalScannerBlock.FACING) != hitResult.getDirection())
			return false;

		int length = getSignalLength();

		if ((state.getValue(RetinalScannerBlock.POWERED) && length != 0) || isConsideredInvisible(entity))
			return false;

		if (entity instanceof Player player) {
			Owner viewingPlayer = new Owner(player);

			if (!isOwnedBy(player) && !isAllowed(viewingPlayer.getName())) {
				if (sendMessage.get())
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.RETINAL_SCANNER.getDescriptionId()), Utils.localize("messages.securitycraft:retinalScanner.notOwner", getOwner().getName()), ChatFormatting.RED);

				return true;
			}

			if (sendMessage.get())
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.RETINAL_SCANNER.getDescriptionId()), Utils.localize("messages.securitycraft:retinalScanner.hello", viewingPlayer.getName()), ChatFormatting.GREEN);
		}
		else if (activatedOnlyByPlayer())
			return false;

		level.setBlockAndUpdate(worldPosition, state.setValue(RetinalScannerBlock.POWERED, true));
		BlockUtils.updateIndirectNeighbors(level, worldPosition, SCContent.RETINAL_SCANNER);
		powerTicksLeft = length;

		return true;
	}

	@Override
	public <T> void onOptionChanged(Option<T> option) {
		if (option == signalLength && level != null) {
			powerTicksLeft = 0;
			level.setBlockAndUpdate(worldPosition, getBlockState().setValue(RetinalScannerBlock.POWERED, false));
			BlockUtils.updateIndirectNeighbors(level, worldPosition, getBlockState().getBlock());
		}

		super.onOptionChanged(option);
	}

	@Override
	public int getDefaultViewCooldown() {
		return getSignalLength() + 30;
	}

	@Override
	public int getViewCooldown() {
		return viewCooldown;
	}

	@Override
	public void setViewCooldown(int viewCooldown) {
		//not persisted (see load/saveAdditional), so this must not call setChanged() - doing so every tick while the
		//cooldown counts down needlessly marks the chunk dirty and re-fires the neighbour signal update every tick
		this.viewCooldown = viewCooldown;
	}

	@Override
	public boolean activatedOnlyByPlayer() {
		return !activatedByEntities.get();
	}

	public int getSignalLength() {
		return signalLength.get();
	}

	public boolean isDisabled() {
		return disabled.get();
	}

	@Override
	public double getMaximumDistance() {
		return maximumDistance.get();
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST, ModuleType.DISGUISE
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				activatedByEntities, sendMessage, signalLength, disabled, maximumDistance, respectInvisibility
		};
	}

	@Override
	public void setOwner(String name, String uuid) {
		super.setOwner(name, uuid);

		if (name != null && !name.isEmpty() && !name.equals("owner")) {
			setOwnerProfile(new com.mojang.authlib.GameProfile(null, name));
		}
	}

	public void setOwnerProfile(com.mojang.authlib.GameProfile ownerProfile) {
		this.ownerProfile = ownerProfile;
		updateOwnerProfile();
	}

	private void updateOwnerProfile() {
		if (ownerProfile != null && (!ownerProfile.isComplete() || !ownerProfile.getProperties().containsKey("textures"))) {
			net.minecraft.world.level.block.entity.SkullBlockEntity.updateGameprofile(ownerProfile, profile -> {
				this.ownerProfile = profile;
				setChanged();
				sync();
			});
		}
		else {
			setChanged();
			sync();
		}
	}

	public com.mojang.authlib.GameProfile getPlayerProfile() {
		return ownerProfile;
	}

	@Override
	public void saveAdditional(net.minecraft.nbt.CompoundTag tag) {
		super.saveAdditional(tag);

		if (!net.minecraft.util.StringUtil.isNullOrEmpty(getOwner().getName()) && !getOwner().getName().equals("owner") && ownerProfile != null) {
			net.minecraft.nbt.CompoundTag profileTag = new net.minecraft.nbt.CompoundTag();
			net.minecraft.nbt.NbtUtils.writeGameProfile(profileTag, ownerProfile);
			tag.put("ownerProfile", profileTag);
		}
	}

	@Override
	public void load(net.minecraft.nbt.CompoundTag tag) {
		super.load(tag);

		if (tag.contains("ownerProfile", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
			setOwnerProfile(net.minecraft.nbt.NbtUtils.readGameProfile(tag.getCompound("ownerProfile")));
		}
		else if (!net.minecraft.util.StringUtil.isNullOrEmpty(getOwner().getName()) && !getOwner().getName().equals("owner")) {
			setOwnerProfile(new com.mojang.authlib.GameProfile(null, getOwner().getName()));
		}
	}

	@Override
	public boolean isConsideredInvisible(LivingEntity entity) {
		return respectInvisibility.isConsideredInvisible(entity);
	}
}
