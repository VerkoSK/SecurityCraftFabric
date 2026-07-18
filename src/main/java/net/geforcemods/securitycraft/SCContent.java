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
import net.minecraft.block.FenceBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.PillarBlock;
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

/** Central registration. Keypad + full reinforced block set (Yarn mappings). */
public class SCContent {
	private static final List<ItemConvertible> TAB_ITEMS = new ArrayList<>();
	public static final List<Block> GLASS_BLOCKS = new ArrayList<>();
	public static final List<Block> REINFORCED_BLOCKS = new ArrayList<>();

	public static Block KEYPAD;
	public static BlockEntityType<KeypadBlockEntity> KEYPAD_BLOCK_ENTITY;

	public static final RegistryKey<ItemGroup> TAB_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, id("general"));

	private static final String[][] REINFORCED = {
			{"reinforced_acacia_log", "pillar"},
			{"reinforced_acacia_planks", "cube"},
			{"reinforced_acacia_wood", "pillar"},
			{"reinforced_amethyst_block", "cube"},
			{"reinforced_andesite", "cube"},
			{"reinforced_bamboo_block", "pillar"},
			{"reinforced_bamboo_fence", "fence"},
			{"reinforced_bamboo_mosaic", "cube"},
			{"reinforced_bamboo_planks", "cube"},
			{"reinforced_basalt", "pillar"},
			{"reinforced_birch_log", "pillar"},
			{"reinforced_birch_planks", "cube"},
			{"reinforced_birch_wood", "pillar"},
			{"reinforced_black_concrete", "cube"},
			{"reinforced_black_terracotta", "cube"},
			{"reinforced_black_wool", "cube"},
			{"reinforced_blackstone", "cube"},
			{"reinforced_blue_concrete", "cube"},
			{"reinforced_blue_ice", "cube"},
			{"reinforced_blue_terracotta", "cube"},
			{"reinforced_blue_wool", "cube"},
			{"reinforced_bone_block", "pillar"},
			{"reinforced_bricks", "cube"},
			{"reinforced_brown_concrete", "cube"},
			{"reinforced_brown_terracotta", "cube"},
			{"reinforced_brown_wool", "cube"},
			{"reinforced_calcite", "cube"},
			{"reinforced_cherry_log", "pillar"},
			{"reinforced_cherry_planks", "cube"},
			{"reinforced_cherry_wood", "pillar"},
			{"reinforced_chiseled_copper", "cube"},
			{"reinforced_chiseled_deepslate", "cube"},
			{"reinforced_chiseled_nether_bricks", "cube"},
			{"reinforced_chiseled_polished_blackstone", "cube"},
			{"reinforced_chiseled_quartz_block", "cube"},
			{"reinforced_chiseled_red_sandstone", "cube"},
			{"reinforced_chiseled_sandstone", "cube"},
			{"reinforced_chiseled_stone_bricks", "cube"},
			{"reinforced_chiseled_tuff", "cube"},
			{"reinforced_chiseled_tuff_bricks", "cube"},
			{"reinforced_clay", "cube"},
			{"reinforced_coal_block", "cube"},
			{"reinforced_coarse_dirt", "cube"},
			{"reinforced_cobbled_deepslate", "cube"},
			{"reinforced_cobblestone", "cube"},
			{"reinforced_copper_block", "cube"},
			{"reinforced_cracked_deepslate_bricks", "cube"},
			{"reinforced_cracked_deepslate_tiles", "cube"},
			{"reinforced_cracked_nether_bricks", "cube"},
			{"reinforced_cracked_polished_blackstone_bricks", "cube"},
			{"reinforced_cracked_stone_bricks", "cube"},
			{"reinforced_crimson_hyphae", "pillar"},
			{"reinforced_crimson_nylium", "cube"},
			{"reinforced_crimson_planks", "cube"},
			{"reinforced_crimson_stem", "pillar"},
			{"reinforced_crying_obsidian", "cube"},
			{"reinforced_cut_copper", "cube"},
			{"reinforced_cut_red_sandstone", "cube"},
			{"reinforced_cut_sandstone", "cube"},
			{"reinforced_cyan_concrete", "cube"},
			{"reinforced_cyan_terracotta", "cube"},
			{"reinforced_cyan_wool", "cube"},
			{"reinforced_dark_oak_log", "pillar"},
			{"reinforced_dark_oak_planks", "cube"},
			{"reinforced_dark_oak_wood", "pillar"},
			{"reinforced_dark_prismarine", "cube"},
			{"reinforced_deepslate", "pillar"},
			{"reinforced_deepslate_bricks", "cube"},
			{"reinforced_deepslate_tiles", "cube"},
			{"reinforced_diamond_block", "cube"},
			{"reinforced_diorite", "cube"},
			{"reinforced_dirt", "cube"},
			{"reinforced_dripstone_block", "cube"},
			{"reinforced_emerald_block", "cube"},
			{"reinforced_end_stone", "cube"},
			{"reinforced_end_stone_bricks", "cube"},
			{"reinforced_exposed_chiseled_copper", "cube"},
			{"reinforced_exposed_copper", "cube"},
			{"reinforced_exposed_cut_copper", "cube"},
			{"reinforced_glowstone", "cube"},
			{"reinforced_gold_block", "cube"},
			{"reinforced_granite", "cube"},
			{"reinforced_grass_path", "cube"},
			{"reinforced_gravel", "cube"},
			{"reinforced_gray_concrete", "cube"},
			{"reinforced_gray_terracotta", "cube"},
			{"reinforced_gray_wool", "cube"},
			{"reinforced_green_concrete", "cube"},
			{"reinforced_green_terracotta", "cube"},
			{"reinforced_green_wool", "cube"},
			{"reinforced_hardened_clay", "cube"},
			{"reinforced_ice", "cube"},
			{"reinforced_iron_bars", "pane"},
			{"reinforced_iron_block", "cube"},
			{"reinforced_jungle_log", "pillar"},
			{"reinforced_jungle_planks", "cube"},
			{"reinforced_jungle_wood", "pillar"},
			{"reinforced_lapis_block", "cube"},
			{"reinforced_light_blue_concrete", "cube"},
			{"reinforced_light_blue_terracotta", "cube"},
			{"reinforced_light_blue_wool", "cube"},
			{"reinforced_light_gray_concrete", "cube"},
			{"reinforced_light_gray_terracotta", "cube"},
			{"reinforced_light_gray_wool", "cube"},
			{"reinforced_lime_concrete", "cube"},
			{"reinforced_lime_terracotta", "cube"},
			{"reinforced_lime_wool", "cube"},
			{"reinforced_magenta_concrete", "cube"},
			{"reinforced_magenta_terracotta", "cube"},
			{"reinforced_magenta_wool", "cube"},
			{"reinforced_magma_block", "cube"},
			{"reinforced_mangrove_log", "pillar"},
			{"reinforced_mangrove_planks", "cube"},
			{"reinforced_mangrove_wood", "pillar"},
			{"reinforced_moss_block", "cube"},
			{"reinforced_mossy_cobblestone", "cube"},
			{"reinforced_mossy_stone_bricks", "cube"},
			{"reinforced_mud", "cube"},
			{"reinforced_mud_bricks", "cube"},
			{"reinforced_nether_bricks", "cube"},
			{"reinforced_nether_wart_block", "cube"},
			{"reinforced_netherite_block", "cube"},
			{"reinforced_netherrack", "cube"},
			{"reinforced_oak_log", "pillar"},
			{"reinforced_oak_planks", "cube"},
			{"reinforced_oak_wood", "pillar"},
			{"reinforced_obsidian", "cube"},
			{"reinforced_ochre_froglight", "pillar"},
			{"reinforced_orange_concrete", "cube"},
			{"reinforced_orange_terracotta", "cube"},
			{"reinforced_orange_wool", "cube"},
			{"reinforced_oxidized_chiseled_copper", "cube"},
			{"reinforced_oxidized_copper", "cube"},
			{"reinforced_oxidized_cut_copper", "cube"},
			{"reinforced_packed_ice", "cube"},
			{"reinforced_packed_mud", "cube"},
			{"reinforced_pearlescent_froglight", "pillar"},
			{"reinforced_pink_concrete", "cube"},
			{"reinforced_pink_terracotta", "cube"},
			{"reinforced_pink_wool", "cube"},
			{"reinforced_polished_andesite", "cube"},
			{"reinforced_polished_basalt", "pillar"},
			{"reinforced_polished_blackstone", "cube"},
			{"reinforced_polished_blackstone_bricks", "cube"},
			{"reinforced_polished_deepslate", "cube"},
			{"reinforced_polished_diorite", "cube"},
			{"reinforced_polished_granite", "cube"},
			{"reinforced_polished_tuff", "cube"},
			{"reinforced_prismarine", "cube"},
			{"reinforced_prismarine_bricks", "cube"},
			{"reinforced_purple_concrete", "cube"},
			{"reinforced_purple_terracotta", "cube"},
			{"reinforced_purple_wool", "cube"},
			{"reinforced_purpur_block", "cube"},
			{"reinforced_purpur_pillar", "pillar"},
			{"reinforced_quartz_block", "cube"},
			{"reinforced_quartz_bricks", "cube"},
			{"reinforced_quartz_pillar", "pillar"},
			{"reinforced_raw_copper_block", "cube"},
			{"reinforced_raw_gold_block", "cube"},
			{"reinforced_raw_iron_block", "cube"},
			{"reinforced_red_concrete", "cube"},
			{"reinforced_red_nether_bricks", "cube"},
			{"reinforced_red_sand", "cube"},
			{"reinforced_red_sandstone", "cube"},
			{"reinforced_red_terracotta", "cube"},
			{"reinforced_red_wool", "cube"},
			{"reinforced_redstone_block", "cube"},
			{"reinforced_rooted_dirt", "cube"},
			{"reinforced_sand", "cube"},
			{"reinforced_sandstone", "cube"},
			{"reinforced_shroomlight", "cube"},
			{"reinforced_smooth_basalt", "cube"},
			{"reinforced_smooth_quartz", "cube"},
			{"reinforced_smooth_red_sandstone", "cube"},
			{"reinforced_smooth_sandstone", "cube"},
			{"reinforced_smooth_stone", "cube"},
			{"reinforced_snow_block", "cube"},
			{"reinforced_soul_sand", "cube"},
			{"reinforced_soul_soil", "cube"},
			{"reinforced_spruce_log", "pillar"},
			{"reinforced_spruce_planks", "cube"},
			{"reinforced_spruce_wood", "pillar"},
			{"reinforced_stone", "cube"},
			{"reinforced_stone_bricks", "cube"},
			{"reinforced_stone_polished_andesite", "cube"},
			{"reinforced_stone_polished_granite", "cube"},
			{"reinforced_stripped_acacia_log", "pillar"},
			{"reinforced_stripped_acacia_wood", "pillar"},
			{"reinforced_stripped_bamboo_block", "pillar"},
			{"reinforced_stripped_birch_log", "pillar"},
			{"reinforced_stripped_birch_wood", "pillar"},
			{"reinforced_stripped_cherry_log", "pillar"},
			{"reinforced_stripped_cherry_wood", "pillar"},
			{"reinforced_stripped_crimson_hyphae", "pillar"},
			{"reinforced_stripped_crimson_stem", "pillar"},
			{"reinforced_stripped_dark_oak_log", "pillar"},
			{"reinforced_stripped_dark_oak_wood", "pillar"},
			{"reinforced_stripped_jungle_log", "pillar"},
			{"reinforced_stripped_jungle_wood", "pillar"},
			{"reinforced_stripped_mangrove_log", "pillar"},
			{"reinforced_stripped_mangrove_wood", "pillar"},
			{"reinforced_stripped_oak_log", "pillar"},
			{"reinforced_stripped_oak_wood", "pillar"},
			{"reinforced_stripped_spruce_log", "pillar"},
			{"reinforced_stripped_spruce_wood", "pillar"},
			{"reinforced_stripped_warped_hyphae", "pillar"},
			{"reinforced_stripped_warped_stem", "pillar"},
			{"reinforced_tinted_glass", "glass"},
			{"reinforced_tuff", "cube"},
			{"reinforced_tuff_bricks", "cube"},
			{"reinforced_verdant_froglight", "pillar"},
			{"reinforced_warped_hyphae", "pillar"},
			{"reinforced_warped_nylium", "cube"},
			{"reinforced_warped_planks", "cube"},
			{"reinforced_warped_stem", "pillar"},
			{"reinforced_warped_wart_block", "cube"},
			{"reinforced_weathered_chiseled_copper", "cube"},
			{"reinforced_weathered_copper", "cube"},
			{"reinforced_weathered_cut_copper", "cube"},
			{"reinforced_white_concrete", "cube"},
			{"reinforced_white_terracotta", "cube"},
			{"reinforced_white_wool", "cube"},
			{"reinforced_yellow_concrete", "cube"},
			{"reinforced_yellow_terracotta", "cube"},
			{"reinforced_yellow_wool", "cube"}
	};

	public static Identifier id(String name) {
		return Identifier.of(SecurityCraft.MODID, name);
	}

	public static void init() {
		KEYPAD = register("keypad", key -> new KeypadBlock(AbstractBlock.Settings.create().strength(2.0F, 12000.0F).requiresTool().registryKey(key)));

		for (String[] entry : REINFORCED)
			registerReinforced(entry[0], entry[1]);

		KEYPAD_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("keypad"), FabricBlockEntityTypeBuilder.create(KeypadBlockEntity::new, KEYPAD).build());
		registerItemGroup();
	}

	private static void registerReinforced(String name, String category) {
		Block block = register(name, key -> switch (category) {
			case "pillar" -> new PillarBlock(pillarProps(name).registryKey(key));
			case "glass" -> new BaseReinforcedBlock(glassProps().registryKey(key));
			case "pane" -> new PaneBlock(paneProps().registryKey(key));
			case "fence" -> new FenceBlock(fenceProps().registryKey(key));
			default -> new BaseReinforcedBlock(cubeProps(name).registryKey(key));
		});
		REINFORCED_BLOCKS.add(block);

		if (category.equals("glass"))
			GLASS_BLOCKS.add(block);
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
		ItemGroup group = FabricItemGroup.builder().icon(() -> new ItemStack(KEYPAD)).displayName(Text.translatable("itemGroup.securitycraft.general")).entries((displayContext, entries) -> TAB_ITEMS.forEach(entries::add)).build();
		Registry.register(Registries.ITEM_GROUP, id("general"), group);
	}

	private static AbstractBlock.Settings cubeProps(String name) {
		boolean wood = name.contains("planks") || name.contains("mosaic") || name.contains("bookshelf");
		boolean wool = name.contains("wool");
		AbstractBlock.Settings p = AbstractBlock.Settings.create().strength(wood || wool ? 2.0F : 5.0F, 12000.0F).sounds(wood ? BlockSoundGroup.WOOD : wool ? BlockSoundGroup.WOOL : BlockSoundGroup.STONE);

		if (!wood && !wool)
			p.requiresTool();

		return p;
	}

	private static AbstractBlock.Settings pillarProps(String name) {
		boolean wood = name.contains("log") || name.contains("wood") || name.contains("stem") || name.contains("hyphae") || name.contains("bamboo");
		AbstractBlock.Settings p = AbstractBlock.Settings.create().strength(wood ? 2.0F : 5.0F, 12000.0F).sounds(wood ? BlockSoundGroup.WOOD : BlockSoundGroup.STONE);

		if (!wood)
			p.requiresTool();

		return p;
	}

	private static AbstractBlock.Settings glassProps() {
		return AbstractBlock.Settings.create().strength(1.5F, 12000.0F).sounds(BlockSoundGroup.GLASS).nonOpaque().allowsSpawning((state, world, pos, type) -> false).solidBlock((state, world, pos) -> false).suffocates((state, world, pos) -> false).blockVision((state, world, pos) -> false);
	}

	private static AbstractBlock.Settings paneProps() {
		return AbstractBlock.Settings.create().strength(5.0F, 12000.0F).requiresTool().sounds(BlockSoundGroup.METAL).nonOpaque();
	}

	private static AbstractBlock.Settings fenceProps() {
		return AbstractBlock.Settings.create().strength(2.0F, 12000.0F).sounds(BlockSoundGroup.WOOD);
	}
}
