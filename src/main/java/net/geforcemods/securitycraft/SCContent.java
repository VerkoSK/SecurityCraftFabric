package net.geforcemods.securitycraft;

import java.util.ArrayList;
import java.util.List;

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

/** Central registration for all blocks, items, block entities and the creative tab. */
public class SCContent {
	private static final List<ItemLike> TAB_ITEMS = new ArrayList<>();

	// --- Blocks ---
	public static final Block KEYPAD = new KeypadBlock(BlockBehaviour.Properties.of().strength(2.0F, 12000.0F).requiresCorrectToolForDrops());
	public static final Block REINFORCED_STONE = reinforcedOpaque(SoundType.STONE);
	public static final Block REINFORCED_COBBLESTONE = reinforcedOpaque(SoundType.STONE);
	public static final Block REINFORCED_STONE_BRICKS = reinforcedOpaque(SoundType.STONE);
	public static final Block REINFORCED_SMOOTH_STONE = reinforcedOpaque(SoundType.STONE);
	public static final Block REINFORCED_OAK_PLANKS = reinforcedOpaque(SoundType.WOOD);
	public static final Block REINFORCED_DIRT = reinforcedOpaque(SoundType.GRAVEL);
	public static final Block REINFORCED_IRON_BLOCK = reinforcedOpaque(SoundType.METAL);
	public static final Block REINFORCED_GLASS = reinforcedGlass();

	// --- Block entities (assigned in init once the blocks are registered) ---
	public static BlockEntityType<KeypadBlockEntity> KEYPAD_BLOCK_ENTITY;

	// --- Creative tab ---
	public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("general"));

	public static ResourceLocation id(String name) {
		return ResourceLocation.fromNamespaceAndPath(SecurityCraft.MODID, name);
	}

	public static void init() {
		registerBlock("keypad", KEYPAD);
		registerBlock("reinforced_stone", REINFORCED_STONE);
		registerBlock("reinforced_cobblestone", REINFORCED_COBBLESTONE);
		registerBlock("reinforced_stone_bricks", REINFORCED_STONE_BRICKS);
		registerBlock("reinforced_smooth_stone", REINFORCED_SMOOTH_STONE);
		registerBlock("reinforced_oak_planks", REINFORCED_OAK_PLANKS);
		registerBlock("reinforced_dirt", REINFORCED_DIRT);
		registerBlock("reinforced_iron_block", REINFORCED_IRON_BLOCK);
		registerBlock("reinforced_glass", REINFORCED_GLASS);

		KEYPAD_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("keypad"), FabricBlockEntityTypeBuilder.create(KeypadBlockEntity::new, KEYPAD).build());

		registerCreativeTab();
	}

	private static void registerBlock(String name, Block block) {
		Registry.register(BuiltInRegistries.BLOCK, id(name), block);
		BlockItem item = new BlockItem(block, new Item.Properties());
		Registry.register(BuiltInRegistries.ITEM, id(name), item);
		TAB_ITEMS.add(item);
	}

	private static void registerCreativeTab() {
		CreativeModeTab tab = FabricItemGroup.builder()
				.icon(() -> new ItemStack(KEYPAD))
				.title(Component.translatable("itemGroup.securitycraft.general"))
				.displayItems((params, output) -> TAB_ITEMS.forEach(output::accept))
				.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY.location(), tab);
	}

	private static Block reinforcedOpaque(SoundType sound) {
		return new BaseReinforcedBlock(BlockBehaviour.Properties.of().strength(2.0F, 12000.0F).requiresCorrectToolForDrops().sound(sound));
	}

	private static Block reinforcedGlass() {
		return new BaseReinforcedBlock(BlockBehaviour.Properties.of()
				.strength(1.5F, 12000.0F)
				.sound(SoundType.GLASS)
				.noOcclusion()
				.isValidSpawn((state, level, pos, type) -> false)
				.isRedstoneConductor((state, level, pos) -> false)
				.isSuffocating((state, level, pos) -> false)
				.isViewBlocking((state, level, pos) -> false));
	}
}
