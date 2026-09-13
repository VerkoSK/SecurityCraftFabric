package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.IViewActivated;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Option.DoubleOption;
import net.geforcemods.securitycraft.api.Option.IntOption;
import net.geforcemods.securitycraft.api.Option.RespectInvisibilityOption;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blocks.ScannerTrapdoorBlock;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.ITickingBlockEntity;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Block entity for the {@link ScannerTrapdoorBlock}: owner, allowlist module and the view-scan logic. */
public class ScannerTrapdoorBlockEntity extends CustomizableBlockEntity implements IViewActivated, ITickingBlockEntity {
	private BooleanOption sendMessage = new BooleanOption("sendMessage", true);
	private IntOption signalLength = new IntOption("signalLength", 60, 0, 400, 5);
	private DoubleOption maximumDistance = new DoubleOption("maximumDistance", 5.0D, 0.1D, 25.0D, 0.1D) {
		@Override
		public String getKey(String denotation) {
			return "option.generic.viewActivated.maximumDistance";
		}
	};
	private DisabledOption disabled = new DisabledOption(false);
	private RespectInvisibilityOption respectInvisibility = new RespectInvisibilityOption();
	private int viewCooldown = 0;

	public ScannerTrapdoorBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.SCANNER_TRAPDOOR_BLOCK_ENTITY, pos, state);
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

		if (!(state.getBlock() instanceof ScannerTrapdoorBlock block) || !(entity instanceof Player player))
			return false;

		boolean correctFace = state.getValue(TrapDoorBlock.OPEN) ? hitResult.getDirection().getAxis() == state.getValue(HorizontalDirectionalBlock.FACING).getAxis() : hitResult.getDirection().getAxis() == Direction.Axis.Y;

		if (!correctFace)
			return false;

		Owner viewer = new Owner(player);

		if (!isOwnedBy(player) && !isAllowed(viewer.getName())) {
			if (sendMessage.get())
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.SCANNER_TRAPDOOR.getDescriptionId()), Utils.localize("messages.securitycraft:retinalScanner.notOwner", getOwner().getName()), ChatFormatting.RED);

			return true;
		}

		boolean willOpen = !state.getValue(TrapDoorBlock.OPEN);

		block.activate(level, worldPosition);

		if (willOpen) {
			if (sendMessage.get())
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.SCANNER_TRAPDOOR.getDescriptionId()), Utils.localize("messages.securitycraft:retinalScanner.hello", viewer.getName()), ChatFormatting.GREEN);

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
