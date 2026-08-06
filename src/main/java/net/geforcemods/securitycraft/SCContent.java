package net.geforcemods.securitycraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.geforcemods.securitycraft.blockentities.KeypadBlockEntity;
import net.geforcemods.securitycraft.blocks.KeypadBlock;
import net.geforcemods.securitycraft.blocks.reinforced.BaseReinforcedBlock;
import net.geforcemods.securitycraft.items.BlockReinforcerItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

/** Central registration. Keypad + full reinforced block set (all shapes). */
public class SCContent {
	private static final List<ItemLike> TAB_ITEMS = new ArrayList<>();
	public static final List<Block> GLASS_BLOCKS = new ArrayList<>();
	public static final List<Block> REINFORCED_BLOCKS = new ArrayList<>();
	public static final Map<String, Block> REINFORCED_BY_NAME = new HashMap<>();
	public static final List<Block> CUTOUT_BLOCKS = new ArrayList<>();
	public static final List<Block> GLASS_PANE_BLOCKS = new ArrayList<>();

	public static Block KEYPAD;
	public static net.geforcemods.securitycraft.blocks.LaserBlock LASER_BLOCK;
	public static net.geforcemods.securitycraft.blocks.LaserFieldBlock LASER_FIELD;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.LaserBlockBlockEntity> LASER_BLOCK_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.api.OwnableBlockEntity> ABSTRACT_BLOCK_ENTITY;
	public static net.geforcemods.securitycraft.blocks.KeyPanelBlock KEY_PANEL;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.KeyPanelBlockEntity> KEY_PANEL_BLOCK_ENTITY;
	public static Item KEY_PANEL_ITEM;
	public static net.geforcemods.securitycraft.blocks.FrameBlock FRAME;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.FrameBlockEntity> FRAME_BLOCK_ENTITY;
	public static net.geforcemods.securitycraft.items.ModuleItem REDSTONE_MODULE;
	public static net.geforcemods.securitycraft.items.ModuleItem ALLOWLIST_MODULE;
	public static net.geforcemods.securitycraft.items.ModuleItem DENYLIST_MODULE;
	public static net.geforcemods.securitycraft.items.ModuleItem HARMING_MODULE;
	public static net.geforcemods.securitycraft.items.ModuleItem SMART_MODULE;
	public static net.geforcemods.securitycraft.items.ModuleItem STORAGE_MODULE;
	public static net.geforcemods.securitycraft.items.ModuleItem DISGUISE_MODULE;
	public static net.geforcemods.securitycraft.items.ModuleItem SPEED_MODULE;
	public static Item LENS;
	public static Item UNIVERSAL_BLOCK_REINFORCER_LVL1;
	public static Item UNIVERSAL_BLOCK_REINFORCER_LVL2;
	public static Item UNIVERSAL_BLOCK_REINFORCER_LVL3;
	public static Item UNIVERSAL_BLOCK_REMOVER;
	public static BlockEntityType<KeypadBlockEntity> KEYPAD_BLOCK_ENTITY;

