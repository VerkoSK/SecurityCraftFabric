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
import net.geforcemods.securitycraft.blocks.ScannerDoorBlock;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.ITickingBlockEntity;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Lower-half block entity for the {@link ScannerDoorBlock}: owner, allowlist module and the view-scan logic. */
public class ScannerDoorBlockEntity extends CustomizableBlockEntity implements IViewActivated, ITickingBlockEntity {
	private BooleanOption sendMessage = new BooleanOption("sendMessage", true);
	private SignalLengthOption signalLength = new SignalLengthOption(0);
	private DoubleOption maximumDistance = new DoubleOption("maximumDistance", 5.0D, 0.1D, 25.0D, 0.1D) {
		@Override
		public String getKey(String denotation) {
			return "option.generic.viewActivated.maximumDistance";
		}
	};
	private DisabledOption disabled = new DisabledOption(false);
	private RespectInvisibilityOption respectInvisibility = new RespectInvisibilityOption();
	private int viewCooldown = 0;

	public ScannerDoorBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.SCANNER_DOOR_BLOCK_ENTITY, pos, state);
	}

	@Override
	public void tick(Level level, BlockPos pos, BlockState state) {
		checkView(level, pos);
	}

	@Override
	public boolean onEntityViewed(LivingEntity entity, BlockHitResult hitResult) {
		if (isDisabled() || isConsideredInvisible(entity))
			return false;

		BlockState state = getBlockState();

		if (!(state.getBlock() instanceof ScannerDoorBlock block))
			return false;

		if (hitResult.getDirection().getAxis() != state.getValue(DoorBlock.FACING).getAxis())
			return false;

		if (!(entity instanceof Player player))
			return false;

		Owner viewer = new Owner(player);

		if (!isOwnedBy(player) && !isAllowed(viewer.getName())) {
			if (sendMessage.get())
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.SCANNER_DOOR_ITEM.getDescriptionId()), Utils.localize("messages.securitycraft:retinalScanner.notOwner", getOwner().getName()), ChatFormatting.RED);

			return true;
		}

		boolean willOpen = !state.getValue(DoorBlock.OPEN);

		block.activate(level, worldPosition);

		if (willOpen) {
			if (sendMessage.get())
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.SCANNER_DOOR_ITEM.getDescriptionId()), Utils.localize("messages.securitycraft:retinalScanner.hello", viewer.getName()), ChatFormatting.GREEN);

			if (signalLength.get() > 0)
				level.scheduleTick(worldPosition, block, signalLength.get());
		}

		return true;
	}

	@Override
	public int getDefaultViewCooldown() {
		return signalLength.get() + 30;
	}

	@Override
	public int getViewCooldown() {
		return viewCooldown;
	}

	@Override
	public void setViewCooldown(int viewCooldown) {
		this.viewCooldown = viewCooldown;
		setChanged();
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
				ModuleType.ALLOWLIST
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				sendMessage, signalLength, disabled, maximumDistance, respectInvisibility
		};
	}

	@Override
	public boolean isConsideredInvisible(LivingEntity entity) {
		return respectInvisibility.isConsideredInvisible(entity);
	}
}
