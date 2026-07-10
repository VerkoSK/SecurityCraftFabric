package net.geforcemods.securitycraft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.geforcemods.securitycraft.blockentities.KeypadBlockEntity;
import net.geforcemods.securitycraft.blocks.KeypadBlock;
import net.geforcemods.securitycraft.blocks.reinforced.BaseReinforcedBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Central registration for all blocks, items, block entities and the creative tab.
 *
 * <p>Since Minecraft 1.21.2 every block and item must carry its registry key in its settings
 * ({@code registryKey(...)}) before registration, so blocks are constructed inside {@link #register}
 * where the key is known rather than as static-final fields.
 */
public class SCContent {
	private static final List<ItemConvertible> TAB_ITEMS = new ArrayList<>();

	public static Block KEYPAD;
	public static Block REINFORCED_STONE;
	public static Block REINFORCED_COBBLESTONE;
	public static Block REINFORCED_STONE_BRICKS;
	public static Block REINFORCED_SMOOTH_STONE;
	public static Block REINFORCED_OAK_PLANKS;
	public static Block REINFORCED_DIRT;
	public static Block REINFORCED_IRON_BLOCK;
	public static Block REINFORCED_GLASS;

	public static BlockEntityType<KeypadBlockEntity> KEYPAD_BLOCK_ENTITY;

	public static final RegistryKey<ItemGroup> TAB_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, id("general"));

	public static Identifier id(String name) {
		return Identifier.of(SecurityCraft.MODID, name);
	}

	public static void init() {
		KEYPAD = register("keypad", key -> new KeypadBlock(baseSettings().registryKey(key)));
		REINFORCED_STONE = register("reinforced_stone", key -> new BaseReinforcedBlock(reinforcedOpaque(BlockSoundGroup.STONE).registryKey(key)));
		REINFORCED_COBBLESTONE = register("reinforced_cobblestone", key -> new BaseReinforcedBlock(reinforcedOpaque(BlockSoundGroup.STONE).registryKey(key)));
		REINFORCED_STONE_BRICKS = register("reinforced_stone_bricks", key -> new BaseReinforcedBlock(reinforcedOpaque(BlockSoundGroup.STONE).registryKey(key)));
		REINFORCED_SMOOTH_STONE = register("reinforced_smooth_stone", key -> new BaseReinforcedBlock(reinforcedOpaque(BlockSoundGroup.STONE).registryKey(key)));
		REINFORCED_OAK_PLANKS = register("reinforced_oak_planks", key -> new BaseReinforcedBlock(reinforcedOpaque(BlockSoundGroup.WOOD).registryKey(key)));
		REINFORCED_DIRT = register("reinforced_dirt", key -> new BaseReinforcedBlock(reinforcedOpaque(BlockSoundGroup.GRAVEL).registryKey(key)));
		REINFORCED_IRON_BLOCK = register("reinforced_iron_block", key -> new BaseReinforcedBlock(reinforcedOpaque(BlockSoundGroup.METAL).registryKey(key)));
		REINFORCED_GLASS = register("reinforced_glass", key -> new BaseReinforcedBlock(reinforcedGlass().registryKey(key)));

		KEYPAD_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("keypad"), FabricBlockEntityTypeBuilder.create(KeypadBlockEntity::new, KEYPAD).build());

		registerItemGroup();
	}

	private static Block register(String name, Function<RegistryKey<Block>, Block> factory) {
		Identifier identifier = id(name);
		RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, identifier);
		Block block = Registry.register(Registries.BLOCK, identifier, factory.apply(blockKey));
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, identifier);
		BlockItem item = Registry.register(Registries.ITEM, identifier, new BlockItem(block, new Item.Settings().registryKey(itemKey)));
		TAB_ITEMS.add(item);
		return block;
	}

	private static void registerItemGroup() {
		ItemGroup group = FabricItemGroup.builder()
				.icon(() -> new ItemStack(KEYPAD))
				.displayName(Text.translatable("itemGroup.securitycraft.general"))
				.entries((displayContext, entries) -> TAB_ITEMS.forEach(entries::add))
				.build();
		Registry.register(Registries.ITEM_GROUP, id("general"), group);
	}

	private static AbstractBlock.Settings baseSettings() {
		return AbstractBlock.Settings.create().strength(2.0F, 12000.0F).requiresTool();
	}

	private static AbstractBlock.Settings reinforcedOpaque(BlockSoundGroup sound) {
		return AbstractBlock.Settings.create().strength(2.0F, 12000.0F).requiresTool().sounds(sound);
	}

	private static AbstractBlock.Settings reinforcedGlass() {
		return AbstractBlock.Settings.create()
				.strength(1.5F, 12000.0F)
				.sounds(BlockSoundGroup.GLASS)
				.nonOpaque()
				.allowsSpawning((state, world, pos, type) -> false)
				.solidBlock((state, world, pos) -> false)
				.suffocates((state, world, pos) -> false)
				.blockVision((state, world, pos) -> false);
	}
}