	public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("general"));

	public static RecipeSerializer<?> BLOCK_REINFORCING_SERIALIZER;
	public static RecipeSerializer<?> BLOCK_UNREINFORCING_SERIALIZER;
	public static RecipeSerializer<?> LENS_COLORING_SERIALIZER;
	public static net.minecraft.world.inventory.MenuType<net.geforcemods.securitycraft.inventory.BlockReinforcerMenu> BLOCK_REINFORCER_MENU;
	public static net.minecraft.world.inventory.MenuType<net.geforcemods.securitycraft.inventory.LaserBlockMenu> LASER_BLOCK_MENU;
	public static net.minecraft.world.inventory.MenuType<net.geforcemods.securitycraft.inventory.DisguiseModuleMenu> DISGUISE_MODULE_MENU;

	private static final String[][] REINFORCED = {
			{"reinforced_acacia_button", "button"},
			{"reinforced_acacia_fence", "fence"},
			{"reinforced_acacia_fence_gate", "fence_gate"},
			{"reinforced_acacia_log", "pillar"},
			{"reinforced_acacia_planks", "cube"},
			{"reinforced_acacia_pressure_plate", "pressure_plate"},
			{"reinforced_acacia_slab", "slab"},
			{"reinforced_acacia_stairs", "stairs"},
			{"reinforced_acacia_wood", "pillar"},
			{"reinforced_amethyst_block", "cube"},
			{"reinforced_andesite", "cube"},
			{"reinforced_andesite_slab", "slab"},
			{"reinforced_andesite_stairs", "stairs"},
			{"reinforced_andesite_wall", "wall"},
			{"reinforced_bamboo_block", "pillar"},
			{"reinforced_bamboo_button", "button"},
			{"reinforced_bamboo_fence", "fence"},
			{"reinforced_bamboo_fence_gate", "fence_gate"},
			{"reinforced_bamboo_mosaic", "cube"},
			{"reinforced_bamboo_mosaic_slab", "slab"},
			{"reinforced_bamboo_mosaic_stairs", "stairs"},
			{"reinforced_bamboo_planks", "cube"},
			{"reinforced_bamboo_pressure_plate", "pressure_plate"},
			{"reinforced_bamboo_slab", "slab"},
			{"reinforced_bamboo_stairs", "stairs"},
			{"reinforced_basalt", "pillar"},
			{"reinforced_birch_button", "button"},
			{"reinforced_birch_fence", "fence"},
			{"reinforced_birch_fence_gate", "fence_gate"},
			{"reinforced_birch_log", "pillar"},
			{"reinforced_birch_planks", "cube"},
			{"reinforced_birch_pressure_plate", "pressure_plate"},
			{"reinforced_birch_slab", "slab"},
			{"reinforced_birch_stairs", "stairs"},
			{"reinforced_birch_wood", "pillar"},
			{"reinforced_black_concrete", "cube"},
			{"reinforced_black_stained_glass", "glass"},
			{"reinforced_black_stained_glass_pane", "pane"},
			{"reinforced_black_terracotta", "cube"},
			{"reinforced_black_wool", "cube"},
			{"reinforced_blackstone", "cube"},
			{"reinforced_blackstone_slab", "slab"},
			{"reinforced_blackstone_stairs", "stairs"},
			{"reinforced_blackstone_wall", "wall"},
			{"reinforced_blue_concrete", "cube"},
			{"reinforced_blue_ice", "cube"},
			{"reinforced_blue_stained_glass", "glass"},
			{"reinforced_blue_stained_glass_pane", "pane"},
			{"reinforced_blue_terracotta", "cube"},
			{"reinforced_blue_wool", "cube"},
			{"reinforced_bone_block", "pillar"},
			{"reinforced_brick_slab", "slab"},
			{"reinforced_brick_stairs", "stairs"},
			{"reinforced_brick_wall", "wall"},
			{"reinforced_bricks", "cube"},
			{"reinforced_brown_concrete", "cube"},
			{"reinforced_brown_stained_glass", "glass"},
			{"reinforced_brown_stained_glass_pane", "pane"},
			{"reinforced_brown_terracotta", "cube"},
			{"reinforced_brown_wool", "cube"},
			{"reinforced_calcite", "cube"},
			{"reinforced_cherry_button", "button"},
			{"reinforced_cherry_fence", "fence"},
			{"reinforced_cherry_fence_gate", "fence_gate"},
			{"reinforced_cherry_log", "pillar"},
			{"reinforced_cherry_planks", "cube"},
			{"reinforced_cherry_pressure_plate", "pressure_plate"},
			{"reinforced_cherry_slab", "slab"},
			{"reinforced_cherry_stairs", "stairs"},
			{"reinforced_cherry_wood", "pillar"},
			{"reinforced_chiseled_deepslate", "cube"},
			{"reinforced_chiseled_nether_bricks", "cube"},
			{"reinforced_chiseled_polished_blackstone", "cube"},
			{"reinforced_chiseled_quartz_block", "cube"},
			{"reinforced_chiseled_red_sandstone", "cube"},
			{"reinforced_chiseled_sandstone", "cube"},
			{"reinforced_chiseled_stone_bricks", "cube"},
			{"reinforced_clay", "cube"},
			{"reinforced_coal_block", "cube"},
			{"reinforced_coarse_dirt", "cube"},
			{"reinforced_cobbled_deepslate", "cube"},
			{"reinforced_cobbled_deepslate_slab", "slab"},
			{"reinforced_cobbled_deepslate_stairs", "stairs"},
			{"reinforced_cobbled_deepslate_wall", "wall"},
			{"reinforced_cobblestone", "cube"},
			{"reinforced_cobblestone_slab", "slab"},
			{"reinforced_cobblestone_stairs", "stairs"},
			{"reinforced_cobblestone_wall", "wall"},
			{"reinforced_copper_block", "cube"},
			{"reinforced_cracked_deepslate_bricks", "cube"},
			{"reinforced_cracked_deepslate_tiles", "cube"},
			{"reinforced_cracked_nether_bricks", "cube"},
			{"reinforced_cracked_polished_blackstone_bricks", "cube"},
			{"reinforced_cracked_stone_bricks", "cube"},
			{"reinforced_crimson_button", "button"},
			{"reinforced_crimson_fence", "fence"},
			{"reinforced_crimson_fence_gate", "fence_gate"},
			{"reinforced_crimson_hyphae", "pillar"},
			{"reinforced_crimson_nylium", "cube"},
			{"reinforced_crimson_planks", "cube"},
			{"reinforced_crimson_pressure_plate", "pressure_plate"},
			{"reinforced_crimson_slab", "slab"},
			{"reinforced_crimson_stairs", "stairs"},
			{"reinforced_crimson_stem", "pillar"},
			{"reinforced_crying_obsidian", "cube"},
			{"reinforced_crystal_quartz_slab", "slab"},
			{"reinforced_crystal_quartz_stairs", "stairs"},
			{"reinforced_cut_copper", "cube"},
			{"reinforced_cut_copper_slab", "slab"},
			{"reinforced_cut_copper_stairs", "stairs"},
			{"reinforced_cut_red_sandstone", "cube"},
			{"reinforced_cut_red_sandstone_slab", "slab"},
			{"reinforced_cut_sandstone", "cube"},
			{"reinforced_cut_sandstone_slab", "slab"},
			{"reinforced_cyan_concrete", "cube"},
			{"reinforced_cyan_stained_glass", "glass"},
			{"reinforced_cyan_stained_glass_pane", "pane"},
			{"reinforced_cyan_terracotta", "cube"},
			{"reinforced_cyan_wool", "cube"},
			{"reinforced_dark_oak_button", "button"},
			{"reinforced_dark_oak_fence", "fence"},
			{"reinforced_dark_oak_fence_gate", "fence_gate"},
			{"reinforced_dark_oak_log", "pillar"},
			{"reinforced_dark_oak_planks", "cube"},
			{"reinforced_dark_oak_pressure_plate", "pressure_plate"},
			{"reinforced_dark_oak_slab", "slab"},
			{"reinforced_dark_oak_stairs", "stairs"},
			{"reinforced_dark_oak_wood", "pillar"},
			{"reinforced_dark_prismarine", "cube"},
			{"reinforced_dark_prismarine_slab", "slab"},
			{"reinforced_dark_prismarine_stairs", "stairs"},
			{"reinforced_deepslate", "pillar"},
			{"reinforced_deepslate_brick_slab", "slab"},
			{"reinforced_deepslate_brick_stairs", "stairs"},
			{"reinforced_deepslate_brick_wall", "wall"},
			{"reinforced_deepslate_bricks", "cube"},
			{"reinforced_deepslate_tile_slab", "slab"},
			{"reinforced_deepslate_tile_stairs", "stairs"},
			{"reinforced_deepslate_tile_wall", "wall"},
			{"reinforced_deepslate_tiles", "cube"},
			{"reinforced_diamond_block", "cube"},
			{"reinforced_diorite", "cube"},
			{"reinforced_diorite_slab", "slab"},
			{"reinforced_diorite_stairs", "stairs"},
			{"reinforced_diorite_wall", "wall"},
			{"reinforced_dirt", "cube"},
			{"reinforced_dripstone_block", "cube"},
			{"reinforced_emerald_block", "cube"},
			{"reinforced_end_stone", "cube"},
			{"reinforced_end_stone_brick_slab", "slab"},
			{"reinforced_end_stone_brick_stairs", "stairs"},
			{"reinforced_end_stone_brick_wall", "wall"},
			{"reinforced_end_stone_bricks", "cube"},
			{"reinforced_exposed_copper", "cube"},
			{"reinforced_exposed_cut_copper", "cube"},
			{"reinforced_exposed_cut_copper_slab", "slab"},
			{"reinforced_exposed_cut_copper_stairs", "stairs"},
			{"reinforced_fence_gate", "fence_gate"},
			{"reinforced_glass", "glass"},
			{"reinforced_glass_pane", "pane"},
			{"reinforced_glowstone", "cube"},
			{"reinforced_gold_block", "cube"},
			{"reinforced_granite", "cube"},
			{"reinforced_granite_slab", "slab"},
			{"reinforced_granite_stairs", "stairs"},
			{"reinforced_granite_wall", "wall"},
			{"reinforced_grass_path", "cube"},
			{"reinforced_gravel", "cube"},
			{"reinforced_gray_concrete", "cube"},
			{"reinforced_gray_stained_glass", "glass"},
			{"reinforced_gray_stained_glass_pane", "pane"},
			{"reinforced_gray_terracotta", "cube"},
			{"reinforced_gray_wool", "cube"},
			{"reinforced_green_concrete", "cube"},
			{"reinforced_green_stained_glass", "glass"},
			{"reinforced_green_stained_glass_pane", "pane"},
			{"reinforced_green_terracotta", "cube"},
			{"reinforced_green_wool", "cube"},
			{"reinforced_hardened_clay", "cube"},
			{"reinforced_ice", "cube"},
			{"reinforced_iron_bars", "pane"},
			{"reinforced_iron_block", "cube"},
			{"reinforced_iron_trapdoor", "trapdoor"},
			{"reinforced_jungle_button", "button"},
			{"reinforced_jungle_fence", "fence"},
			{"reinforced_jungle_fence_gate", "fence_gate"},
			{"reinforced_jungle_log", "pillar"},
			{"reinforced_jungle_planks", "cube"},
			{"reinforced_jungle_pressure_plate", "pressure_plate"},
			{"reinforced_jungle_slab", "slab"},
			{"reinforced_jungle_stairs", "stairs"},
			{"reinforced_jungle_wood", "pillar"},
			{"reinforced_lapis_block", "cube"},
			{"reinforced_light_blue_concrete", "cube"},
			{"reinforced_light_blue_stained_glass", "glass"},
			{"reinforced_light_blue_stained_glass_pane", "pane"},
			{"reinforced_light_blue_terracotta", "cube"},
			{"reinforced_light_blue_wool", "cube"},
			{"reinforced_light_gray_concrete", "cube"},
			{"reinforced_light_gray_stained_glass", "glass"},
			{"reinforced_light_gray_stained_glass_pane", "pane"},
			{"reinforced_light_gray_terracotta", "cube"},
			{"reinforced_light_gray_wool", "cube"},
			{"reinforced_lime_concrete", "cube"},
			{"reinforced_lime_stained_glass", "glass"},
			{"reinforced_lime_stained_glass_pane", "pane"},
			{"reinforced_lime_terracotta", "cube"},
			{"reinforced_lime_wool", "cube"},
			{"reinforced_magenta_concrete", "cube"},
			{"reinforced_magenta_stained_glass", "glass"},
			{"reinforced_magenta_stained_glass_pane", "pane"},
			{"reinforced_magenta_terracotta", "cube"},
			{"reinforced_magenta_wool", "cube"},
			{"reinforced_magma_block", "cube"},
			{"reinforced_mangrove_button", "button"},
			{"reinforced_mangrove_fence", "fence"},
			{"reinforced_mangrove_fence_gate", "fence_gate"},
			{"reinforced_mangrove_log", "pillar"},
			{"reinforced_mangrove_planks", "cube"},
			{"reinforced_mangrove_pressure_plate", "pressure_plate"},
			{"reinforced_mangrove_slab", "slab"},
			{"reinforced_mangrove_stairs", "stairs"},
			{"reinforced_mangrove_wood", "pillar"},
			{"reinforced_moss_block", "cube"},
			{"reinforced_mossy_cobblestone", "cube"},
			{"reinforced_mossy_cobblestone_slab", "slab"},
			{"reinforced_mossy_cobblestone_stairs", "stairs"},
			{"reinforced_mossy_cobblestone_wall", "wall"},
			{"reinforced_mossy_stone_brick_slab", "slab"},
			{"reinforced_mossy_stone_brick_stairs", "stairs"},
			{"reinforced_mossy_stone_brick_wall", "wall"},
			{"reinforced_mossy_stone_bricks", "cube"},
			{"reinforced_mud", "cube"},
			{"reinforced_mud_brick_slab", "slab"},
			{"reinforced_mud_brick_stairs", "stairs"},
			{"reinforced_mud_brick_wall", "wall"},
			{"reinforced_mud_bricks", "cube"},
			{"reinforced_nether_brick_fence", "fence"},
			{"reinforced_nether_brick_slab", "slab"},
			{"reinforced_nether_brick_stairs", "stairs"},
			{"reinforced_nether_brick_wall", "wall"},
			{"reinforced_nether_bricks", "cube"},
			{"reinforced_nether_wart_block", "cube"},
			{"reinforced_netherite_block", "cube"},
			{"reinforced_netherrack", "cube"},
			{"reinforced_normal_stone_slab", "slab"},
			{"reinforced_oak_button", "button"},
			{"reinforced_oak_fence", "fence"},
			{"reinforced_oak_fence_gate", "fence_gate"},
			{"reinforced_oak_log", "pillar"},
			{"reinforced_oak_planks", "cube"},
			{"reinforced_oak_pressure_plate", "pressure_plate"},
			{"reinforced_oak_slab", "slab"},
			{"reinforced_oak_stairs", "stairs"},
			{"reinforced_oak_wood", "pillar"},
			{"reinforced_obsidian", "cube"},
			{"reinforced_ochre_froglight", "pillar"},
			{"reinforced_orange_concrete", "cube"},
			{"reinforced_orange_stained_glass", "glass"},
			{"reinforced_orange_stained_glass_pane", "pane"},
			{"reinforced_orange_terracotta", "cube"},
			{"reinforced_orange_wool", "cube"},
			{"reinforced_oxidized_copper", "cube"},
			{"reinforced_oxidized_cut_copper", "cube"},
			{"reinforced_oxidized_cut_copper_slab", "slab"},
			{"reinforced_oxidized_cut_copper_stairs", "stairs"},
			{"reinforced_packed_ice", "cube"},
			{"reinforced_packed_mud", "cube"},
			{"reinforced_pearlescent_froglight", "pillar"},
			{"reinforced_pink_concrete", "cube"},
			{"reinforced_pink_stained_glass", "glass"},
			{"reinforced_pink_stained_glass_pane", "pane"},
			{"reinforced_pink_terracotta", "cube"},
			{"reinforced_pink_wool", "cube"},
			{"reinforced_polished_andesite", "cube"},
			{"reinforced_polished_andesite_slab", "slab"},
			{"reinforced_polished_andesite_stairs", "stairs"},
			{"reinforced_polished_basalt", "pillar"},
			{"reinforced_polished_blackstone", "cube"},
			{"reinforced_polished_blackstone_brick_slab", "slab"},
			{"reinforced_polished_blackstone_brick_stairs", "stairs"},
			{"reinforced_polished_blackstone_brick_wall", "wall"},
			{"reinforced_polished_blackstone_bricks", "cube"},
			{"reinforced_polished_blackstone_button", "button"},
			{"reinforced_polished_blackstone_pressure_plate", "pressure_plate"},
			{"reinforced_polished_blackstone_slab", "slab"},
			{"reinforced_polished_blackstone_stairs", "stairs"},
			{"reinforced_polished_blackstone_wall", "wall"},
			{"reinforced_polished_deepslate", "cube"},
			{"reinforced_polished_deepslate_slab", "slab"},
			{"reinforced_polished_deepslate_stairs", "stairs"},
			{"reinforced_polished_deepslate_wall", "wall"},
			{"reinforced_polished_diorite", "cube"},
			{"reinforced_polished_diorite_slab", "slab"},
			{"reinforced_polished_diorite_stairs", "stairs"},
			{"reinforced_polished_granite", "cube"},
			{"reinforced_polished_granite_slab", "slab"},
			{"reinforced_polished_granite_stairs", "stairs"},
			{"reinforced_prismarine", "cube"},
			{"reinforced_prismarine_brick_slab", "slab"},
			{"reinforced_prismarine_brick_stairs", "stairs"},
			{"reinforced_prismarine_bricks", "cube"},
			{"reinforced_prismarine_slab", "slab"},
			{"reinforced_prismarine_stairs", "stairs"},
			{"reinforced_prismarine_wall", "wall"},
			{"reinforced_purple_concrete", "cube"},
			{"reinforced_purple_stained_glass", "glass"},
			{"reinforced_purple_stained_glass_pane", "pane"},
			{"reinforced_purple_terracotta", "cube"},
			{"reinforced_purple_wool", "cube"},
			{"reinforced_purpur_block", "cube"},
			{"reinforced_purpur_pillar", "pillar"},
			{"reinforced_purpur_slab", "slab"},
			{"reinforced_purpur_stairs", "stairs"},
			{"reinforced_quartz_block", "cube"},
			{"reinforced_quartz_bricks", "cube"},
			{"reinforced_quartz_pillar", "pillar"},
			{"reinforced_quartz_slab", "slab"},
			{"reinforced_quartz_stairs", "stairs"},
			{"reinforced_raw_copper_block", "cube"},
			{"reinforced_raw_gold_block", "cube"},
			{"reinforced_raw_iron_block", "cube"},
			{"reinforced_red_concrete", "cube"},
			{"reinforced_red_nether_brick_slab", "slab"},
			{"reinforced_red_nether_brick_stairs", "stairs"},
			{"reinforced_red_nether_brick_wall", "wall"},
			{"reinforced_red_nether_bricks", "cube"},
			{"reinforced_red_sand", "cube"},
			{"reinforced_red_sandstone", "cube"},
			{"reinforced_red_sandstone_slab", "slab"},
			{"reinforced_red_sandstone_stairs", "stairs"},
			{"reinforced_red_sandstone_wall", "wall"},
			{"reinforced_red_stained_glass", "glass"},
			{"reinforced_red_stained_glass_pane", "pane"},
			{"reinforced_red_terracotta", "cube"},
			{"reinforced_red_wool", "cube"},
			{"reinforced_redstone_block", "cube"},
			{"reinforced_rooted_dirt", "cube"},
			{"reinforced_sand", "cube"},
			{"reinforced_sandstone", "cube"},
			{"reinforced_sandstone_slab", "slab"},
			{"reinforced_sandstone_stairs", "stairs"},
			{"reinforced_sandstone_wall", "wall"},
			{"reinforced_shroomlight", "cube"},
			{"reinforced_smooth_basalt", "cube"},
			{"reinforced_smooth_crystal_quartz_slab", "slab"},
			{"reinforced_smooth_crystal_quartz_stairs", "stairs"},
			{"reinforced_smooth_quartz", "cube"},
			{"reinforced_smooth_quartz_slab", "slab"},
			{"reinforced_smooth_quartz_stairs", "stairs"},
			{"reinforced_smooth_red_sandstone", "cube"},
			{"reinforced_smooth_red_sandstone_slab", "slab"},
			{"reinforced_smooth_red_sandstone_stairs", "stairs"},
			{"reinforced_smooth_sandstone", "cube"},
			{"reinforced_smooth_sandstone_slab", "slab"},
			{"reinforced_smooth_sandstone_stairs", "stairs"},
			{"reinforced_smooth_stone", "cube"},
			{"reinforced_snow_block", "cube"},
			{"reinforced_soul_sand", "cube"},
			{"reinforced_soul_soil", "cube"},
			{"reinforced_spruce_button", "button"},
			{"reinforced_spruce_fence", "fence"},
			{"reinforced_spruce_fence_gate", "fence_gate"},
			{"reinforced_spruce_log", "pillar"},
			{"reinforced_spruce_planks", "cube"},
			{"reinforced_spruce_pressure_plate", "pressure_plate"},
			{"reinforced_spruce_slab", "slab"},
			{"reinforced_spruce_stairs", "stairs"},
			{"reinforced_spruce_wood", "pillar"},
			{"reinforced_stone", "cube"},
			{"reinforced_stone_brick_slab", "slab"},
			{"reinforced_stone_brick_stairs", "stairs"},
			{"reinforced_stone_brick_wall", "wall"},
			{"reinforced_stone_bricks", "cube"},
			{"reinforced_stone_button", "button"},
			{"reinforced_stone_pressure_plate", "pressure_plate"},
			{"reinforced_stone_slab", "slab"},
			{"reinforced_stone_stairs", "stairs"},
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
			{"reinforced_tuff_slab", "slab"},
			{"reinforced_tuff_stairs", "stairs"},
			{"reinforced_tuff_wall", "wall"},
			{"reinforced_verdant_froglight", "pillar"},
			{"reinforced_warped_button", "button"},
			{"reinforced_warped_fence", "fence"},
			{"reinforced_warped_fence_gate", "fence_gate"},
			{"reinforced_warped_hyphae", "pillar"},
			{"reinforced_warped_nylium", "cube"},
			{"reinforced_warped_planks", "cube"},
			{"reinforced_warped_pressure_plate", "pressure_plate"},
			{"reinforced_warped_slab", "slab"},
			{"reinforced_warped_stairs", "stairs"},
			{"reinforced_warped_stem", "pillar"},
			{"reinforced_warped_wart_block", "cube"},
			{"reinforced_weathered_copper", "cube"},
			{"reinforced_weathered_cut_copper", "cube"},
			{"reinforced_weathered_cut_copper_slab", "slab"},
			{"reinforced_weathered_cut_copper_stairs", "stairs"},
			{"reinforced_white_concrete", "cube"},
			{"reinforced_white_stained_glass", "glass"},
			{"reinforced_white_stained_glass_pane", "pane"},
			{"reinforced_white_terracotta", "cube"},
			{"reinforced_white_wool", "cube"},
			{"reinforced_yellow_concrete", "cube"},
			{"reinforced_yellow_stained_glass", "glass"},
			{"reinforced_yellow_stained_glass_pane", "pane"},
			{"reinforced_yellow_terracotta", "cube"},
			{"reinforced_yellow_wool", "cube"}
	};

	public static ResourceLocation id(String name) {
		return new ResourceLocation(SecurityCraft.MODID, name);
	}

	public static void init() {
		KEYPAD = register("keypad", new KeypadBlock(BlockBehaviour.Properties.of().strength(2.0F, 12000.0F).requiresCorrectToolForDrops()));
		LASER_BLOCK = (net.geforcemods.securitycraft.blocks.LaserBlock) register("laser_block", new net.geforcemods.securitycraft.blocks.LaserBlock(BlockBehaviour.Properties.of().strength(3.5F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
		LASER_FIELD = (net.geforcemods.securitycraft.blocks.LaserFieldBlock) registerBlockNoItem("laser", new net.geforcemods.securitycraft.blocks.LaserFieldBlock(BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.NONE).strength(-1.0F).noLootTable().noOcclusion()));
		KEY_PANEL = (net.geforcemods.securitycraft.blocks.KeyPanelBlock) registerBlockNoItem("key_panel", new net.geforcemods.securitycraft.blocks.KeyPanelBlock(BlockBehaviour.Properties.of().strength(3.5F).sound(SoundType.METAL).noOcclusion()));
		KEY_PANEL_ITEM = registerItem("keypad_item", new net.geforcemods.securitycraft.items.KeyPanelItem(new Item.Properties()));
		FRAME = (net.geforcemods.securitycraft.blocks.FrameBlock) register("keypad_frame", new net.geforcemods.securitycraft.blocks.FrameBlock(BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(5.0F).sound(SoundType.METAL).noOcclusion()));
		net.geforcemods.securitycraft.api.SecurityCraftAPI.registerPasscodeConvertible(new net.geforcemods.securitycraft.blocks.KeypadBlock.Convertible());

		for (String[] entry : REINFORCED)
			registerReinforced(entry[0], entry[1]);

		REDSTONE_MODULE = registerModule("redstone_module", net.geforcemods.securitycraft.misc.ModuleType.REDSTONE, false, false, false);
		ALLOWLIST_MODULE = registerModule("whitelist_module", net.geforcemods.securitycraft.misc.ModuleType.ALLOWLIST, true, true, true);
		DENYLIST_MODULE = registerModule("blacklist_module", net.geforcemods.securitycraft.misc.ModuleType.DENYLIST, true, true, true);
		HARMING_MODULE = registerModule("harming_module", net.geforcemods.securitycraft.misc.ModuleType.HARMING, false, false, false);
		SMART_MODULE = registerModule("smart_module", net.geforcemods.securitycraft.misc.ModuleType.SMART, false, false, false);
		STORAGE_MODULE = registerModule("storage_module", net.geforcemods.securitycraft.misc.ModuleType.STORAGE, false, false, false);
		DISGUISE_MODULE = registerModule("disguise_module", net.geforcemods.securitycraft.misc.ModuleType.DISGUISE, false, true, false);
		SPEED_MODULE = registerModule("speed_module", net.geforcemods.securitycraft.misc.ModuleType.SPEED, false, false, false);
		LENS = registerItem("lens", new Item(new Item.Properties()));

		UNIVERSAL_BLOCK_REINFORCER_LVL1 = registerConverterItem("universal_block_reinforcer_lvl1", 300, true);
		UNIVERSAL_BLOCK_REINFORCER_LVL2 = registerConverterItem("universal_block_reinforcer_lvl2", 2700, true);
		UNIVERSAL_BLOCK_REINFORCER_LVL3 = registerConverterItem("universal_block_reinforcer_lvl3", 0, true);
		UNIVERSAL_BLOCK_REMOVER = registerConverterItem("universal_block_remover", 476, false);

		BLOCK_REINFORCING_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("block_reinforcing"), new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(net.geforcemods.securitycraft.recipe.ReinforcerRecipe.Reinforcing::new));
		BLOCK_UNREINFORCING_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("block_unreinforcing"), new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(net.geforcemods.securitycraft.recipe.ReinforcerRecipe.Unreinforcing::new));
		LENS_COLORING_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("lens_coloring"), new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(net.geforcemods.securitycraft.recipe.LensColoringRecipe::new));
		BLOCK_REINFORCER_MENU = Registry.register(BuiltInRegistries.MENU, id("block_reinforcer"), new net.minecraft.world.inventory.MenuType<>(net.geforcemods.securitycraft.inventory.BlockReinforcerMenu::new, net.minecraft.world.flag.FeatureFlags.VANILLA_SET));

		KEYPAD_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("keypad"), FabricBlockEntityTypeBuilder.create(KeypadBlockEntity::new, KEYPAD).build());
		LASER_BLOCK_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("laser_block"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.LaserBlockBlockEntity::new, LASER_BLOCK).build());
		ABSTRACT_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("abstract"), FabricBlockEntityTypeBuilder.create((pos, state) -> new net.geforcemods.securitycraft.api.OwnableBlockEntity(ABSTRACT_BLOCK_ENTITY, pos, state), LASER_FIELD).build());
		KEY_PANEL_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("key_panel"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.KeyPanelBlockEntity::new, KEY_PANEL).build());
		FRAME_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("keypad_frame"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.FrameBlockEntity::new, FRAME).build());
		LASER_BLOCK_MENU = Registry.register(BuiltInRegistries.MENU, id("laser_block"), new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>((syncId, inv, buf) -> new net.geforcemods.securitycraft.inventory.LaserBlockMenu(syncId, inv, buf)));
		DISGUISE_MODULE_MENU = Registry.register(BuiltInRegistries.MENU, id("disguise_module"), new net.minecraft.world.inventory.MenuType<>(net.geforcemods.securitycraft.inventory.DisguiseModuleMenu::new, net.minecraft.world.flag.FeatureFlags.VANILLA_SET));
		net.geforcemods.securitycraft.misc.SCSounds.register();
		registerCreativeTab();
	}

	private static Block registerBlockNoItem(String name, Block block) {
		Registry.register(BuiltInRegistries.BLOCK, id(name), block);
		return block;
	}

	private static boolean isGlass(String name, String category) {
		return category.equals("glass") || (category.equals("pane") && name.contains("glass"));
	}

	private static boolean isCutout(String name, String category) {
		return category.equals("trapdoor") || name.equals("reinforced_iron_bars");
	}

	private static void registerReinforced(String name, String category) {
		Block block = switch (category) {
			case "pillar" -> new RotatedPillarBlock(shapeProps(name));
			case "glass" -> new BaseReinforcedBlock(glassProps());
			case "pane" -> new IronBarsBlock((name.contains("glass") ? glassProps() : paneProps()));
			case "fence" -> new FenceBlock(shapeProps(name));
			case "fence_gate" -> new FenceGateBlock(shapeProps(name), WoodType.OAK);
			case "wall" -> new WallBlock(shapeProps(name));
			case "slab" -> new SlabBlock(shapeProps(name));
			case "stairs" -> new StairBlock(Blocks.STONE.defaultBlockState(), shapeProps(name));
			case "button" -> new ButtonBlock(shapeProps(name), BlockSetType.STONE, 20, false);
			case "pressure_plate" -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, shapeProps(name), BlockSetType.STONE);
			case "trapdoor" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedTrapdoorBlock(BlockSetType.IRON, shapeProps(name));
			default -> new BaseReinforcedBlock(shapeProps(name));
		};
		register(name, block);
		REINFORCED_BLOCKS.add(block);
		REINFORCED_BY_NAME.put(name, block);

		if (isGlass(name, category))
			GLASS_BLOCKS.add(block);

		if (isCutout(name, category))
			CUTOUT_BLOCKS.add(block);

		if (category.equals("pane") && name.contains("glass"))
			GLASS_PANE_BLOCKS.add(block);
	}

	private static Block register(String name, Block block) {
		Registry.register(BuiltInRegistries.BLOCK, id(name), block);
		BlockItem item = new BlockItem(block, new Item.Properties());
		Registry.register(BuiltInRegistries.ITEM, id(name), item);
		TAB_ITEMS.add(item);
		return block;
	}

	private static void registerCreativeTab() {
		CreativeModeTab technical = FabricItemGroup.builder()
				.icon(() -> new ItemStack(KEYPAD))
				.title(Component.translatable("itemGroup.securitycraft.technical"))
				.displayItems((params, output) -> {
					output.accept(KEYPAD);
					output.accept(KEY_PANEL_ITEM);
					output.accept(FRAME);
					output.accept(LASER_BLOCK);
					output.accept(REDSTONE_MODULE);
					output.accept(ALLOWLIST_MODULE);
					output.accept(DENYLIST_MODULE);
					output.accept(HARMING_MODULE);
					output.accept(SMART_MODULE);
					output.accept(STORAGE_MODULE);
					output.accept(DISGUISE_MODULE);
					output.accept(SPEED_MODULE);
				})
				.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("technical"), technical);

		CreativeModeTab decoration = FabricItemGroup.builder()
				.icon(() -> new ItemStack(REINFORCED_BY_NAME.getOrDefault("reinforced_oak_stairs", KEYPAD)))
				.title(Component.translatable("itemGroup.securitycraft.decoration"))
				.displayItems((params, output) -> {
					output.accept(UNIVERSAL_BLOCK_REINFORCER_LVL1);
					output.accept(UNIVERSAL_BLOCK_REINFORCER_LVL2);
					output.accept(UNIVERSAL_BLOCK_REINFORCER_LVL3);
					output.accept(UNIVERSAL_BLOCK_REMOVER);

					for (Block block : sortByVanillaOrder(REINFORCED_BLOCKS))
						output.accept(block);
				})
				.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("decoration"), decoration);
	}

	private static List<Block> sortByVanillaOrder(List<Block> blocks) {
		List<Item> vanillaOrder = new ArrayList<>();

		for (ResourceKey<CreativeModeTab> key : List.of(CreativeModeTabs.BUILDING_BLOCKS, CreativeModeTabs.COLORED_BLOCKS, CreativeModeTabs.NATURAL_BLOCKS, CreativeModeTabs.FUNCTIONAL_BLOCKS, CreativeModeTabs.REDSTONE_BLOCKS)) {
			CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(key);

			if (tab != null)
				for (ItemStack stack : tab.getDisplayItems())
					vanillaOrder.add(stack.getItem());
		}

		java.util.Map<Item, Integer> index = new HashMap<>();

		for (int i = 0; i < vanillaOrder.size(); i++)
			index.putIfAbsent(vanillaOrder.get(i), i);

		List<Block> sorted = new ArrayList<>(blocks);

		sorted.sort(java.util.Comparator.<Block>comparingInt(block -> {
			Block vanilla = vanillaCounterpart(block);
			Integer i = vanilla == null ? null : index.get(vanilla.asItem());
			return i == null ? Integer.MAX_VALUE : i.intValue();
		}));
		return sorted;
	}

	public static Block vanillaCounterpart(Block reinforced) {
		net.minecraft.resources.ResourceLocation loc = BuiltInRegistries.BLOCK.getKey(reinforced);

		if (!loc.getNamespace().equals(SecurityCraft.MODID) || !loc.getPath().startsWith("reinforced_"))
			return null;

		String path = loc.getPath();
		Block vanilla = BuiltInRegistries.BLOCK.get(new ResourceLocation("minecraft", path.substring("reinforced_".length())));

		return vanilla == Blocks.AIR ? null : vanilla;
	}

	public static Block reinforcedCounterpart(Block vanilla) {
		net.minecraft.resources.ResourceLocation loc = BuiltInRegistries.BLOCK.getKey(vanilla);

		if (!loc.getNamespace().equals("minecraft"))
			return null;

		return REINFORCED_BY_NAME.get("reinforced_" + loc.getPath());
	}

	private static Item registerItem(String name, Item item) {
		Registry.register(BuiltInRegistries.ITEM, id(name), item);
		return item;
	}

	private static net.geforcemods.securitycraft.items.ModuleItem registerModule(String name, net.geforcemods.securitycraft.misc.ModuleType type, boolean containsCustomData, boolean canBeCustomized, boolean hasListData) {
		Item.Properties props = new Item.Properties().stacksTo(1);
		net.geforcemods.securitycraft.items.ModuleItem item = new net.geforcemods.securitycraft.items.ModuleItem(props, type, containsCustomData, canBeCustomized);

		Registry.register(BuiltInRegistries.ITEM, id(name), item);
		TAB_ITEMS.add(item);
		return item;
	}

	private static Item registerConverterItem(String name, int durability, boolean reinforcing) {
		Item.Properties props = new Item.Properties();

		if (durability > 0)
			props.durability(durability);

		Item item = new BlockReinforcerItem(props, reinforcing);
		Registry.register(BuiltInRegistries.ITEM, id(name), item);
		return item;
	}


	private static boolean isWood(String name) {
		return name.matches(".*(oak|spruce|birch|jungle|acacia|mangrove|cherry|bamboo|crimson|warped|planks|mosaic).*");
	}

	private static BlockBehaviour.Properties shapeProps(String name) {
		boolean wood = isWood(name);
		boolean wool = name.contains("wool");
		BlockBehaviour.Properties p = BlockBehaviour.Properties.of().strength(wood || wool ? 2.0F : 5.0F, 12000.0F).sound(wood ? SoundType.WOOD : wool ? SoundType.WOOL : SoundType.STONE);

		if (!wood && !wool)
			p.requiresCorrectToolForDrops();

		return p;
	}

	private static BlockBehaviour.Properties glassProps() {
		return BlockBehaviour.Properties.of().strength(1.5F, 12000.0F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((state, level, pos, type) -> false).isRedstoneConductor((state, level, pos) -> false).isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false);
	}

	private static BlockBehaviour.Properties paneProps() {
		return BlockBehaviour.Properties.of().strength(5.0F, 12000.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion();
	}
}
