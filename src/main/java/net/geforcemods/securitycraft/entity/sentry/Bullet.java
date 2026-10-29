package net.geforcemods.securitycraft.entity.sentry;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.Sets;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.Owner;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class Bullet extends AbstractArrow {
	private static final EntityDataAccessor<Owner> OWNER = SynchedEntityData.defineId(Bullet.class, Owner.getSerializer());
	private Collection<MobEffectInstance> potionEffects = Sets.newHashSet();

	public Bullet(EntityType<Bullet> type, Level level) {
		super(SCContent.BULLET_ENTITY, level);
		pickup = Pickup.DISALLOWED;
	}

	public Bullet(Level level, Sentry shooter, Collection<MobEffectInstance> potionEffects) {
		super(SCContent.BULLET_ENTITY, shooter, level);
		Owner owner = shooter.getOwner();

		this.potionEffects = potionEffects;
		entityData.set(OWNER, new Owner(owner.getName(), owner.getUUID()));
		pickup = Pickup.DISALLOWED;
	}

	/**
	 * @return The owner of the sentry that shot this bullet
	 */
	public Owner getSCOwner() {
		return entityData.get(OWNER);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(OWNER, new Owner());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);

		if (!potionEffects.isEmpty()) {
			ListTag list = new ListTag();

			for (MobEffectInstance effect : potionEffects) {
				list.add(effect.save(new CompoundTag()));
			}

			tag.put("CustomPotionEffects", list);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);

		if (tag.contains("CustomPotionEffects", Tag.TAG_LIST)) {
			ListTag list = tag.getList("CustomPotionEffects", Tag.TAG_COMPOUND);

			for (int i = 0; i < list.size(); ++i) {
				MobEffectInstance effect = MobEffectInstance.load(list.getCompound(i));

				if (effect != null)
					potionEffects.add(effect);
			}
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult raytraceResult) {
		Entity target = raytraceResult.getEntity();

		if (!(target instanceof Sentry) && !(target instanceof TamableAnimal animal && animal.getOwnerUUID() != null && animal.getOwnerUUID().toString().equals(entityData.get(OWNER).getUUID()))) {
			target.hurt(damageSources().arrow(this, getOwner()), (float) ConfigHandler.SERVER.sentryBulletDamage.get());

			if (target instanceof LivingEntity lEntity && !potionEffects.isEmpty()) {
				for (MobEffectInstance effect : potionEffects) {
					lEntity.addEffect(effect);
				}
			}
		}

		discard();
	}

	@Override
	protected void onHitBlock(BlockHitResult raytraceResult) {
		playSound(SoundEvents.STONE_HIT, 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
		discard();
	}

	@Override
	protected void doPostHurtEffects(LivingEntity living) {
		discard();
	}

	@Override
	public void playerTouch(Player entity) {}

	@Override
	protected ItemStack getPickupItem() {
		return ItemStack.EMPTY;
	}

	public Collection<MobEffectInstance> getPotionEffects() {
		return Collections.unmodifiableCollection(potionEffects);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}
}
