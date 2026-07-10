package net.geforcemods.securitycraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.geforcemods.securitycraft.blockentities.KeypadBlockEntity;
import net.geforcemods.securitycraft.blocks.KeypadBlock;
import net.geforcemods.securitycraft.blocks.reinforced.BaseReinforcedBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Central registration for all blocks, items, block entities and the creative tab.
 *
 * <p>Since Minecraft 1.21.2 every block and item must carry its registry id in its
 * {@code Properties} ({@code setId(...)}) before registration, so blocks are constructed inside
 * {@link #register} where the id is known, rather than as static-final fields.
 */
public class SCContent {
	private static final List<ItemLike> TAB_ITEMS = new ArrayList<>();

	// --- Blocks (assigned in init, once their ids are known) ---
	public static Block KEYPAD;
	public static Block REINFORCED_STONE;
	public static Block REINFORCED_COBBLESTONE;
	public static Block REINFORCED_STONE_BRICKS;
	public static Block REINFORCED_SMOOTH_STONE;
	public static Block REINFORCED_OAK_PLANKS;
	public static Block REINFORCED_DIRT;
	public static Block REINFORCED_IRON_BLOCK;
	public static Block REINFORCED_GLASS;

	// --- Block entities ---
	public static BlockEntityType<KeypadBlockEntity> KEYPAD_BLOCK_ENTITY;

	// --- Creative tab ---
	public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("general"));

	public static ResourceLocation id(String name) {
		return ResourceLocation.fromNamespaceAndPath(SecurityCraft.MODID, name);
	}

	public static void init() {
		KEYPAD = register("keypad", key -> new KeypadBlock(baseProps().setId(key)));
		REINFORCED_STONE = register("reinforced_stone", key -> new BaseReinforcedBlock(reinforcedOpaque(SoundType.STONE).setId(key)));
		REINFORCED_COBBLESTONE = register("reinforced_cobblestone", key -> new BaseReinforcedBlock(reinforcedOpaque(SoundType.STONE).setId(key)));
		REINFORCED_STONE_BRICKS = register("reinforced_stone_bricks", key -> new BaseReinforcedBlock(reinforcedOpaque(SoundType.STONE).setId(key)));
		REINFORCED_SMOOTH_STONE = register("reinforced_smooth_stone", key -> new BaseReinforcedBlock(reinforcedOpaque(SoundType.STONE).setId(key)));
		REINFORCED_OAK_PLANKS = register("reinforced_oak_planks", key -> new BaseReinforcedBlock(reinforcedOpaque(SoundType.WOOD).setId(key)));
		REINFORCED_DIRT = register("reinforced_dirt", key -> new BaseReinforcedBlock(reinforcedOpaque(SoundType.GRAVEL).setId(key)));
		REINFORCED_IRON_BLOCK = register("reinforced_iron_block", key -> new BaseReinforcedBlock(reinforcedOpaque(SoundType.METAL).setId(key)));
		REINFORCED_GLASS = register("reinforced_glass", key -> new BaseReinforcedBlock(reinforcedGlass().setId(key)));

		KEYPAD_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("keypad"), FabricBlockEntityTypeBuilder.create(KeypadBlockEntity::new, KEYPAD).build());

		registerCreativeTab();
	}

	private static Block register(String name, Function<ResourceKey<Block>, Block> factory) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id(name));
		Block block = Registry.register(BuiltInRegistries.BLOCK, blockKey, factory.apply(blockKey));
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id(name));
		BlockItem item = Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey)));
		TAB_ITEMS.add(item);
		return block;
	}

	private static void registerCreativeTab() {
		CreativeModeTab tab = FabricItemGroup.builder()
				.icon(() -> new ItemStack(KEYPAD))
				.title(Component.translatable("itemGroup.securitycraft.general"))
				.displayItems((params, output) -> TAB_ITEMS.forEach(output::accept))
				.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY.location(), tab);
	}

	private static BlockBehaviour.Properties baseProps() {
		return BlockBehaviour.Properties.of().strength(2.0F, 12000.0F).requiresCorrectToolForDrops();
	}

	private static BlockBehaviour.Properties reinforcedOpaque(SoundType sound) {
		return BlockBehaviour.Properties.of().strength(2.0F, 12000.0F).requiresCorrectToolForDrops().sound(sound);
	}

	private static BlockBehaviour.Properties reinforcedGlass() {
		return BlockBehaviour.Properties.of()
				.strength(1.5F, 12000.0F)
				.sound(SoundType.GLASS)
				.noOcclusion()
				.isValidSpawn((state, level, pos, type) -> false)
				.isRedstoneConductor((state, level, pos) -> false)
				.isSuffocating((state, level, pos) -> false)
				.isViewBlocking((state, level, pos) -> false);
	}
}
