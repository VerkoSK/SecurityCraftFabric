package net.geforcemods.securitycraft.items;

import java.util.List;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.misc.CustomDamageSources;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class TaserItem extends Item implements FabricItem {
	private boolean powered;

	public TaserItem(Item.Properties properties, boolean isPowered) {
		super(properties);

		powered = isPowered;
	}

	@Override
	public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
		return oldStack.getItem() != newStack.getItem();
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!stack.isDamaged()) {
			if (player.isCrouching() && (player.isCreative() || !powered)) {
				ItemStack oneRedstone = new ItemStack(Items.REDSTONE, 1);

				if (player.isCreative()) {
					if (player.getItemInHand(hand).getItem() == SCContent.TASER)
						player.setItemInHand(hand, new ItemStack(SCContent.TASER_POWERED, 1));
					else
						player.setItemInHand(hand, new ItemStack(SCContent.TASER, 1));

					return InteractionResultHolder.success(stack);
				}
				else if (player.getInventory().contains(oneRedstone)) {
					int redstoneSlot = player.getInventory().findSlotMatchingUnusedItem(oneRedstone);
					ItemStack redstoneStack;

					if (redstoneSlot == -1) {
						if (player.getOffhandItem().getItem() == Items.REDSTONE)
							redstoneStack = player.getOffhandItem();
						else
							return InteractionResultHolder.pass(stack);
					}
					else
						redstoneStack = player.getInventory().getItem(redstoneSlot);

					redstoneStack.setCount(redstoneStack.getCount() - 1);

					if (redstoneSlot == -1)
						player.getInventory().offhand.set(0, redstoneStack);
					else
						player.getInventory().setItem(redstoneSlot, redstoneStack);

					player.setItemInHand(hand, new ItemStack(SCContent.TASER_POWERED, 1));
					return InteractionResultHolder.success(stack);
				}

				return InteractionResultHolder.pass(stack);
			}

			int range = 11;
			Vec3 startVec = player.getEyePosition(1.0F);
			Vec3 lookVec = player.getViewVector(1.0F).scale(range);
			Vec3 endVec = startVec.add(lookVec);
			AABB boundingBox = player.getBoundingBox().expandTowards(lookVec).inflate(1, 1, 1);
			EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(player, startVec, endVec, boundingBox, LivingEntity.class::isInstance, range * range);

			level.playSound(player, player.blockPosition(), SCSounds.TASERFIRED.event, SoundSource.PLAYERS, 1.0F, 1.0F);
			level.gameEvent(player, GameEvent.PROJECTILE_SHOOT, player.blockPosition());

			if (hitResult != null) {
				LivingEntity entity = (LivingEntity) hitResult.getEntity();
				double damage = powered ? ConfigHandler.SERVER.poweredTaserDamage.get() : ConfigHandler.SERVER.taserDamage.get();

				if ((damage == 0.0D || entity.hurt(CustomDamageSources.taser(player), (float) damage)) && !entity.isBlocking()) {
					List<? extends String> effects = powered ? ConfigHandler.SERVER.poweredTaserEffects.get() : ConfigHandler.SERVER.taserEffects.get();

					for (String effectString : effects) {
						MobEffectInstance effect = parseEffect(effectString);

						if (effect != null)
							entity.addEffect(effect);
					}
				}
			}

			if (!player.isCreative()) {
				if (powered) {
					ItemStack taser = new ItemStack(SCContent.TASER, 1);

					taser.hurtAndBreak(150, player, p -> p.broadcastBreakEvent(hand));
					player.setItemInHand(hand, taser);
				}
				else
					stack.hurtAndBreak(150, player, p -> p.broadcastBreakEvent(hand));
			}

			return InteractionResultHolder.consume(stack);
		}

		return InteractionResultHolder.pass(stack);
	}

	private static MobEffectInstance parseEffect(String configValue) {
		try {
			String[] split = configValue.split("\\|");
			MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(split[0]));

			if (effect != null)
				return new MobEffectInstance(effect, Integer.parseInt(split[2]), Integer.parseInt(split[1]) - 1);
		}
		catch (Exception e) {
			SecurityCraft.LOGGER.warn("Invalid taser effect config entry: {}", configValue, e);
		}

		return null;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotIndex, boolean isSelected) {
		if (!level.isClientSide && stack.getDamageValue() >= 1)
			stack.setDamageValue(stack.getDamageValue() - 1);
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}
}
