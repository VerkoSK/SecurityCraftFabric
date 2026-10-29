package net.geforcemods.securitycraft.entity.sentry;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.ICustomizable;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.IgnoreOwnerOption;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.api.SecurityCraftAPI;
import net.geforcemods.securitycraft.blockentities.DisguisableBlockEntity;
import net.geforcemods.securitycraft.blocks.SometimesVisibleBlock;
import net.geforcemods.securitycraft.components.ListModuleData;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.geforcemods.securitycraft.items.BlockReinforcerItem;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Sentry extends PathfinderMob implements RangedAttackMob, IOwnable, IModuleInventory, ICustomizable {
	private static final EntityDataAccessor<Owner> OWNER = SynchedEntityData.<Owner>defineId(Sentry.class, Owner.getSerializer());
	private static final EntityDataAccessor<CompoundTag> MODULE = SynchedEntityData.<CompoundTag>defineId(Sentry.class, EntityDataSerializers.COMPOUND_TAG);
	private static final EntityDataAccessor<CompoundTag> ALLOWLIST = SynchedEntityData.<CompoundTag>defineId(Sentry.class, EntityDataSerializers.COMPOUND_TAG);
	private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.<Integer>defineId(Sentry.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> HEAD_ROTATION = SynchedEntityData.<Float>defineId(Sentry.class, EntityDataSerializers.FLOAT);
	public static final int MAX_TARGET_DISTANCE = 20;
	private float headYTranslation = 0.9F;
	private final float stepSize = 0.025F;
	private boolean animateUpwards = true;
	private boolean animate = false;
	private long previousTargetId = Long.MIN_VALUE;
	private NonNullList<ItemStack> modules = NonNullList.<ItemStack>withSize(getMaxNumberOfModules(), ItemStack.EMPTY);
	private Map<ModuleType, Boolean> moduleStates = new EnumMap<>(ModuleType.class);
	private IgnoreOwnerOption ignoreOwner = new IgnoreOwnerOption(true);
	private boolean shutDown = false;
	public final Predicate<Entity> SENTRY_PREDICATE = entity -> !ConfigHandler.sentryAttackableEntitiesDenylist.contains(Utils.getRegistryName(entity.getType()).toString());

	public Sentry(EntityType<Sentry> type, Level level) {
		super(SCContent.SENTRY_ENTITY, level);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(OWNER, new Owner());
		entityData.define(MODULE, new CompoundTag());
		entityData.define(ALLOWLIST, new CompoundTag());
		entityData.define(MODE, SentryMode.CAMOUFLAGE_HP.ordinal());
		entityData.define(HEAD_ROTATION, 0.0F);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new AttackRangedIfEnabledGoal(this, () -> isModuleEnabled(ModuleType.SPEED) ? 5 : 10, 10.0F));
		targetSelector.addGoal(1, new TargetNearestPlayerOrMobGoal(this));
	}

	@Override
	public void tick() {
		super.tick();

		if (level().isClientSide) {
			if (!animate && headYTranslation > 0.0F)
				animate = true;

			if (animate) {
				if (animateUpwards && headYTranslation > 0.0F) {
					headYTranslation -= stepSize;

					if (headYTranslation <= 0.0F) {
						animateUpwards = false;
						animate = false;
					}
				}
				else if (!animateUpwards && headYTranslation < 0.9F) {
					headYTranslation += stepSize;

					if (headYTranslation >= 0.9F) {
						animateUpwards = true;
						animate = false;
					}
				}
			}
		}
		else if (level().getBlockState(blockPosition()).isAir()) {
			ItemStack disguiseModule = getDisguiseModule();

			if (!disguiseModule.isEmpty()) {
				Block addon = ModuleItem.getBlockAddon(disguiseModule);

				if (addon != null) {
					BlockState disguisedState = addon.defaultBlockState();

					level().setBlockAndUpdate(blockPosition(), SCContent.SENTRY_DISGUISE.defaultBlockState().setValue(SometimesVisibleBlock.INVISIBLE, false).setValue(SometimesVisibleBlock.WATERLOGGED, disguisedState.getFluidState().isSource()));

					if (level().getBlockEntity(blockPosition()) instanceof DisguisableBlockEntity be) {
						be.insertModule(disguiseModule, false);
						be.setOwner(getOwner().getName(), getOwner().getUUID());
					}
				}
			}
		}
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag dataTag) {
		animateUpwards = true;
		animate = true;
		return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		BlockPos pos = blockPosition();
		ItemStack stack = player.getItemInHand(hand);

		if (isOwnedBy(player) && hand == InteractionHand.MAIN_HAND) {
			Item item = stack.getItem();

			if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
				serverPlayer.closeContainer();
			else if (player.level().isClientSide && net.fabricmc.api.EnvType.CLIENT == net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType())
				net.geforcemods.securitycraft.util.ClientUtils.closePlayerScreen();

			if (player.isCrouching()) {
				kill();
				return InteractionResult.SUCCESS;
			}
			else if (item == SCContent.UNIVERSAL_BLOCK_REMOVER) {
				if (!ConfigHandler.vanillaToolBlockBreaking)
					kill();
				else
					player.displayClientMessage(Utils.localize("messages.securitycraft:SubType.cantBeRemoved"), true);

				return InteractionResult.SUCCESS;
			}
			else if (item == SCContent.DISGUISE_MODULE) {
				ItemStack module = getDisguiseModule();

				if (!module.isEmpty()) {
					onModuleRemoved(module, ModuleType.DISGUISE, false);
					removeModule(ModuleType.DISGUISE, false);
					Block.popResource(level(), pos, module);
				}
				else {
					insertModule(stack, false);
					onModuleInserted(stack, ModuleType.DISGUISE, false);

					if (!player.isCreative())
						player.setItemInHand(hand, ItemStack.EMPTY);
				}

				return InteractionResult.SUCCESS;
			}
			else if (item == SCContent.ALLOWLIST_MODULE) {
				ItemStack module = getAllowlistModule();

				if (!module.isEmpty()) {
					onModuleRemoved(module, ModuleType.ALLOWLIST, false);
					removeModule(ModuleType.ALLOWLIST, false);
					Block.popResource(level(), pos, module);
				}
				else {
					insertModule(stack, false);
					onModuleInserted(stack, ModuleType.ALLOWLIST, false);

					if (!player.isCreative())
						player.setItemInHand(hand, ItemStack.EMPTY);
				}

				return InteractionResult.SUCCESS;
			}
			else if (item == SCContent.SPEED_MODULE) {
				if (hasModule(ModuleType.SPEED)) {
					ItemStack module = getModule(ModuleType.SPEED);

					onModuleRemoved(module, ModuleType.SPEED, false);
					removeModule(ModuleType.SPEED, false);
					Block.popResource(level(), pos, module);
				}
				else {
					insertModule(stack, false);

					if (!player.isCreative())
						player.setItemInHand(hand, ItemStack.EMPTY);
				}

				return InteractionResult.SUCCESS;
			}
			else if (item == SCContent.UNIVERSAL_BLOCK_MODIFIER) {
				level().destroyBlock(pos, false);

				if (!getDisguiseModule().isEmpty()) {
					ItemStack module = getDisguiseModule();

					onModuleRemoved(module, ModuleType.DISGUISE, false);
					removeModule(ModuleType.DISGUISE, false);
					Block.popResource(level(), pos, module);
				}

				if (!getAllowlistModule().isEmpty()) {
					ItemStack module = getAllowlistModule();

					onModuleRemoved(module, ModuleType.ALLOWLIST, false);
					removeModule(ModuleType.ALLOWLIST, false);
					Block.popResource(level(), pos, module);
				}

				if (hasModule(ModuleType.SPEED)) {
					ItemStack module = getModule(ModuleType.SPEED);

					onModuleRemoved(module, ModuleType.SPEED, false);
					removeModule(ModuleType.SPEED, false);
					Block.popResource(level(), pos, module);
				}

				entityData.set(MODULE, new CompoundTag());
				entityData.set(ALLOWLIST, new CompoundTag());
				return InteractionResult.SUCCESS;
			}
			else if (item == SCContent.UNIVERSAL_OWNER_CHANGER) {
				String newOwner = stack.getHoverName().getString();

				setOwner(newOwner, PlayerUtils.isPlayerOnline(newOwner) ? PlayerUtils.getPlayerFromName(newOwner).getUUID().toString() : "ownerUUID");

				if (!level().isClientSide && level().getBlockEntity(pos) instanceof IOwnable ownable)
					ownable.setOwner(getOwner().getName(), getOwner().getUUID());

				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.UNIVERSAL_OWNER_CHANGER.getDescriptionId()), Utils.localize("messages.securitycraft:universalOwnerChanger.changed", newOwner), ChatFormatting.GREEN);
				return InteractionResult.SUCCESS;
			}
			else if (item == SCContent.REMOTE_ACCESS_SENTRY) {
				item.useOn(new UseOnContext(player, hand, new BlockHitResult(new Vec3(blockPosition().getX(), blockPosition().getY(), blockPosition().getZ()), Direction.NORTH, pos, false)));
				return InteractionResult.SUCCESS;
			}
			else if (item == Items.NAME_TAG || item instanceof BlockReinforcerItem)
				return super.mobInteract(player, hand);
			else {
				toggleMode(player);
				return InteractionResult.SUCCESS;
			}
		}
		else if (!isOwnedBy(player) && hand == InteractionHand.MAIN_HAND && stack.getItem() == SCContent.UNIVERSAL_OWNER_CHANGER && isDefaultOwner()) {
			if (ConfigHandler.allowBlockClaim) {
				String newOwner = player.getName().getString();

				if (!player.isCreative())
					stack.shrink(1);

				setOwner(newOwner, player.getUUID().toString());

				if (!level().isClientSide && level().getBlockEntity(pos) instanceof IOwnable ownable)
					ownable.setOwner(getOwner().getName(), getOwner().getUUID());

				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.UNIVERSAL_OWNER_CHANGER.getDescriptionId()), Utils.localize("messages.securitycraft:universalOwnerChanger.claimed"), ChatFormatting.GREEN);
			}
			else
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.UNIVERSAL_OWNER_CHANGER.getDescriptionId()), Utils.localize("messages.securitycraft:universalOwnerChanger.noClaiming"), ChatFormatting.RED);

			return InteractionResult.SUCCESS;
		}

		return super.mobInteract(player, hand);
	}

	/**
	 * Cleanly removes this sentry from the world, dropping the module and removing the block the sentry is disguised with
	 */
	@Override
	public void remove(RemovalReason reason) {
		BlockPos pos = blockPosition();

		if (!level().isClientSide) {
			ItemStack sentryStack = new ItemStack(SCContent.SENTRY);

			if (hasCustomName())
				sentryStack.setHoverName(getCustomName());

			Block.popResource(level(), pos, sentryStack);
			dropAllModules();
		}

		level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()); //if the robustness of the sentry was increased, the disguise block is not removed when the sentry is removed
		super.remove(reason);
	}

	@Override
	public void kill() {
		remove(RemovalReason.KILLED);
		gameEvent(GameEvent.ENTITY_DIE);
	}

	/**
	 * Sets this sentry's mode to the next one and sends the player a message about the switch
	 *
	 * @param player The player that threw the switch
	 */
	public void toggleMode(Player player) {
		toggleMode(player, entityData.get(MODE) + 1, true);
	}

	/**
	 * Sets this sentry's mode to the given mode (or 0 if the mode is not one of 0, 1, 2) and sends the player a message about
	 * the switch if wanted
	 *
	 * @param player The player that threw the switch
	 * @param mode The mode (int) that the sentry should be switched to
	 * @param sendMessage Whether a message should be sent to the player informing them about the switch
	 */
	public void toggleMode(Player player, int mode, boolean sendMessage) {
		if (mode < 0 || mode >= SentryMode.values().length)
			mode = 0;

		entityData.set(MODE, mode);

		if (SentryMode.values()[mode].isCamouflage()) {
			animateUpwards = true;

			if (headYTranslation > 0.0F)
				animate = true;

			if (level().getBlockState(blockPosition()).getBlock() == SCContent.SENTRY_DISGUISE)
				level().setBlockAndUpdate(blockPosition(), level().getBlockState(blockPosition()).setValue(SometimesVisibleBlock.INVISIBLE, false));
		}
		else {
			animateUpwards = false;

			if (headYTranslation < 0.9F || !animate)
				animate = true;

			setTarget(null);

			if (level().getBlockState(blockPosition()).getBlock() == SCContent.SENTRY_DISGUISE)
				level().setBlockAndUpdate(blockPosition(), level().getBlockState(blockPosition()).setValue(SometimesVisibleBlock.INVISIBLE, true));
		}

		if (sendMessage && player.level().isClientSide)
			PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.SENTRY.getDescriptionId()), Utils.localize(SentryMode.values()[mode].getModeKey()).append(Utils.localize(SentryMode.values()[mode].getDescriptionKey())), ChatFormatting.DARK_RED, true);
	}

	@Override
	public void setTarget(LivingEntity target) {
		if (getMode() != SentryMode.CAMOUFLAGE_IDLE && (target == null && previousTargetId != Long.MIN_VALUE || (target != null && previousTargetId != target.getId()))) {
			if (getMode().isCamouflage() && target == null) {
				animateUpwards = true;
				animate = true;

				if (level().getBlockState(blockPosition()).getBlock() == SCContent.SENTRY_DISGUISE)
					level().setBlockAndUpdate(blockPosition(), level().getBlockState(blockPosition()).setValue(SometimesVisibleBlock.INVISIBLE, false));
			}
			else if (target != null) {
				animateUpwards = false;

				if (headYTranslation == 0.0F)
					animate = true;

				if (level().getBlockState(blockPosition()).getBlock() == SCContent.SENTRY_DISGUISE)
					level().setBlockAndUpdate(blockPosition(), level().getBlockState(blockPosition()).setValue(SometimesVisibleBlock.INVISIBLE, true));
			}
		}

		previousTargetId = target == null ? Long.MIN_VALUE : target.getId();
		super.setTarget(target);

		if (!level().isClientSide && target != null) {
			double d0 = target.getX() - getX();
			double d1 = target.getZ() - getZ();

			entityData.set(HEAD_ROTATION, (float) (Mth.atan2(d1, d0) * (180D / Math.PI) + 90));
		}
	}

	@Override
	public float getEyeHeight(net.minecraft.world.entity.Pose pose) { //the sentry's eyes are higher so that it can see targets even if it's in a block
		return 1.5F;
	}

	@Override
	public void performRangedAttack(LivingEntity target, float distanceFactor) {
		double d0 = target.getX() - getX();
		double d1 = target.getBoundingBox().minY + target.getBbHeight() / 3.0F - getY() - 1.25D;
		double d2 = target.getZ() - getZ();
		double d3 = Math.sqrt(d0 * d0 + d2 * d2);
		Collection<MobEffectInstance> potionEffects = Collections.emptyList();
		Projectile throwableEntity = null;
		BlockEntity be = level().getBlockEntity(blockPosition().below());

		animateUpwards = false;
		animate = true;
		entityData.set(HEAD_ROTATION, (float) (Mth.atan2(d2, d0) * (180D / Math.PI) + 90));

		if (be instanceof Container container) {
			boolean shootOtherProjectiles = isModuleEnabled(ModuleType.SMART);

			for (int i = 0; i < container.getContainerSize(); i++) {
				ItemStack stack = container.getItem(i);

				if (!stack.isEmpty()) {
					Item item = stack.getItem();

					if (item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) {
						ItemStack extracted = container.removeItem(i, 1);

						potionEffects = PotionUtils.getMobEffects(extracted);
						break;
					}
					else if (shootOtherProjectiles) {
						if (((DispenserBlock) Blocks.DISPENSER).getDispenseMethod(stack) instanceof AbstractProjectileDispenseBehavior behavior) {
							ItemStack extracted = container.removeItem(i, 1);

							throwableEntity = behavior.getProjectile(level(), position().add(0.0D, 1.25D, 0.0D), extracted);

							if (throwableEntity instanceof AbstractArrow arrow)
								arrow.pickup = Pickup.CREATIVE_ONLY;

							break;
						}
						else if (item instanceof ISentryBulletContainer bulletContainer) {
							ItemStack extracted = container.removeItem(i, 1);

							throwableEntity = bulletContainer.getProjectileFromItem(this, level(), position().add(0.0D, 1.25D, 0.0D), extracted);
							break;
						}
					}
				}
			}
		}

		if (throwableEntity == null) {
			throwableEntity = new Bullet(level(), this, potionEffects);
			throwableEntity.setPos(getX(), getY() + 1.25D, getZ());
			playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
		}
		else {
			throwableEntity.setOwner(this);

			if (throwableEntity instanceof ThrowableProjectile)
				d1 += 0.25D;

			if (((DispenserBlock) Blocks.DISPENSER).getDispenseMethod(new ItemStack(Items.ARROW)) instanceof AbstractProjectileDispenseBehavior behavior && level() instanceof ServerLevel serverLevel)
				behavior.playSound(new BlockSourceImpl(serverLevel, blockPosition()));
		}

		gameEvent(GameEvent.PROJECTILE_SHOOT);
		throwableEntity.shoot(d0, d1 + d3 * 0.2F, d2, 1.6F, 0.0F);
		level().addFreshEntity(throwableEntity);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		tag.put("TileEntityData", getDisguiseModuleTag());
		tag.put(" allowlist", getAllowlistModuleTag());
		writeModuleInventory(tag);
		writeModuleStates(tag);
		writeOptions(tag);
		tag.putInt("SentryMode", entityData.get(MODE));
		tag.putFloat("HeadRotation", entityData.get(HEAD_ROTATION));
		tag.putBoolean("ShutDown", shutDown);
		getOwner().save(tag, needsValidation());
		super.addAdditionalSaveData(tag);
	}

	private CompoundTag getDisguiseModuleTag() {
		CompoundTag tag = new CompoundTag();
		ItemStack stack = entityData.get(MODULE).isEmpty() ? getModule(ModuleType.DISGUISE) : ItemStack.of(entityData.get(MODULE));

		if (!stack.isEmpty())
			stack.save(tag);

		return tag;
	}

	private CompoundTag getAllowlistModuleTag() {
		CompoundTag tag = new CompoundTag();
		ItemStack stack = entityData.get(ALLOWLIST).isEmpty() ? getModule(ModuleType.ALLOWLIST) : ItemStack.of(entityData.get(ALLOWLIST));

		if (!stack.isEmpty())
			stack.save(tag);

		return tag;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		CompoundTag teTag = tag.getCompound("TileEntityData");
		CompoundTag allowlistTag = tag.getCompound(" allowlist");

		modules = readModuleInventory(tag);
		moduleStates = readModuleStates(tag);
		readOptions(tag);
		entityData.set(MODULE, teTag);
		entityData.set(ALLOWLIST, allowlistTag);
		entityData.set(MODE, tag.getInt("SentryMode"));
		entityData.set(HEAD_ROTATION, tag.getFloat("HeadRotation"));
		shutDown = tag.getBoolean("ShutDown");
		getOwner().load(tag);
		super.readAdditionalSaveData(tag);
	}

	@Override
	public Owner getOwner() {
		return entityData.get(OWNER);
	}

	@Override
	public void setOwner(String name, String uuid) {
		entityData.set(OWNER, new Owner(name, uuid));
	}

	public void setDisguiseModule(ItemStack disguiseModule) {
		Block addon = ModuleItem.getBlockAddon(disguiseModule);

		if (addon != null) {
			BlockState disguisedState = addon.defaultBlockState();

			level().setBlockAndUpdate(blockPosition(), SCContent.SENTRY_DISGUISE.defaultBlockState().setValue(SometimesVisibleBlock.INVISIBLE, !getMode().isCamouflage()).setValue(SometimesVisibleBlock.WATERLOGGED, disguisedState.getFluidState().isSource()));

			if (level().getBlockEntity(blockPosition()) instanceof DisguisableBlockEntity be) {
				be.insertModule(disguiseModule, false);
				be.setOwner(getOwner().getName(), getOwner().getUUID());
			}
		}

		entityData.set(MODULE, disguiseModule.save(new CompoundTag()));
	}

	public void setAllowlistModule(ItemStack allowlistModule) {
		entityData.set(ALLOWLIST, allowlistModule.save(new CompoundTag()));
	}

	public ItemStack getDisguiseModule() {
		CompoundTag tag = entityData.get(MODULE);

		if (tag == null || tag.isEmpty())
			return getModule(ModuleType.DISGUISE);
		else
			return ItemStack.of(tag);
	}

	public ItemStack getAllowlistModule() {
		CompoundTag tag = entityData.get(ALLOWLIST);

		if (tag == null || tag.isEmpty())
			return getModule(ModuleType.ALLOWLIST);
		else
			return ItemStack.of(tag);
	}

	public SentryMode getMode() {
		int mode = entityData.get(MODE);

		return mode < 0 || mode >= SentryMode.values().length ? SentryMode.CAMOUFLAGE_HP : SentryMode.values()[mode];
	}

	public float getHeadYTranslation(float partialTicks) {
		if (!animate)
			return headYTranslation;

		return animateUpwards ? Mth.lerp(partialTicks, headYTranslation + stepSize, headYTranslation) : Mth.lerp(partialTicks, headYTranslation - stepSize, headYTranslation);
	}

	public boolean isTargetingAllowedPlayer(LivingEntity potentialTarget) {
		if (potentialTarget != null && getMode().attacksPlayers()) {
			ListModuleData data = ListModuleData.fromStack(getAllowlistModule());

			return data.affectEveryone() || data.isPlayerOnList(potentialTarget.getName().getString()) || data.isTeamOfPlayerOnList(level(), potentialTarget.getName().getString());
		}

		return false;
	}

	public boolean isAllowed(LivingEntity potentialTarget) {
		return (ignoresOwner() && isOwnedBy(potentialTarget)) || isTargetingAllowedPlayer(potentialTarget) || IModuleInventory.super.isAllowed(potentialTarget);
	}

	public static boolean isAllowedToAttack(LivingEntity potentialTarget) {
		return SecurityCraftAPI.getRegisteredSentryAttackTargetChecks().stream().anyMatch(check -> check.canAttack(potentialTarget));
	}

	public void setShutDown(boolean shutDown) {
		this.shutDown = shutDown;
	}

	public boolean isShutDown() {
		return shutDown;
	}

	public boolean isDefaultOwner() {
		return getOwner().isDefaultOwner();
	}

	//start: disallow sentry from taking damage
	@Override
	public boolean doHurtTarget(Entity entity) {
		return false;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public boolean attackable() {
		return false;
	}
	//end: disallow sentry from taking damage

	@Override
	public boolean checkSpawnRules( net.minecraft.world.level.LevelAccessor level, MobSpawnType spawnReason) {
		return false;
	}

	@Override
	protected void jumpFromGround() {} //sentries don't jump!

	@Override
	public boolean isPathFinding() {
		return false;
	}

	@Override
	public void checkDespawn() {} //sentries don't despawn

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false; //sentries don't despawn
	}

	//sentries are heavy, so don't push them around!
	@Override
	public void playerTouch(Player entity) {}

	@Override
	public void move(MoverType type, Vec3 vec) {} //no moving sentries!

	@Override
	protected void doPush(Entity entity) {}

	@Override
	protected void pushEntities() {}

	@Override
	public boolean ignoreExplosion() {
		return true; //does not get pushed around by explosions
	}

	@Override
	public boolean isPickable() {
		return true; //needs to stay true so blocks can't be broken through the sentry
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public Direction getMotionDirection() {
		return Direction.UP;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return null;
	}

	@Override
	public void playAmbientSound() {}

	@Override
	public void thunderHit(ServerLevel level, net.minecraft.world.entity.LightningBolt lightning) {
		if (!level.isClientSide)
			shutDown = !shutDown;
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(SCContent.SENTRY);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}

	public float getOriginalHeadRotation() {
		return entityData.get(HEAD_ROTATION);
	}

	@Override
	public Level myLevel() {
		return level();
	}

	@Override
	public BlockPos myPos() {
		return blockPosition();
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		return modules;
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST, ModuleType.DISGUISE, ModuleType.SPEED, ModuleType.SMART
		};
	}

	@Override
	public boolean isModuleEnabled(ModuleType module) {
		return hasModule(module) && moduleStates.get(module) == Boolean.TRUE; //prevent NPE
	}

	@Override
	public void toggleModuleState(ModuleType module, boolean shouldBeEnabled) {
		moduleStates.put(module, shouldBeEnabled);
	}

	@Override
	public void onModuleInserted(ItemStack stack, ModuleType module, boolean toggled) {
		IModuleInventory.super.onModuleInserted(stack, module, toggled);

		if (module == ModuleType.ALLOWLIST)
			setAllowlistModule(stack);
		else if (module == ModuleType.DISGUISE)
			setDisguiseModule(stack);
	}

	@Override
	public void onModuleRemoved(ItemStack stack, ModuleType module, boolean toggled) {
		IModuleInventory.super.onModuleRemoved(stack, module, toggled);

		if (module == ModuleType.ALLOWLIST)
			entityData.set(ALLOWLIST, new CompoundTag());
		else if (module == ModuleType.DISGUISE) {
			level().setBlockAndUpdate(blockPosition(), Blocks.AIR.defaultBlockState());
			entityData.set(MODULE, new CompoundTag());
		}
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				ignoreOwner
		};
	}

	@Override
	public boolean ignoresOwner() {
		return ignoreOwner.get();
	}

	public void setupSentry(Player owner) {
		setOwner(owner.getName().getString(), owner.getGameProfile().getId().toString());
	}

	public enum SentryMode {
		AGGRESSIVE_HP(true, true, false),
		AGGRESSIVE_H(true, false, false),
		AGGRESSIVE_P(false, true, false),
		CAMOUFLAGE_HP(true, true, true),
		CAMOUFLAGE_H(true, false, true),
		CAMOUFLAGE_P(false, true, true),
		IDLE(false, false, false),
		CAMOUFLAGE_IDLE(false, false, true);

		private final boolean attacksHostile;
		private final boolean attacksPlayers;
		private final boolean camouflage;

		SentryMode(boolean attacksHostile, boolean attacksPlayers, boolean camouflage) {
			this.attacksHostile = attacksHostile;
			this.attacksPlayers = attacksPlayers;
			this.camouflage = camouflage;
		}

		public boolean attacksHostile() {
			return attacksHostile;
		}

		public boolean attacksPlayers() {
			return attacksPlayers;
		}

		public boolean isCamouflage() {
			return camouflage;
		}

		public String getModeKey() {
			return "messages.securitycraft:sentry.mode" + (ordinal() + 1);
		}

		public String getDescriptionKey() {
			return switch (this) {
				case AGGRESSIVE_HP, CAMOUFLAGE_HP -> "messages.securitycraft:sentry.descriptionMode1";
				case AGGRESSIVE_H, CAMOUFLAGE_H -> "messages.securitycraft:sentry.descriptionMode2";
				case AGGRESSIVE_P, CAMOUFLAGE_P -> "messages.securitycraft:sentry.descriptionMode3";
				case IDLE, CAMOUFLAGE_IDLE -> "messages.securitycraft:sentry.descriptionMode4";
			};
		}
	}
}
