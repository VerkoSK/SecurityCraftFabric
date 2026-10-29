package net.geforcemods.securitycraft.blockentities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.ILockable;
import net.geforcemods.securitycraft.api.NamedBlockEntity;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Option.IgnoreOwnerOption;
import net.geforcemods.securitycraft.api.Option.IntOption;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity.TeleportationType;
import net.geforcemods.securitycraft.blocks.RiftStabilizerBlock;
import net.geforcemods.securitycraft.misc.BlockEntityTracker;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.network.NetworkHandler;
import net.geforcemods.securitycraft.network.SyncRiftStabilizerPayload;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.ITickingBlockEntity;
import net.geforcemods.securitycraft.util.IToggleableEntries;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RiftStabilizerBlockEntity extends DisguisableBlockEntity implements ITickingBlockEntity, ILockable, IToggleableEntries<TeleportationType> {
	private final IntOption signalLength = new IntOption("signalLength", 60, 0, 400, 5); //20 seconds max
	private final IntOption range = new IntOption("range", 5, 1, 15, 1);
	private final DisabledOption disabled = new DisabledOption(false);
	private IgnoreOwnerOption ignoreOwner = new IgnoreOwnerOption(true);
	private final Map<TeleportationType, Boolean> teleportationFilter = new EnumMap<>(TeleportationType.class);
	private double lastTeleportDistance;
	private TeleportationType lastTeleportationType;
	private boolean tracked = false;

	public RiftStabilizerBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.RIFT_STABILIZER_BLOCK_ENTITY, pos, state);
		//when adding new types ONLY ADD TO THE END. anything else will break saved data.
		//ordering is done in ToggleListScreen based on the user's current language
		teleportationFilter.put(TeleportationType.CHORUS_FRUIT, true);
		teleportationFilter.put(TeleportationType.ENDER_PEARL, true);
		teleportationFilter.put(TeleportationType.ENDERMAN, false);
		teleportationFilter.put(TeleportationType.SHULKER, false);
		teleportationFilter.put(TeleportationType.MODDED, false);
	}

	@Override
	public void tick(Level level, BlockPos pos, BlockState state) {
		if (!tracked) {
			BlockEntityTracker.RIFT_STABILIZER.track(this);
			tracked = true;
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();

		// Stop tracking Rift Stabilizers when they are removed from the world
		BlockEntityTracker.RIFT_STABILIZER.stopTracking(this);
	}

	@Override
	public void setFilter(TeleportationType teleportationType, boolean allowed) {
		if (teleportationFilter.containsKey(teleportationType)) {
			teleportationFilter.put(teleportationType, allowed);
			setChanged();

			if (level.isClientSide)
				NetworkHandler.sendToServer(new SyncRiftStabilizerPayload(worldPosition, teleportationType, allowed));

			RiftStabilizerBlockEntity connectedBlockEntity = RiftStabilizerBlock.getConnectedBlockEntity(level, worldPosition);

			if (connectedBlockEntity != null) {
				connectedBlockEntity.teleportationFilter.put(teleportationType, allowed);
				connectedBlockEntity.setChanged();
			}
		}
	}

	@Override
	public boolean getFilter(TeleportationType teleportationType) {
		return teleportationFilter.containsKey(teleportationType) && teleportationFilter.get(teleportationType);
	}

	@Override
	public ToIntFunction<TeleportationType> getComparatorOutputFunction() {
		return t -> t.ordinal() + 1;
	}

	@Override
	public Map<TeleportationType, Boolean> getFilters() {
		return teleportationFilter;
	}

	@Override
	public TeleportationType getDefaultType() {
		return TeleportationType.MODDED;
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);

		CompoundTag teleportationNBT = new CompoundTag();
		int i = 0;

		for (boolean b : teleportationFilter.values()) {
			teleportationNBT.putBoolean("teleportationType" + i, b);
			i++;
		}

		tag.put("teleportationTypes", teleportationNBT);
		tag.putDouble("lastTeleportDistance", lastTeleportDistance);

		if (lastTeleportationType != null)
			tag.putInt("lastTeleportationType", lastTeleportationType.ordinal());
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		if (tag.contains("teleportationTypes", Tag.TAG_COMPOUND)) {
			CompoundTag teleportationNBT = tag.getCompound("teleportationTypes");
			int i = 0;

			for (TeleportationType teleportationType : teleportationFilter.keySet()) {
				teleportationFilter.put(teleportationType, teleportationNBT.getBoolean("teleportationType" + i));
				i++;
			}
		}

		lastTeleportDistance = tag.getDouble("lastTeleportDistance");

		if (tag.contains("lastTeleportationType"))
			lastTeleportationType = TeleportationType.values()[tag.getInt("lastTeleportationType")];
	}

	@Override
	public void onModuleInserted(ItemStack stack, ModuleType module, boolean toggled) {
		super.onModuleInserted(stack, module, toggled);

		RiftStabilizerBlockEntity connectedBlockEntity = RiftStabilizerBlock.getConnectedBlockEntity(level, worldPosition);

		if (connectedBlockEntity != null && (toggled ? !connectedBlockEntity.isModuleEnabled(module) : !connectedBlockEntity.hasModule(module)))
			connectedBlockEntity.insertModule(stack, toggled);

		if (module == ModuleType.DISGUISE) {
			onInsertDisguiseModule(this, stack);

			if (connectedBlockEntity != null)
				onInsertDisguiseModule(connectedBlockEntity, stack);
		}
	}

	@Override
	public void onModuleRemoved(ItemStack stack, ModuleType module, boolean toggled) {
		super.onModuleRemoved(stack, module, toggled);

		RiftStabilizerBlockEntity connectedBlockEntity = RiftStabilizerBlock.getConnectedBlockEntity(level, worldPosition);

		if (connectedBlockEntity != null && (toggled ? connectedBlockEntity.isModuleEnabled(module) : connectedBlockEntity.hasModule(module)))
			connectedBlockEntity.removeModule(module, toggled);

		if (module == ModuleType.DISGUISE) {
			onRemoveDisguiseModule(this);

			if (connectedBlockEntity != null)
				onRemoveDisguiseModule(connectedBlockEntity);
		}
		else if (module == ModuleType.SMART) {
			onRemoveSmartModule(this);

			if (connectedBlockEntity != null)
				onRemoveSmartModule(connectedBlockEntity);
		}
	}

	private void onInsertDisguiseModule(BlockEntity be, ItemStack stack) {
		if (!be.getLevel().isClientSide)
			be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
		else
			ClientUtils.recompileChunk(be.getBlockPos());
	}

	private void onRemoveDisguiseModule(BlockEntity be) {
		if (!be.getLevel().isClientSide)
			be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
		else
			ClientUtils.recompileChunk(be.getBlockPos());
	}

	private void onRemoveSmartModule(RiftStabilizerBlockEntity be) {
		be.teleportationFilter.put(TeleportationType.CHORUS_FRUIT, true);
		be.teleportationFilter.put(TeleportationType.ENDER_PEARL, true);
		be.teleportationFilter.put(TeleportationType.ENDERMAN, false);
		be.teleportationFilter.put(TeleportationType.SHULKER, false);
		be.teleportationFilter.put(TeleportationType.MODDED, false);
	}

	public void setLastTeleport(double teleportDistance, TeleportationType type) {
		lastTeleportDistance = teleportDistance;
		lastTeleportationType = type;
	}

	public double getLastTeleportDistance() {
		return lastTeleportDistance;
	}

	public TeleportationType getLastTeleportationType() {
		return lastTeleportationType;
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST, ModuleType.DISGUISE, ModuleType.REDSTONE, ModuleType.HARMING, ModuleType.SMART
		};
	}

	@Override
	public <T> void onOptionChanged(Option<T> option) {
		RiftStabilizerBlockEntity connectedBlockEntity = RiftStabilizerBlock.getConnectedBlockEntity(level, worldPosition);

		if (connectedBlockEntity != null) {
			if (option instanceof IntOption io) {
				if (option == signalLength)
					connectedBlockEntity.setSignalLength(io.get());
				else if (option == range)
					connectedBlockEntity.setRange(io.get());
				else
					throw new UnsupportedOperationException("Unhandled option synchronization in rift stabilizer! " + option.getName());
			}
			else if (option instanceof BooleanOption bo) {
				if (option == disabled)
					connectedBlockEntity.setDisabled(bo.get());
				else if (option == ignoreOwner)
					connectedBlockEntity.setIgnoresOwner(bo.get());
				else
					throw new UnsupportedOperationException("Unhandled option synchronization in rift stabilizer! " + option.getName());
			}
			else
				throw new UnsupportedOperationException("Unhandled option synchronization in rift stabilizer! " + option.getName());
		}

		super.onOptionChanged(option);
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				signalLength, range, disabled, ignoreOwner
		};
	}

	@Override
	public void onOwnerChanged(BlockState state, Level level, BlockPos pos, Player player, Owner oldOwner, Owner newOwner) {
		RiftStabilizerBlockEntity be = RiftStabilizerBlock.getConnectedBlockEntity(level, pos);

		if (be != null) {
			be.setOwner(getOwner().getName(), getOwner().getUUID());

			if (!level.isClientSide)
				level.getServer().getPlayerList().broadcastAll(be.getUpdatePacket());
		}

		super.onOwnerChanged(state, level, pos, player, oldOwner, newOwner);
	}

	public void setSignalLength(int signalLength) {
		if (getSignalLength() != signalLength) {
			this.signalLength.setValue(signalLength);
			level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.POWERED, false));
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); //sync option change to client
			setChanged();
		}
	}

	public int getSignalLength() {
		return signalLength.get();
	}

	public void setRange(int range) {
		if (getRange() != range) {
			this.range.setValue(range);
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); //sync option change to client
			setChanged();
		}
	}

	public int getRange() {
		return range.get();
	}

	public void setDisabled(boolean disabled) {
		if (isDisabled() != disabled) {
			this.disabled.setValue(disabled);
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); //sync option change to client
			setChanged();
		}
	}

	public boolean isDisabled() {
		return disabled.get();
	}

	public void setIgnoresOwner(boolean ignoresOwner) {
		if (ignoresOwner() != ignoresOwner) {
			ignoreOwner.setValue(ignoresOwner);
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); //sync option change to client
			setChanged();
		}
	}

	@Override
	public boolean ignoresOwner() {
		return ignoreOwner.get();
	}

	@Override
	public void setCustomName(Component customName) {
		super.setCustomName(customName);

		if (getBlockState().getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER && level.getBlockEntity(worldPosition.above()) instanceof NamedBlockEntity nameSetter)
			nameSetter.setCustomName(customName);
	}

	public static boolean onEntityTeleport(Entity entity, Vec3 prev, Vec3 target, TeleportationType type) {
		if (type == null)
			return false;

		Level level = entity.level();
		List<RiftStabilizerBlockEntity> targetPosBlockEntities = BlockEntityTracker.RIFT_STABILIZER.getBlockEntitiesInRange(level, target);
		List<RiftStabilizerBlockEntity> sourcePosBlockEntities = BlockEntityTracker.RIFT_STABILIZER.getBlockEntitiesInRange(level, prev);
		List<RiftStabilizerBlockEntity> blockEntities = new ArrayList<>();
		RiftStabilizerBlockEntity riftStabilizer = null;
		boolean targetPosProhibited = false;

		blockEntities.addAll(targetPosBlockEntities);
		blockEntities.addAll(sourcePosBlockEntities);
		blockEntities = blockEntities.stream().distinct().sorted(Comparator.comparingDouble(b -> Math.min(b.getBlockPos().distToCenterSqr(target), b.getBlockPos().distToCenterSqr(prev)))).toList();

		for (RiftStabilizerBlockEntity be : blockEntities) {
			if (!be.isDisabled() && be.getFilter(type) && (!(entity instanceof Player player) || !(be.isOwnedBy(player) && be.ignoresOwner()) && !be.isAllowed(player))) {
				riftStabilizer = be;
				targetPosProhibited = be.getBlockPos().distToCenterSqr(target) < be.getBlockPos().distToCenterSqr(prev);
				break;
			}
		}

		if (riftStabilizer != null) {
			BlockPos pos = riftStabilizer.getBlockPos();
			Vec3 centerPos = new AABB(pos).getCenter();
			Vec3 from = targetPosProhibited ? target : prev;
			Vec3 distance = from.subtract(centerPos);

			if (entity instanceof Player player) {
				level.playSound(null, prev.x, prev.y, prev.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.5F);
				PlayerUtils.sendMessageToPlayer(player, SCContent.RIFT_STABILIZER.getName(), Component.translatable(targetPosProhibited ? "messages.securitycraft:rift_stabilizer.no_teleport_to" : "messages.securitycraft:rift_stabilizer.no_teleport_from"), ChatFormatting.RED);

				if (riftStabilizer.isModuleEnabled(ModuleType.HARMING))
					player.hurt(entity.damageSources().fall(), 5.0F);
			}

			riftStabilizer.setLastTeleport(Math.max(Math.abs(distance.x), Math.max(Math.abs(distance.y), Math.abs(distance.z))) - 0.5D, type);

			if (riftStabilizer.isModuleEnabled(ModuleType.REDSTONE)) {
				int signalLength = riftStabilizer.getSignalLength();

				level.setBlockAndUpdate(pos, riftStabilizer.getBlockState().cycle(RiftStabilizerBlock.POWERED));
				BlockUtils.updateIndirectNeighbors(level, pos, SCContent.RIFT_STABILIZER);

				if (signalLength > 0)
					level.scheduleTick(pos, SCContent.RIFT_STABILIZER, signalLength);
			}

			return true;
		}

		return false;
	}

	public enum TeleportationType {
		CHORUS_FRUIT(Items.CHORUS_FRUIT.getDescriptionId()),
		ENDER_PEARL(Items.ENDER_PEARL.getDescriptionId()),
		ENDERMAN(EntityType.ENDERMAN.getDescriptionId()),
		SHULKER(EntityType.SHULKER.getDescriptionId()),
		MODDED("gui.securitycraft:rift_stabilizer.modded");

		public final String label;

		TeleportationType(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}
