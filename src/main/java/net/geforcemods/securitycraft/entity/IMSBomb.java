package net.geforcemods.securitycraft.entity;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blockentities.IMSBlockEntity;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

/**
 * 1:1 with the original SecurityCraft {@code entity.IMSBomb}: launches straight up, then homes onto its target.
 * Upstream keeps the owner and the launch time in custom synched data; registering a custom
 * {@code EntityDataSerializer} needs a Forge-only registry, so both are plain fields here and the launch time is
 * written to NBT so it survives a reload.
 */
public class IMSBomb extends Fireball {
	private Owner owner = new Owner();
	private int ticksFlying = 0;
	private int launchTime;
	private boolean launching = true;
	private boolean isFast;
	private Vec3 upwardsSpeed;

	public IMSBomb(EntityType<IMSBomb> type, Level level) {
		super(SCContent.IMS_BOMB_ENTITY, level);
	}

	public IMSBomb(Level level, double x, double y, double z, Vec3 acceleration, int height, IMSBlockEntity be) {
		super(SCContent.IMS_BOMB_ENTITY, x, y, z, acceleration, level);

		Owner beOwner = be.getOwner();

		owner = new Owner(beOwner.getName(), beOwner.getUUID());
		launchTime = height * 3; //the ims bomb entity travels upwards by 1/3 blocks per tick
		isFast = be.isModuleEnabled(ModuleType.SPEED);
	}

	@Override
	public void tick() {
		if (!launching)
			super.tick();
		else {
			//move up before homing onto target
			if (ticksFlying < launchTime) {
				if (upwardsSpeed == null)
					upwardsSpeed = new Vec3(0, isFast ? 0.66F : 0.33F, 0);

				ticksFlying += isFast ? 2 : 1;
				move(MoverType.SELF, upwardsSpeed);
			}
			else
				launching = false;
		}
	}

	@Override
	protected void onHit(HitResult result) {
		if (!level().isClientSide && result.getType() == Type.BLOCK && level().getBlockState(((BlockHitResult) result).getBlockPos()).getBlock() != SCContent.IMS) {
			BlockPos impactPos = ((BlockHitResult) result).getBlockPos();

			level().explode(this, impactPos.getX(), impactPos.getY() + 1D, impactPos.getZ(), ConfigHandler.smallerMineExplosion ? 3.5F : 7F, ConfigHandler.shouldSpawnFire, BlockUtils.getExplosionInteraction());
			discard();
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("launchTime", launchTime);
		tag.putInt("ticksFlying", ticksFlying);
		tag.putBoolean("launching", launching);
		tag.putBoolean("isFast", isFast);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		launchTime = tag.getInt("launchTime");
		ticksFlying = tag.getInt("ticksFlying");
		launching = tag.getBoolean("launching");
		isFast = tag.getBoolean("isFast");
	}

	/**
	 * @return The owner of the IMS which shot this bullet
	 */
	public Owner getSCOwner() {
		return owner;
	}

	@Override
	protected float getInertia() {
		return isFast ? 1.5F : 1.0F;
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return true;
	}

	@Override
	protected MovementEmission getMovementEmission() {
		return MovementEmission.NONE;
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public float getPickRadius() {
		return 0.3F;
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
		return new ClientboundAddEntityPacket(this, serverEntity);
	}
}
