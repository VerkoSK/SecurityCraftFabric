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
	public static Block REINFORCED_DOOR;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.ReinforcedDoorBlockEntity> REINFORCED_DOOR_BLOCK_ENTITY;
	public static net.geforcemods.securitycraft.blocks.ElectrifiedIronFenceBlock ELECTRIFIED_IRON_FENCE;
	public static net.geforcemods.securitycraft.blocks.ElectrifiedIronFenceGateBlock ELECTRIFIED_IRON_FENCE_GATE;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.ElectrifiedFenceAndGateBlockEntity> ELECTRIFIED_FENCE_AND_GATE_BLOCK_ENTITY;
	public static Item UNIVERSAL_OWNER_CHANGER;
	public static Item SC_MANUAL;
	/** The secret sign items, in registration order, for the creative tab. */
	public static final List<ItemLike> SECRET_SIGN_ITEMS = new ArrayList<>();
	public static Block SECRET_OAK_SIGN;
	public static Block SECRET_OAK_WALL_SIGN;
	public static Block SECRET_OAK_HANGING_SIGN;
	public static Block SECRET_OAK_WALL_HANGING_SIGN;
	public static Block SECRET_SPRUCE_SIGN;
	public static Block SECRET_SPRUCE_WALL_SIGN;
	public static Block SECRET_SPRUCE_HANGING_SIGN;
	public static Block SECRET_SPRUCE_WALL_HANGING_SIGN;
	public static Block SECRET_BIRCH_SIGN;
	public static Block SECRET_BIRCH_WALL_SIGN;
	public static Block SECRET_BIRCH_HANGING_SIGN;
	public static Block SECRET_BIRCH_WALL_HANGING_SIGN;
	public static Block SECRET_JUNGLE_SIGN;
	public static Block SECRET_JUNGLE_WALL_SIGN;
	public static Block SECRET_JUNGLE_HANGING_SIGN;
	public static Block SECRET_JUNGLE_WALL_HANGING_SIGN;
	public static Block SECRET_ACACIA_SIGN;
	public static Block SECRET_ACACIA_WALL_SIGN;
	public static Block SECRET_ACACIA_HANGING_SIGN;
	public static Block SECRET_ACACIA_WALL_HANGING_SIGN;
	public static Block SECRET_DARK_OAK_SIGN;
	public static Block SECRET_DARK_OAK_WALL_SIGN;
	public static Block SECRET_DARK_OAK_HANGING_SIGN;
	public static Block SECRET_DARK_OAK_WALL_HANGING_SIGN;
	public static Block SECRET_MANGROVE_SIGN;
	public static Block SECRET_MANGROVE_WALL_SIGN;
	public static Block SECRET_MANGROVE_HANGING_SIGN;
	public static Block SECRET_MANGROVE_WALL_HANGING_SIGN;
	public static Block SECRET_CHERRY_SIGN;
	public static Block SECRET_CHERRY_WALL_SIGN;
	public static Block SECRET_CHERRY_HANGING_SIGN;
	public static Block SECRET_CHERRY_WALL_HANGING_SIGN;
	public static Block SECRET_BAMBOO_SIGN;
	public static Block SECRET_BAMBOO_WALL_SIGN;
	public static Block SECRET_BAMBOO_HANGING_SIGN;
	public static Block SECRET_BAMBOO_WALL_HANGING_SIGN;
	public static Block SECRET_CRIMSON_SIGN;
	public static Block SECRET_CRIMSON_WALL_SIGN;
	public static Block SECRET_CRIMSON_HANGING_SIGN;
	public static Block SECRET_CRIMSON_WALL_HANGING_SIGN;
	public static Block SECRET_WARPED_SIGN;
	public static Block SECRET_WARPED_WALL_SIGN;
	public static Block SECRET_WARPED_HANGING_SIGN;
	public static Block SECRET_WARPED_WALL_HANGING_SIGN;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.SecretSignBlockEntity> SECRET_SIGN_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.SecretHangingSignBlockEntity> SECRET_HANGING_SIGN_BLOCK_ENTITY;
	public static Item CRYSTAL_QUARTZ_ITEM;
	public static Block CRYSTAL_QUARTZ;
	public static Block SMOOTH_CRYSTAL_QUARTZ;
	public static Block CHISELED_CRYSTAL_QUARTZ;
	public static Block CRYSTAL_QUARTZ_BRICKS;
	public static Block CRYSTAL_QUARTZ_PILLAR;
	public static Block CRYSTAL_QUARTZ_STAIRS;
	public static Block CRYSTAL_QUARTZ_SLAB;
	public static Block SMOOTH_CRYSTAL_QUARTZ_STAIRS;
	public static Block SMOOTH_CRYSTAL_QUARTZ_SLAB;
	public static net.minecraft.world.level.material.FlowingFluid FAKE_WATER;
	public static net.minecraft.world.level.material.FlowingFluid FLOWING_FAKE_WATER;
	public static net.minecraft.world.level.material.FlowingFluid FAKE_LAVA;
	public static net.minecraft.world.level.material.FlowingFluid FLOWING_FAKE_LAVA;
	public static Block FAKE_WATER_BLOCK;
	public static Block FAKE_LAVA_BLOCK;
	public static Item FAKE_WATER_BUCKET;
	public static Item FAKE_LAVA_BUCKET;
	public static Item UNIVERSAL_BLOCK_MODIFIER;
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
	public static net.minecraft.core.component.DataComponentType<net.geforcemods.securitycraft.components.ListModuleData> LIST_MODULE_DATA;
	public static Item LENS;
	public static Item UNIVERSAL_BLOCK_REINFORCER_LVL1;
	public static Item UNIVERSAL_BLOCK_REINFORCER_LVL2;
	public static Item UNIVERSAL_BLOCK_REINFORCER_LVL3;
	public static Item UNIVERSAL_BLOCK_REMOVER;
	public static BlockEntityType<KeypadBlockEntity> KEYPAD_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.ReinforcedHopperBlockEntity> REINFORCED_HOPPER_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.ReinforcedDispenserBlockEntity> REINFORCED_DISPENSER_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.ReinforcedDropperBlockEntity> REINFORCED_DROPPER_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.ReinforcedObserverBlockEntity> REINFORCED_OBSERVER_BLOCK_ENTITY;
	public static Block KEYPAD_CHEST;
	public static Block KEYPAD_BARREL;
	public static Block KEYPAD_FURNACE;
	public static Block KEYPAD_SMOKER;
	public static Block KEYPAD_BLAST_FURNACE;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.KeypadChestBlockEntity> KEYPAD_CHEST_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.KeypadBarrelBlockEntity> KEYPAD_BARREL_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.KeypadFurnaceBlockEntity> KEYPAD_FURNACE_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.KeypadSmokerBlockEntity> KEYPAD_SMOKER_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.KeypadBlastFurnaceBlockEntity> KEYPAD_BLAST_FURNACE_BLOCK_ENTITY;
	public static net.minecraft.world.inventory.MenuType<net.geforcemods.securitycraft.inventory.KeypadFurnaceMenu> KEYPAD_FURNACE_MENU;
	public static net.minecraft.world.inventory.MenuType<net.geforcemods.securitycraft.inventory.KeypadSmokerMenu> KEYPAD_SMOKER_MENU;
	public static net.minecraft.world.inventory.MenuType<net.geforcemods.securitycraft.inventory.KeypadBlastFurnaceMenu> KEYPAD_BLAST_FURNACE_MENU;

	public static net.geforcemods.securitycraft.blocks.PortableRadarBlock PORTABLE_RADAR;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.PortableRadarBlockEntity> PORTABLE_RADAR_BLOCK_ENTITY;

	//explosives
	public static Block MINE;
	public static Block BOUNCING_BETTY;
	public static Block CLAYMORE;
	public static Block IMS;
	public static Block TRACK_MINE;
	public static Block STONE_MINE;
	public static Block DEEPSLATE_MINE;
	public static Block COBBLED_DEEPSLATE_MINE;
	public static Block DIRT_MINE;
	public static Block COBBLESTONE_MINE;
	public static Block SAND_MINE;
	public static Block GRAVEL_MINE;
	public static Block NETHERRACK_MINE;
	public static Block END_STONE_MINE;
	public static Block COAL_MINE;
	public static Block DEEPSLATE_COAL_MINE;
	public static Block IRON_MINE;
	public static Block DEEPSLATE_IRON_MINE;
	public static Block GOLD_MINE;
	public static Block DEEPSLATE_GOLD_MINE;
	public static Block COPPER_MINE;
	public static Block DEEPSLATE_COPPER_MINE;
	public static Block REDSTONE_MINE;
	public static Block DEEPSLATE_REDSTONE_MINE;
	public static Block EMERALD_MINE;
	public static Block DEEPSLATE_EMERALD_MINE;
	public static Block LAPIS_MINE;
	public static Block DEEPSLATE_LAPIS_MINE;
	public static Block DIAMOND_MINE;
	public static Block DEEPSLATE_DIAMOND_MINE;
	public static Block NETHER_GOLD_MINE;
	public static Block QUARTZ_MINE;
	public static Block ANCIENT_DEBRIS_MINE;
	public static Block GILDED_BLACKSTONE_MINE;
	public static Block FURNACE_MINE;
	public static Block SMOKER_MINE;
	public static Block BLAST_FURNACE_MINE;
	public static Block SUSPICIOUS_SAND_MINE;
	public static Block SUSPICIOUS_GRAVEL_MINE;
	public static Block CREAKING_HEART_MINE;
	public static Item ANCIENT_DEBRIS_MINE_ITEM;
	public static Item MINE_REMOTE_ACCESS_TOOL;
	public static Item WIRE_CUTTERS;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.MineBlockEntity> MINE_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.BouncingBettyBlockEntity> BOUNCING_BETTY_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.ClaymoreBlockEntity> CLAYMORE_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.IMSBlockEntity> IMS_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.TrackMineBlockEntity> TRACK_MINE_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.BrushableMineBlockEntity> BRUSHABLE_MINE_BLOCK_ENTITY;
	public static BlockEntityType<net.geforcemods.securitycraft.blockentities.CreakingHeartMineBlockEntity> CREAKING_HEART_MINE_BLOCK_ENTITY;
	public static net.minecraft.world.entity.EntityType<net.geforcemods.securitycraft.entity.BouncingBetty> BOUNCING_BETTY_ENTITY;
	public static net.minecraft.world.entity.EntityType<net.geforcemods.securitycraft.entity.IMSBomb> IMS_BOMB_ENTITY;
	public static net.minecraft.core.component.DataComponentType<net.geforcemods.securitycraft.components.GlobalPositions> BOUND_MINES;
	public static RecipeSerializer<? extends net.minecraft.world.item.crafting.CustomRecipe> COPY_MINE_REMOTE_ACCESS_TOOL_RECIPE_SERIALIZER;
	public static net.minecraft.world.inventory.MenuType<net.geforcemods.securitycraft.inventory.SingleLensMenu> SINGLE_LENS_MENU;
	public static net.minecraft.world.inventory.MenuType<net.geforcemods.securitycraft.inventory.CustomizeBlockMenu> CUSTOMIZE_BLOCK_MENU;

	public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("general"));

	public static net.minecraft.core.component.DataComponentType<net.minecraft.util.Unit> UNREINFORCING;
	public static RecipeSerializer<? extends net.minecraft.world.item.crafting.CustomRecipe> BLOCK_REINFORCING_SERIALIZER;
	public static RecipeSerializer<? extends net.minecraft.world.item.crafting.CustomRecipe> BLOCK_UNREINFORCING_SERIALIZER;
	public static net.minecraft.world.inventory.MenuType<net.geforcemods.securitycraft.inventory.BlockReinforcerMenu> BLOCK_REINFORCER_MENU;
	public static net.minecraft.world.inventory.MenuType<net.geforcemods.securitycraft.inventory.LaserBlockMenu> LASER_BLOCK_MENU;
	public static net.minecraft.world.inventory.MenuType<net.geforcemods.securitycraft.inventory.DisguiseModuleMenu> DISGUISE_MODULE_MENU;

	/**
	 * The reinforced blocks whose properties are copied straight off their vanilla counterpart, the way upstream's
	 * {@code reinforcedBlock(name, vanillaBlock, ...)} does it, instead of being spelled out by shape the way the
	 * {@link #REINFORCED} table above does.
	 */
	private static final Object[][] REINFORCED_COPIES = {
			{"reinforced_white_carpet", Blocks.WHITE_CARPET, "carpet"},
			{"reinforced_orange_carpet", Blocks.ORANGE_CARPET, "carpet"},
			{"reinforced_magenta_carpet", Blocks.MAGENTA_CARPET, "carpet"},
			{"reinforced_light_blue_carpet", Blocks.LIGHT_BLUE_CARPET, "carpet"},
			{"reinforced_yellow_carpet", Blocks.YELLOW_CARPET, "carpet"},
			{"reinforced_lime_carpet", Blocks.LIME_CARPET, "carpet"},
			{"reinforced_pink_carpet", Blocks.PINK_CARPET, "carpet"},
			{"reinforced_gray_carpet", Blocks.GRAY_CARPET, "carpet"},
			{"reinforced_light_gray_carpet", Blocks.LIGHT_GRAY_CARPET, "carpet"},
			{"reinforced_cyan_carpet", Blocks.CYAN_CARPET, "carpet"},
			{"reinforced_purple_carpet", Blocks.PURPLE_CARPET, "carpet"},
			{"reinforced_blue_carpet", Blocks.BLUE_CARPET, "carpet"},
			{"reinforced_brown_carpet", Blocks.BROWN_CARPET, "carpet"},
			{"reinforced_green_carpet", Blocks.GREEN_CARPET, "carpet"},
			{"reinforced_red_carpet", Blocks.RED_CARPET, "carpet"},
			{"reinforced_black_carpet", Blocks.BLACK_CARPET, "carpet"},
			{"reinforced_moss_carpet", Blocks.MOSS_CARPET, "carpet"},
			{"reinforced_white_glazed_terracotta", Blocks.WHITE_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_orange_glazed_terracotta", Blocks.ORANGE_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_magenta_glazed_terracotta", Blocks.MAGENTA_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_light_blue_glazed_terracotta", Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_yellow_glazed_terracotta", Blocks.YELLOW_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_lime_glazed_terracotta", Blocks.LIME_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_pink_glazed_terracotta", Blocks.PINK_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_gray_glazed_terracotta", Blocks.GRAY_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_light_gray_glazed_terracotta", Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_cyan_glazed_terracotta", Blocks.CYAN_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_purple_glazed_terracotta", Blocks.PURPLE_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_blue_glazed_terracotta", Blocks.BLUE_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_brown_glazed_terracotta", Blocks.BROWN_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_green_glazed_terracotta", Blocks.GREEN_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_red_glazed_terracotta", Blocks.RED_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_black_glazed_terracotta", Blocks.BLACK_GLAZED_TERRACOTTA, "glazed_terracotta"},
			{"reinforced_bookshelf", Blocks.BOOKSHELF, "cube"},
			{"reinforced_sea_lantern", Blocks.SEA_LANTERN, "cube"},
			{"reinforced_redstone_lamp", Blocks.REDSTONE_LAMP, "redstone_lamp"},
			{"reinforced_grass_block", Blocks.GRASS_BLOCK, "grass_block"},
			{"reinforced_podzol", Blocks.PODZOL, "snowy_dirt"},
			{"reinforced_mycelium", Blocks.MYCELIUM, "snowy_dirt"},
			{"reinforced_cobweb", Blocks.COBWEB, "cobweb"},
			{"reinforced_chain", Blocks.CHAIN, "chain"},
			{"reinforced_end_rod", Blocks.END_ROD, "end_rod"},
			{"reinforced_lantern", Blocks.LANTERN, "lantern"},
			{"reinforced_soul_lantern", Blocks.SOUL_LANTERN, "lantern"},
			{"reinforced_ladder", Blocks.LADDER, "ladder"},
			{"reinforced_scaffolding", Blocks.SCAFFOLDING, "scaffolding"},
			{"reinforced_lever", Blocks.LEVER, "lever"},
	};

	/**
	 * The reinforced half of the crystal quartz set. It goes through the same path as {@link #REINFORCED_COPIES},
	 * copying the vanilla quartz block each one is shaped after; the cyan tint comes from {@link #crystalQuartzProps}.
	 */
	private static final Object[][] CRYSTAL_QUARTZ_REINFORCED = {
			{"reinforced_crystal_quartz_block", Blocks.QUARTZ_BLOCK, "cube"},
			{"reinforced_smooth_crystal_quartz", Blocks.SMOOTH_QUARTZ, "cube"},
			{"reinforced_chiseled_crystal_quartz_block", Blocks.CHISELED_QUARTZ_BLOCK, "cube"},
			{"reinforced_crystal_quartz_bricks", Blocks.QUARTZ_BRICKS, "cube"},
			{"reinforced_crystal_quartz_pillar", Blocks.QUARTZ_PILLAR, "pillar"},
			{"reinforced_crystal_quartz_stairs", Blocks.QUARTZ_STAIRS, "stairs"},
			{"reinforced_crystal_quartz_slab", Blocks.QUARTZ_SLAB, "slab"},
			{"reinforced_smooth_crystal_quartz_stairs", Blocks.SMOOTH_QUARTZ_STAIRS, "stairs"},
			{"reinforced_smooth_crystal_quartz_slab", Blocks.SMOOTH_QUARTZ_SLAB, "slab"}
	};

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
			{"reinforced_exposed_chiseled_copper", "cube"},
			{"reinforced_exposed_copper", "cube"},
			{"reinforced_exposed_cut_copper", "cube"},
			{"reinforced_exposed_cut_copper_slab", "slab"},
			{"reinforced_exposed_cut_copper_stairs", "stairs"},
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
			{"reinforced_oxidized_chiseled_copper", "cube"},
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
			{"reinforced_polished_tuff", "cube"},
			{"reinforced_polished_tuff_slab", "slab"},
			{"reinforced_polished_tuff_stairs", "stairs"},
			{"reinforced_polished_tuff_wall", "wall"},
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
			{"reinforced_tuff_brick_slab", "slab"},
			{"reinforced_tuff_brick_stairs", "stairs"},
			{"reinforced_tuff_brick_wall", "wall"},
			{"reinforced_tuff_bricks", "cube"},
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
			{"reinforced_weathered_chiseled_copper", "cube"},
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
		return ResourceLocation.fromNamespaceAndPath(SecurityCraft.MODID, name);
	}

	public static void init() {
		KEYPAD = register("keypad", key -> new KeypadBlock(alwaysDrop(BlockBehaviour.Properties.of().strength(2.0F, 12000.0F)).setId(key)));
		LASER_BLOCK = (net.geforcemods.securitycraft.blocks.LaserBlock) register("laser_block", key -> new net.geforcemods.securitycraft.blocks.LaserBlock(alwaysDrop(BlockBehaviour.Properties.of().strength(3.5F).sound(SoundType.METAL)).setId(key)));
		LASER_FIELD = (net.geforcemods.securitycraft.blocks.LaserFieldBlock) registerBlockNoItem("laser", key -> new net.geforcemods.securitycraft.blocks.LaserFieldBlock(alwaysDrop(BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.NONE).strength(-1.0F).noLootTable().noOcclusion()).setId(key)));
		KEY_PANEL = (net.geforcemods.securitycraft.blocks.KeyPanelBlock) registerBlockNoItem("key_panel", key -> new net.geforcemods.securitycraft.blocks.KeyPanelBlock(alwaysDrop(BlockBehaviour.Properties.of().strength(3.5F).sound(SoundType.METAL).noOcclusion()).setId(key)));
		KEY_PANEL_ITEM = registerItem("keypad_item", new net.geforcemods.securitycraft.items.KeyPanelItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id("keypad_item")))));
		FRAME = (net.geforcemods.securitycraft.blocks.FrameBlock) register("keypad_frame", key -> new net.geforcemods.securitycraft.blocks.FrameBlock(alwaysDrop(BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(5.0F).sound(SoundType.METAL).noOcclusion()).setId(key)));
		REINFORCED_DOOR = register("reinforced_iron_door", key -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedDoorBlock(BlockSetType.IRON, alwaysDrop(BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(5.0F, 12000.0F).sound(SoundType.METAL).noOcclusion()).setId(key)));
		//listing the door here is what lets the Universal Block Reinforcer turn a vanilla iron door into it, and
		//the Remover turn it back; both look the counterpart up by the reinforced_<vanilla> name
		//deliberately NOT added to REINFORCED_BLOCKS: that list drives the grey tint, and the door has its own
		//artwork, so tinting it would grey out an item the original leaves alone
		REINFORCED_BY_NAME.put("reinforced_iron_door", REINFORCED_DOOR);
		CUTOUT_BLOCKS.add(REINFORCED_DOOR);
		//upstream registers the electrified iron fence gate under the legacy name "reinforced_fence_gate"
		ELECTRIFIED_IRON_FENCE = (net.geforcemods.securitycraft.blocks.ElectrifiedIronFenceBlock) register("electrified_iron_fence", new net.geforcemods.securitycraft.blocks.ElectrifiedIronFenceBlock(alwaysDrop(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, Float.MAX_VALUE).sound(SoundType.METAL))));
		ELECTRIFIED_IRON_FENCE_GATE = (net.geforcemods.securitycraft.blocks.ElectrifiedIronFenceGateBlock) register("reinforced_fence_gate", new net.geforcemods.securitycraft.blocks.ElectrifiedIronFenceGateBlock(alwaysDrop(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, Float.MAX_VALUE).sound(SoundType.METAL))));
		//both textures have transparent pixels; Fabric has no equivalent of the "render_type" model field upstream relies on
		CUTOUT_BLOCKS.add(ELECTRIFIED_IRON_FENCE);
		CUTOUT_BLOCKS.add(ELECTRIFIED_IRON_FENCE_GATE);
		//the passcode-protected containers; the furnace family's light level reads the LIT property the way vanilla's does
		KEYPAD_CHEST = register("keypad_chest", new net.geforcemods.securitycraft.blocks.KeypadChestBlock(alwaysDrop(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, Float.MAX_VALUE).sound(SoundType.METAL))));
		KEYPAD_BARREL = register("keypad_barrel", new net.geforcemods.securitycraft.blocks.KeypadBarrelBlock(alwaysDrop(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, Float.MAX_VALUE).sound(SoundType.METAL))));
		KEYPAD_FURNACE = register("keypad_furnace", new net.geforcemods.securitycraft.blocks.KeypadFurnaceBlock(keypadFurnaceProps()));
		KEYPAD_SMOKER = register("keypad_smoker", new net.geforcemods.securitycraft.blocks.KeypadSmokerBlock(keypadFurnaceProps()));
		KEYPAD_BLAST_FURNACE = register("keypad_blast_furnace", new net.geforcemods.securitycraft.blocks.KeypadBlastFurnaceBlock(keypadFurnaceProps()));
		//the furnace family's front window is a see-through texture, same situation as the reinforced door
		CUTOUT_BLOCKS.add(KEYPAD_FURNACE);
		CUTOUT_BLOCKS.add(KEYPAD_SMOKER);
		CUTOUT_BLOCKS.add(KEYPAD_BLAST_FURNACE);
		net.geforcemods.securitycraft.api.SecurityCraftAPI.registerPasscodeConvertible(new net.geforcemods.securitycraft.blocks.KeypadBlock.Convertible());
		net.geforcemods.securitycraft.api.SecurityCraftAPI.registerPasscodeConvertible(new net.geforcemods.securitycraft.blocks.KeypadChestBlock.Convertible());
		net.geforcemods.securitycraft.api.SecurityCraftAPI.registerPasscodeConvertible(new net.geforcemods.securitycraft.blocks.KeypadBarrelBlock.Convertible());
		net.geforcemods.securitycraft.api.SecurityCraftAPI.registerPasscodeConvertible(new net.geforcemods.securitycraft.blocks.AbstractKeypadFurnaceBlock.Convertible(Blocks.FURNACE, KEYPAD_FURNACE));
		net.geforcemods.securitycraft.api.SecurityCraftAPI.registerPasscodeConvertible(new net.geforcemods.securitycraft.blocks.AbstractKeypadFurnaceBlock.Convertible(Blocks.SMOKER, KEYPAD_SMOKER));
		net.geforcemods.securitycraft.api.SecurityCraftAPI.registerPasscodeConvertible(new net.geforcemods.securitycraft.blocks.AbstractKeypadFurnaceBlock.Convertible(Blocks.BLAST_FURNACE, KEYPAD_BLAST_FURNACE));

		for (String[] entry : REINFORCED)
			registerReinforced(entry[0], entry[1]);

		LIST_MODULE_DATA = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("list_module_data"), net.minecraft.core.component.DataComponentType.<net.geforcemods.securitycraft.components.ListModuleData>builder().persistent(net.geforcemods.securitycraft.components.ListModuleData.CODEC).networkSynchronized(net.geforcemods.securitycraft.components.ListModuleData.STREAM_CODEC).cacheEncoding().build());
		//the reinforced blocks that keep a vanilla block entity of their own, so they cannot go through either table
		registerFunctionalReinforced("reinforced_hopper", new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedHopperBlock(reinforcedCopy(Blocks.HOPPER)));
		registerFunctionalReinforced("reinforced_dispenser", new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedDispenserBlock(reinforcedCopy(Blocks.DISPENSER)));
		registerFunctionalReinforced("reinforced_dropper", new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedDropperBlock(reinforcedCopy(Blocks.DROPPER)));
		registerFunctionalReinforced("reinforced_observer", new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedObserverBlock(reinforcedCopy(Blocks.OBSERVER)));

		//the crystal quartz set: the mod's own decorative stone, plus its reinforced counterparts
		CRYSTAL_QUARTZ = register("crystal_quartz", new Block(crystalQuartzProps(Blocks.QUARTZ_BLOCK)));
		SMOOTH_CRYSTAL_QUARTZ = register("smooth_crystal_quartz", new Block(crystalQuartzProps(Blocks.SMOOTH_QUARTZ)));
		CHISELED_CRYSTAL_QUARTZ = register("chiseled_crystal_quartz", new Block(crystalQuartzProps(Blocks.CHISELED_QUARTZ_BLOCK)));
		CRYSTAL_QUARTZ_BRICKS = register("crystal_quartz_bricks", new Block(crystalQuartzProps(Blocks.QUARTZ_BRICKS)));
		CRYSTAL_QUARTZ_PILLAR = register("crystal_quartz_pillar", new RotatedPillarBlock(crystalQuartzProps(Blocks.QUARTZ_PILLAR)));
		CRYSTAL_QUARTZ_STAIRS = register("crystal_quartz_stairs", new StairBlock(CRYSTAL_QUARTZ.defaultBlockState(), crystalQuartzProps(Blocks.QUARTZ_STAIRS)));
		CRYSTAL_QUARTZ_SLAB = register("crystal_quartz_slab", new SlabBlock(crystalQuartzProps(Blocks.QUARTZ_SLAB)));
		SMOOTH_CRYSTAL_QUARTZ_STAIRS = register("smooth_crystal_quartz_stairs", new StairBlock(SMOOTH_CRYSTAL_QUARTZ.defaultBlockState(), crystalQuartzProps(Blocks.SMOOTH_QUARTZ_STAIRS)));
		SMOOTH_CRYSTAL_QUARTZ_SLAB = register("smooth_crystal_quartz_slab", new SlabBlock(crystalQuartzProps(Blocks.SMOOTH_QUARTZ_SLAB)));
		CRYSTAL_QUARTZ_ITEM = registerItem("crystal_quartz_item", new Item(new Item.Properties()));

		//the secret signs: only their owner and the players on their allowlist can read what is written on them
		SECRET_OAK_SIGN = registerBlockNoItem("secret_sign_standing", new net.geforcemods.securitycraft.blocks.SecretStandingSignBlock(reinforcedCopy(Blocks.OAK_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.OAK_SIGN)));
		SECRET_OAK_WALL_SIGN = registerBlockNoItem("secret_sign_wall", new net.geforcemods.securitycraft.blocks.SecretWallSignBlock(reinforcedCopy(Blocks.OAK_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.OAK_SIGN)));
		SECRET_OAK_HANGING_SIGN = registerBlockNoItem("secret_oak_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretCeilingHangingSignBlock(reinforcedCopy(Blocks.OAK_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.OAK_HANGING_SIGN)));
		SECRET_OAK_WALL_HANGING_SIGN = registerBlockNoItem("secret_oak_wall_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretWallHangingSignBlock(reinforcedCopy(Blocks.OAK_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.OAK_HANGING_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_sign_item", new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), SECRET_OAK_SIGN, SECRET_OAK_WALL_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_oak_hanging_sign", new net.minecraft.world.item.HangingSignItem(SECRET_OAK_HANGING_SIGN, SECRET_OAK_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))));
		SECRET_SPRUCE_SIGN = registerBlockNoItem("secret_spruce_sign_standing", new net.geforcemods.securitycraft.blocks.SecretStandingSignBlock(reinforcedCopy(Blocks.SPRUCE_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.SPRUCE_SIGN)));
		SECRET_SPRUCE_WALL_SIGN = registerBlockNoItem("secret_spruce_sign_wall", new net.geforcemods.securitycraft.blocks.SecretWallSignBlock(reinforcedCopy(Blocks.SPRUCE_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.SPRUCE_SIGN)));
		SECRET_SPRUCE_HANGING_SIGN = registerBlockNoItem("secret_spruce_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretCeilingHangingSignBlock(reinforcedCopy(Blocks.SPRUCE_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.SPRUCE_HANGING_SIGN)));
		SECRET_SPRUCE_WALL_HANGING_SIGN = registerBlockNoItem("secret_spruce_wall_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretWallHangingSignBlock(reinforcedCopy(Blocks.SPRUCE_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.SPRUCE_HANGING_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_spruce_sign_item", new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), SECRET_SPRUCE_SIGN, SECRET_SPRUCE_WALL_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_spruce_hanging_sign", new net.minecraft.world.item.HangingSignItem(SECRET_SPRUCE_HANGING_SIGN, SECRET_SPRUCE_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))));
		SECRET_BIRCH_SIGN = registerBlockNoItem("secret_birch_sign_standing", new net.geforcemods.securitycraft.blocks.SecretStandingSignBlock(reinforcedCopy(Blocks.BIRCH_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.BIRCH_SIGN)));
		SECRET_BIRCH_WALL_SIGN = registerBlockNoItem("secret_birch_sign_wall", new net.geforcemods.securitycraft.blocks.SecretWallSignBlock(reinforcedCopy(Blocks.BIRCH_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.BIRCH_SIGN)));
		SECRET_BIRCH_HANGING_SIGN = registerBlockNoItem("secret_birch_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretCeilingHangingSignBlock(reinforcedCopy(Blocks.BIRCH_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.BIRCH_HANGING_SIGN)));
		SECRET_BIRCH_WALL_HANGING_SIGN = registerBlockNoItem("secret_birch_wall_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretWallHangingSignBlock(reinforcedCopy(Blocks.BIRCH_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.BIRCH_HANGING_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_birch_sign_item", new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), SECRET_BIRCH_SIGN, SECRET_BIRCH_WALL_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_birch_hanging_sign", new net.minecraft.world.item.HangingSignItem(SECRET_BIRCH_HANGING_SIGN, SECRET_BIRCH_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))));
		SECRET_JUNGLE_SIGN = registerBlockNoItem("secret_jungle_sign_standing", new net.geforcemods.securitycraft.blocks.SecretStandingSignBlock(reinforcedCopy(Blocks.JUNGLE_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.JUNGLE_SIGN)));
		SECRET_JUNGLE_WALL_SIGN = registerBlockNoItem("secret_jungle_sign_wall", new net.geforcemods.securitycraft.blocks.SecretWallSignBlock(reinforcedCopy(Blocks.JUNGLE_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.JUNGLE_SIGN)));
		SECRET_JUNGLE_HANGING_SIGN = registerBlockNoItem("secret_jungle_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretCeilingHangingSignBlock(reinforcedCopy(Blocks.JUNGLE_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.JUNGLE_HANGING_SIGN)));
		SECRET_JUNGLE_WALL_HANGING_SIGN = registerBlockNoItem("secret_jungle_wall_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretWallHangingSignBlock(reinforcedCopy(Blocks.JUNGLE_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.JUNGLE_HANGING_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_jungle_sign_item", new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), SECRET_JUNGLE_SIGN, SECRET_JUNGLE_WALL_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_jungle_hanging_sign", new net.minecraft.world.item.HangingSignItem(SECRET_JUNGLE_HANGING_SIGN, SECRET_JUNGLE_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))));
		SECRET_ACACIA_SIGN = registerBlockNoItem("secret_acacia_sign_standing", new net.geforcemods.securitycraft.blocks.SecretStandingSignBlock(reinforcedCopy(Blocks.ACACIA_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.ACACIA_SIGN)));
		SECRET_ACACIA_WALL_SIGN = registerBlockNoItem("secret_acacia_sign_wall", new net.geforcemods.securitycraft.blocks.SecretWallSignBlock(reinforcedCopy(Blocks.ACACIA_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.ACACIA_SIGN)));
		SECRET_ACACIA_HANGING_SIGN = registerBlockNoItem("secret_acacia_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretCeilingHangingSignBlock(reinforcedCopy(Blocks.ACACIA_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.ACACIA_HANGING_SIGN)));
		SECRET_ACACIA_WALL_HANGING_SIGN = registerBlockNoItem("secret_acacia_wall_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretWallHangingSignBlock(reinforcedCopy(Blocks.ACACIA_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.ACACIA_HANGING_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_acacia_sign_item", new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), SECRET_ACACIA_SIGN, SECRET_ACACIA_WALL_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_acacia_hanging_sign", new net.minecraft.world.item.HangingSignItem(SECRET_ACACIA_HANGING_SIGN, SECRET_ACACIA_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))));
		SECRET_DARK_OAK_SIGN = registerBlockNoItem("secret_dark_oak_sign_standing", new net.geforcemods.securitycraft.blocks.SecretStandingSignBlock(reinforcedCopy(Blocks.DARK_OAK_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.DARK_OAK_SIGN)));
		SECRET_DARK_OAK_WALL_SIGN = registerBlockNoItem("secret_dark_oak_sign_wall", new net.geforcemods.securitycraft.blocks.SecretWallSignBlock(reinforcedCopy(Blocks.DARK_OAK_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.DARK_OAK_SIGN)));
		SECRET_DARK_OAK_HANGING_SIGN = registerBlockNoItem("secret_dark_oak_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretCeilingHangingSignBlock(reinforcedCopy(Blocks.DARK_OAK_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.DARK_OAK_HANGING_SIGN)));
		SECRET_DARK_OAK_WALL_HANGING_SIGN = registerBlockNoItem("secret_dark_oak_wall_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretWallHangingSignBlock(reinforcedCopy(Blocks.DARK_OAK_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.DARK_OAK_HANGING_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_dark_oak_sign_item", new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), SECRET_DARK_OAK_SIGN, SECRET_DARK_OAK_WALL_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_dark_oak_hanging_sign", new net.minecraft.world.item.HangingSignItem(SECRET_DARK_OAK_HANGING_SIGN, SECRET_DARK_OAK_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))));
		SECRET_MANGROVE_SIGN = registerBlockNoItem("secret_mangrove_sign_standing", new net.geforcemods.securitycraft.blocks.SecretStandingSignBlock(reinforcedCopy(Blocks.MANGROVE_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.MANGROVE_SIGN)));
		SECRET_MANGROVE_WALL_SIGN = registerBlockNoItem("secret_mangrove_sign_wall", new net.geforcemods.securitycraft.blocks.SecretWallSignBlock(reinforcedCopy(Blocks.MANGROVE_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.MANGROVE_SIGN)));
		SECRET_MANGROVE_HANGING_SIGN = registerBlockNoItem("secret_mangrove_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretCeilingHangingSignBlock(reinforcedCopy(Blocks.MANGROVE_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.MANGROVE_HANGING_SIGN)));
		SECRET_MANGROVE_WALL_HANGING_SIGN = registerBlockNoItem("secret_mangrove_wall_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretWallHangingSignBlock(reinforcedCopy(Blocks.MANGROVE_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.MANGROVE_HANGING_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_mangrove_sign_item", new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), SECRET_MANGROVE_SIGN, SECRET_MANGROVE_WALL_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_mangrove_hanging_sign", new net.minecraft.world.item.HangingSignItem(SECRET_MANGROVE_HANGING_SIGN, SECRET_MANGROVE_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))));
		SECRET_CHERRY_SIGN = registerBlockNoItem("secret_cherry_sign_standing", new net.geforcemods.securitycraft.blocks.SecretStandingSignBlock(reinforcedCopy(Blocks.CHERRY_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.CHERRY_SIGN)));
		SECRET_CHERRY_WALL_SIGN = registerBlockNoItem("secret_cherry_sign_wall", new net.geforcemods.securitycraft.blocks.SecretWallSignBlock(reinforcedCopy(Blocks.CHERRY_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.CHERRY_SIGN)));
		SECRET_CHERRY_HANGING_SIGN = registerBlockNoItem("secret_cherry_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretCeilingHangingSignBlock(reinforcedCopy(Blocks.CHERRY_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.CHERRY_HANGING_SIGN)));
		SECRET_CHERRY_WALL_HANGING_SIGN = registerBlockNoItem("secret_cherry_wall_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretWallHangingSignBlock(reinforcedCopy(Blocks.CHERRY_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.CHERRY_HANGING_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_cherry_sign_item", new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), SECRET_CHERRY_SIGN, SECRET_CHERRY_WALL_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_cherry_hanging_sign", new net.minecraft.world.item.HangingSignItem(SECRET_CHERRY_HANGING_SIGN, SECRET_CHERRY_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))));
		SECRET_BAMBOO_SIGN = registerBlockNoItem("secret_bamboo_sign_standing", new net.geforcemods.securitycraft.blocks.SecretStandingSignBlock(reinforcedCopy(Blocks.BAMBOO_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.BAMBOO_SIGN)));
		SECRET_BAMBOO_WALL_SIGN = registerBlockNoItem("secret_bamboo_sign_wall", new net.geforcemods.securitycraft.blocks.SecretWallSignBlock(reinforcedCopy(Blocks.BAMBOO_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.BAMBOO_SIGN)));
		SECRET_BAMBOO_HANGING_SIGN = registerBlockNoItem("secret_bamboo_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretCeilingHangingSignBlock(reinforcedCopy(Blocks.BAMBOO_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.BAMBOO_HANGING_SIGN)));
		SECRET_BAMBOO_WALL_HANGING_SIGN = registerBlockNoItem("secret_bamboo_wall_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretWallHangingSignBlock(reinforcedCopy(Blocks.BAMBOO_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.BAMBOO_HANGING_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_bamboo_sign_item", new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), SECRET_BAMBOO_SIGN, SECRET_BAMBOO_WALL_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_bamboo_hanging_sign", new net.minecraft.world.item.HangingSignItem(SECRET_BAMBOO_HANGING_SIGN, SECRET_BAMBOO_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))));
		SECRET_CRIMSON_SIGN = registerBlockNoItem("secret_crimson_sign_standing", new net.geforcemods.securitycraft.blocks.SecretStandingSignBlock(reinforcedCopy(Blocks.CRIMSON_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.CRIMSON_SIGN)));
		SECRET_CRIMSON_WALL_SIGN = registerBlockNoItem("secret_crimson_sign_wall", new net.geforcemods.securitycraft.blocks.SecretWallSignBlock(reinforcedCopy(Blocks.CRIMSON_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.CRIMSON_SIGN)));
		SECRET_CRIMSON_HANGING_SIGN = registerBlockNoItem("secret_crimson_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretCeilingHangingSignBlock(reinforcedCopy(Blocks.CRIMSON_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.CRIMSON_HANGING_SIGN)));
		SECRET_CRIMSON_WALL_HANGING_SIGN = registerBlockNoItem("secret_crimson_wall_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretWallHangingSignBlock(reinforcedCopy(Blocks.CRIMSON_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.CRIMSON_HANGING_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_crimson_sign_item", new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), SECRET_CRIMSON_SIGN, SECRET_CRIMSON_WALL_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_crimson_hanging_sign", new net.minecraft.world.item.HangingSignItem(SECRET_CRIMSON_HANGING_SIGN, SECRET_CRIMSON_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))));
		SECRET_WARPED_SIGN = registerBlockNoItem("secret_warped_sign_standing", new net.geforcemods.securitycraft.blocks.SecretStandingSignBlock(reinforcedCopy(Blocks.WARPED_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.WARPED_SIGN)));
		SECRET_WARPED_WALL_SIGN = registerBlockNoItem("secret_warped_sign_wall", new net.geforcemods.securitycraft.blocks.SecretWallSignBlock(reinforcedCopy(Blocks.WARPED_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.WARPED_SIGN)));
		SECRET_WARPED_HANGING_SIGN = registerBlockNoItem("secret_warped_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretCeilingHangingSignBlock(reinforcedCopy(Blocks.WARPED_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.WARPED_HANGING_SIGN)));
		SECRET_WARPED_WALL_HANGING_SIGN = registerBlockNoItem("secret_warped_wall_hanging_sign", new net.geforcemods.securitycraft.blocks.SecretWallHangingSignBlock(reinforcedCopy(Blocks.WARPED_HANGING_SIGN), net.minecraft.world.level.block.SignBlock.getWoodType(Blocks.WARPED_HANGING_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_warped_sign_item", new net.minecraft.world.item.SignItem(new Item.Properties().stacksTo(16), SECRET_WARPED_SIGN, SECRET_WARPED_WALL_SIGN)));
		SECRET_SIGN_ITEMS.add(registerItem("secret_warped_hanging_sign", new net.minecraft.world.item.HangingSignItem(SECRET_WARPED_HANGING_SIGN, SECRET_WARPED_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))));

		for (Object[] entry : CRYSTAL_QUARTZ_REINFORCED)
			registerReinforcedCopy((String) entry[0], (Block) entry[1], (String) entry[2]);

		for (Object[] entry : REINFORCED_COPIES)
			registerReinforcedCopy((String) entry[0], (Block) entry[1], (String) entry[2]);

		REDSTONE_MODULE = registerModule("redstone_module", net.geforcemods.securitycraft.misc.ModuleType.REDSTONE, false, false, false);
		ALLOWLIST_MODULE = registerModule("whitelist_module", net.geforcemods.securitycraft.misc.ModuleType.ALLOWLIST, true, true, true);
		DENYLIST_MODULE = registerModule("blacklist_module", net.geforcemods.securitycraft.misc.ModuleType.DENYLIST, true, true, true);
		HARMING_MODULE = registerModule("harming_module", net.geforcemods.securitycraft.misc.ModuleType.HARMING, false, false, false);
		SMART_MODULE = registerModule("smart_module", net.geforcemods.securitycraft.misc.ModuleType.SMART, false, false, false);
		STORAGE_MODULE = registerModule("storage_module", net.geforcemods.securitycraft.misc.ModuleType.STORAGE, false, false, false);
		DISGUISE_MODULE = registerModule("disguise_module", net.geforcemods.securitycraft.misc.ModuleType.DISGUISE, false, true, false);
		SPEED_MODULE = registerModule("speed_module", net.geforcemods.securitycraft.misc.ModuleType.SPEED, false, false, false);
		UNIVERSAL_BLOCK_MODIFIER = registerItem("universal_block_modifier", new net.geforcemods.securitycraft.items.UniversalBlockModifierItem(new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, id("universal_block_modifier")))));
		UNIVERSAL_OWNER_CHANGER = registerItem("universal_owner_changer", new net.geforcemods.securitycraft.items.UniversalOwnerChangerItem(new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, id("universal_owner_changer")))));
		SC_MANUAL = registerItem("sc_manual", new net.geforcemods.securitycraft.items.SCManualItem(new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, id("sc_manual")))));
		LENS = registerItem("lens", new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id("lens")))));
		//the fluids have to exist before their blocks, and the blocks before the buckets, since each refers back
		FLOWING_FAKE_WATER = Registry.register(BuiltInRegistries.FLUID, id("flowing_fake_water"), new net.geforcemods.securitycraft.fluids.FakeWaterFluid.Flowing());
		FAKE_WATER = Registry.register(BuiltInRegistries.FLUID, id("fake_water"), new net.geforcemods.securitycraft.fluids.FakeWaterFluid.Source());
		FLOWING_FAKE_LAVA = Registry.register(BuiltInRegistries.FLUID, id("flowing_fake_lava"), new net.geforcemods.securitycraft.fluids.FakeLavaFluid.Flowing());
		FAKE_LAVA = Registry.register(BuiltInRegistries.FLUID, id("fake_lava"), new net.geforcemods.securitycraft.fluids.FakeLavaFluid.Source());
		FAKE_WATER_BLOCK = registerBlockNoItem("fake_water_block", key -> new net.geforcemods.securitycraft.blocks.FakeWaterBlock(reinforcedCopy(Blocks.WATER).setId(key), () -> FAKE_WATER));
		FAKE_LAVA_BLOCK = registerBlockNoItem("fake_lava_block", key -> new net.geforcemods.securitycraft.blocks.FakeLavaBlock(reinforcedCopy(Blocks.LAVA).setId(key), () -> FAKE_LAVA));
		FAKE_WATER_BUCKET = registerItem("bucket_f_water", new net.geforcemods.securitycraft.items.FakeLiquidBucketItem(() -> FAKE_WATER, new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, id("bucket_f_water")))));
		FAKE_LAVA_BUCKET = registerItem("bucket_f_lava", new net.geforcemods.securitycraft.items.FakeLiquidBucketItem(() -> FAKE_LAVA, new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, id("bucket_f_lava")))));

		PORTABLE_RADAR = (net.geforcemods.securitycraft.blocks.PortableRadarBlock) register("portable_radar", key -> new net.geforcemods.securitycraft.blocks.PortableRadarBlock(mineProp(net.minecraft.world.level.material.MapColor.COLOR_BLACK, 5.0F).setId(key)));

		MINE = register("mine", key -> new net.geforcemods.securitycraft.blocks.mines.MineBlock(mineProp(net.minecraft.world.level.material.MapColor.METAL, 3.5F).sound(SoundType.METAL).forceSolidOn().pushReaction(net.minecraft.world.level.material.PushReaction.NORMAL).setId(key)));
		BOUNCING_BETTY = register("bouncing_betty", key -> new net.geforcemods.securitycraft.blocks.mines.BouncingBettyBlock(mineProp(net.minecraft.world.level.material.MapColor.METAL, 3.5F).sound(SoundType.METAL).forceSolidOn().pushReaction(net.minecraft.world.level.material.PushReaction.NORMAL).setId(key)));
		CLAYMORE = register("claymore", key -> new net.geforcemods.securitycraft.blocks.mines.ClaymoreBlock(mineProp(net.minecraft.world.level.material.MapColor.TERRACOTTA_GREEN, 3.5F).sound(SoundType.METAL).forceSolidOn().pushReaction(net.minecraft.world.level.material.PushReaction.NORMAL).setId(key)));
		IMS = register("ims", key -> new net.geforcemods.securitycraft.blocks.mines.IMSBlock(mineProp(net.minecraft.world.level.material.MapColor.TERRACOTTA_GREEN, 3.5F).sound(SoundType.METAL).setId(key)));
		TRACK_MINE = register("track_mine", key -> new net.geforcemods.securitycraft.blocks.mines.TrackMineBlock(mineProp(net.minecraft.world.level.material.MapColor.METAL, 0.7F).noCollission().sound(SoundType.METAL).setId(key)));
		STONE_MINE = registerBlockMine("stone_mine", Blocks.STONE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		DEEPSLATE_MINE = registerBlockMine("deepslate_mine", Blocks.DEEPSLATE, net.geforcemods.securitycraft.blocks.mines.DeepslateMineBlock::new);
		COBBLED_DEEPSLATE_MINE = registerBlockMine("cobbled_deepslate_mine", Blocks.COBBLED_DEEPSLATE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		DIRT_MINE = registerBlockMine("dirt_mine", Blocks.DIRT, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		COBBLESTONE_MINE = registerBlockMine("cobblestone_mine", Blocks.COBBLESTONE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		SAND_MINE = registerBlockMine("sand_mine", Blocks.SAND, net.geforcemods.securitycraft.blocks.mines.FallingBlockMineBlock::new);
		GRAVEL_MINE = registerBlockMine("gravel_mine", Blocks.GRAVEL, net.geforcemods.securitycraft.blocks.mines.FallingBlockMineBlock::new);
		NETHERRACK_MINE = registerBlockMine("netherrack_mine", Blocks.NETHERRACK, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		END_STONE_MINE = registerBlockMine("end_stone_mine", Blocks.END_STONE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		COAL_MINE = registerBlockMine("coal_mine", Blocks.COAL_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		DEEPSLATE_COAL_MINE = registerBlockMine("deepslate_coal_mine", Blocks.DEEPSLATE_COAL_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		IRON_MINE = registerBlockMine("iron_mine", Blocks.IRON_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		DEEPSLATE_IRON_MINE = registerBlockMine("deepslate_iron_mine", Blocks.DEEPSLATE_IRON_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		GOLD_MINE = registerBlockMine("gold_mine", Blocks.GOLD_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		DEEPSLATE_GOLD_MINE = registerBlockMine("deepslate_gold_mine", Blocks.DEEPSLATE_GOLD_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		COPPER_MINE = registerBlockMine("copper_mine", Blocks.COPPER_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		DEEPSLATE_COPPER_MINE = registerBlockMine("deepslate_copper_mine", Blocks.DEEPSLATE_COPPER_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		REDSTONE_MINE = registerBlockMine("redstone_mine", Blocks.REDSTONE_ORE, net.geforcemods.securitycraft.blocks.mines.RedstoneOreMineBlock::new);
		DEEPSLATE_REDSTONE_MINE = registerBlockMine("deepslate_redstone_mine", Blocks.DEEPSLATE_REDSTONE_ORE, net.geforcemods.securitycraft.blocks.mines.RedstoneOreMineBlock::new);
		EMERALD_MINE = registerBlockMine("emerald_mine", Blocks.EMERALD_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		DEEPSLATE_EMERALD_MINE = registerBlockMine("deepslate_emerald_mine", Blocks.DEEPSLATE_EMERALD_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		LAPIS_MINE = registerBlockMine("lapis_mine", Blocks.LAPIS_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		DEEPSLATE_LAPIS_MINE = registerBlockMine("deepslate_lapis_mine", Blocks.DEEPSLATE_LAPIS_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		DIAMOND_MINE = registerBlockMine("diamond_mine", Blocks.DIAMOND_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		DEEPSLATE_DIAMOND_MINE = registerBlockMine("deepslate_diamond_mine", Blocks.DEEPSLATE_DIAMOND_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		NETHER_GOLD_MINE = registerBlockMine("nether_gold_mine", Blocks.NETHER_GOLD_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		QUARTZ_MINE = registerBlockMine("quartz_mine", Blocks.NETHER_QUARTZ_ORE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		//the only block mine whose item is fire resistant, so it is registered separately from its block
		ANCIENT_DEBRIS_MINE = registerBlockNoItem("ancient_debris_mine", key -> new net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock(reinforcedCopy(Blocks.ANCIENT_DEBRIS).setId(key), Blocks.ANCIENT_DEBRIS));
		ANCIENT_DEBRIS_MINE_ITEM = registerItem("ancient_debris_mine", new BlockItem(ANCIENT_DEBRIS_MINE, new Item.Properties().fireResistant().setId(ResourceKey.create(Registries.ITEM, id("ancient_debris_mine")))));
		GILDED_BLACKSTONE_MINE = registerBlockMine("gilded_blackstone_mine", Blocks.GILDED_BLACKSTONE, net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock::new);
		//the light level override is mandatory: Properties.ofFullCopy carries vanilla's litBlockEmission lambda, which reads a LIT property FurnaceMineBlock does not have
		FURNACE_MINE = registerBlockNoItem("furnace_mine", key -> new net.geforcemods.securitycraft.blocks.mines.FurnaceMineBlock(reinforcedCopy(Blocks.FURNACE).lightLevel(state -> 0).setId(key), Blocks.FURNACE));
		registerBlockItem("furnace_mine", FURNACE_MINE);
		SMOKER_MINE = registerBlockNoItem("smoker_mine", key -> new net.geforcemods.securitycraft.blocks.mines.FurnaceMineBlock(reinforcedCopy(Blocks.SMOKER).lightLevel(state -> 0).setId(key), Blocks.SMOKER));
		registerBlockItem("smoker_mine", SMOKER_MINE);
		BLAST_FURNACE_MINE = registerBlockNoItem("blast_furnace_mine", key -> new net.geforcemods.securitycraft.blocks.mines.FurnaceMineBlock(reinforcedCopy(Blocks.BLAST_FURNACE).lightLevel(state -> 0).setId(key), Blocks.BLAST_FURNACE));
		registerBlockItem("blast_furnace_mine", BLAST_FURNACE_MINE);
		SUSPICIOUS_SAND_MINE = registerBlockMine("suspicious_sand_mine", Blocks.SUSPICIOUS_SAND, net.geforcemods.securitycraft.blocks.mines.BrushableMineBlock::new);
		SUSPICIOUS_GRAVEL_MINE = registerBlockMine("suspicious_gravel_mine", Blocks.SUSPICIOUS_GRAVEL, net.geforcemods.securitycraft.blocks.mines.BrushableMineBlock::new);
		CREAKING_HEART_MINE = registerBlockMine("creaking_heart_mine", Blocks.CREAKING_HEART, net.geforcemods.securitycraft.blocks.mines.CreakingHeartMineBlock::new);

		BOUND_MINES = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("bound_mines"), net.minecraft.core.component.DataComponentType.<net.geforcemods.securitycraft.components.GlobalPositions>builder().persistent(net.geforcemods.securitycraft.components.GlobalPositions.codec(net.geforcemods.securitycraft.items.MineRemoteAccessToolItem.MAX_MINES)).networkSynchronized(net.geforcemods.securitycraft.components.GlobalPositions.streamCodec(net.geforcemods.securitycraft.items.MineRemoteAccessToolItem.MAX_MINES)).cacheEncoding().build());
		MINE_REMOTE_ACCESS_TOOL = registerItem("remote_access_mine", new net.geforcemods.securitycraft.items.MineRemoteAccessToolItem(new Item.Properties().stacksTo(1).component(BOUND_MINES, net.geforcemods.securitycraft.items.MineRemoteAccessToolItem.DEFAULT_POSITIONS).setId(ResourceKey.create(Registries.ITEM, id("remote_access_mine")))));
		WIRE_CUTTERS = registerItem("wire_cutters", new net.geforcemods.securitycraft.items.WireCuttersItem(new Item.Properties().durability(476).setId(ResourceKey.create(Registries.ITEM, id("wire_cutters")))));

		UNIVERSAL_BLOCK_REINFORCER_LVL1 = registerConverterItem("universal_block_reinforcer_lvl1", 300, true);
		UNIVERSAL_BLOCK_REINFORCER_LVL2 = registerConverterItem("universal_block_reinforcer_lvl2", 2700, true);
		UNIVERSAL_BLOCK_REINFORCER_LVL3 = registerConverterItem("universal_block_reinforcer_lvl3", 0, true);
		UNIVERSAL_BLOCK_REMOVER = registerConverterItem("universal_block_remover", 476, false);

		UNREINFORCING = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("unreinforcing"), net.minecraft.core.component.DataComponentType.<net.minecraft.util.Unit>builder().persistent(com.mojang.serialization.Codec.unit(net.minecraft.util.Unit.INSTANCE)).networkSynchronized(net.minecraft.network.codec.StreamCodec.unit(net.minecraft.util.Unit.INSTANCE)).build());
		BLOCK_REINFORCING_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("block_reinforcing"), new net.minecraft.world.item.crafting.CustomRecipe.Serializer<>(net.geforcemods.securitycraft.recipe.ReinforcerRecipe.Reinforcing::new));
		BLOCK_UNREINFORCING_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("block_unreinforcing"), new net.minecraft.world.item.crafting.CustomRecipe.Serializer<>(net.geforcemods.securitycraft.recipe.ReinforcerRecipe.Unreinforcing::new));
		BLOCK_REINFORCER_MENU = Registry.register(BuiltInRegistries.MENU, id("block_reinforcer"), new net.minecraft.world.inventory.MenuType<>(net.geforcemods.securitycraft.inventory.BlockReinforcerMenu::new, net.minecraft.world.flag.FeatureFlags.VANILLA_SET));

		KEYPAD_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("keypad"), FabricBlockEntityTypeBuilder.create(KeypadBlockEntity::new, KEYPAD).build());
		LASER_BLOCK_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("laser_block"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.LaserBlockBlockEntity::new, LASER_BLOCK).build());
		//every reinforced block carries this block entity purely to remember its owner, so they all have to be
		//listed as valid for the type; REINFORCED_BLOCKS is already fully populated by this point in init()
		java.util.List<Block> abstractBeBlocks = new ArrayList<>(java.util.List.of(LASER_FIELD, STONE_MINE, DEEPSLATE_MINE, COBBLED_DEEPSLATE_MINE, DIRT_MINE, COBBLESTONE_MINE, SAND_MINE, GRAVEL_MINE, NETHERRACK_MINE, END_STONE_MINE, COAL_MINE, DEEPSLATE_COAL_MINE, IRON_MINE, DEEPSLATE_IRON_MINE, GOLD_MINE, DEEPSLATE_GOLD_MINE, COPPER_MINE, DEEPSLATE_COPPER_MINE, REDSTONE_MINE, DEEPSLATE_REDSTONE_MINE, EMERALD_MINE, DEEPSLATE_EMERALD_MINE, LAPIS_MINE, DEEPSLATE_LAPIS_MINE, DIAMOND_MINE, DEEPSLATE_DIAMOND_MINE, NETHER_GOLD_MINE, QUARTZ_MINE, ANCIENT_DEBRIS_MINE, GILDED_BLACKSTONE_MINE, FURNACE_MINE, SMOKER_MINE, BLAST_FURNACE_MINE));

		abstractBeBlocks.addAll(REINFORCED_BLOCKS);
		//the door has its own block entity type now; it stays listed here so doors placed by an older build,
		//whose saved block entity still says "abstract", keep loading with their owner intact
		abstractBeBlocks.add(REINFORCED_DOOR);
		ABSTRACT_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("abstract"), FabricBlockEntityTypeBuilder.create((pos, state) -> new net.geforcemods.securitycraft.api.OwnableBlockEntity(ABSTRACT_BLOCK_ENTITY, pos, state), abstractBeBlocks.toArray(new Block[0])).build());
		ELECTRIFIED_FENCE_AND_GATE_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("electrified_fence_and_gate"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.ElectrifiedFenceAndGateBlockEntity::new, ELECTRIFIED_IRON_FENCE, ELECTRIFIED_IRON_FENCE_GATE).build());
		REINFORCED_DOOR_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("reinforced_door"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.ReinforcedDoorBlockEntity::new, REINFORCED_DOOR).build());
		KEYPAD_CHEST_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("keypad_chest"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.KeypadChestBlockEntity::new, KEYPAD_CHEST).build());
		KEYPAD_BARREL_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("keypad_barrel"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.KeypadBarrelBlockEntity::new, KEYPAD_BARREL).build());
		KEYPAD_FURNACE_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("keypad_furnace"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.KeypadFurnaceBlockEntity::new, KEYPAD_FURNACE).build());
		KEYPAD_SMOKER_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("keypad_smoker"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.KeypadSmokerBlockEntity::new, KEYPAD_SMOKER).build());
		KEYPAD_BLAST_FURNACE_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("keypad_blast_furnace"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.KeypadBlastFurnaceBlockEntity::new, KEYPAD_BLAST_FURNACE).build());
		KEYPAD_FURNACE_MENU = Registry.register(BuiltInRegistries.MENU, id("keypad_furnace"), new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>((syncId, inv, buf) -> new net.geforcemods.securitycraft.inventory.KeypadFurnaceMenu(syncId, inv, buf)));
		KEYPAD_SMOKER_MENU = Registry.register(BuiltInRegistries.MENU, id("keypad_smoker"), new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>((syncId, inv, buf) -> new net.geforcemods.securitycraft.inventory.KeypadSmokerMenu(syncId, inv, buf)));
		KEYPAD_BLAST_FURNACE_MENU = Registry.register(BuiltInRegistries.MENU, id("keypad_blast_furnace"), new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>((syncId, inv, buf) -> new net.geforcemods.securitycraft.inventory.KeypadBlastFurnaceMenu(syncId, inv, buf)));
		REINFORCED_HOPPER_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("reinforced_hopper"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.ReinforcedHopperBlockEntity::new, REINFORCED_BY_NAME.get("reinforced_hopper")).build());
		REINFORCED_DISPENSER_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("reinforced_dispenser"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.ReinforcedDispenserBlockEntity::new, REINFORCED_BY_NAME.get("reinforced_dispenser")).build());
		REINFORCED_DROPPER_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("reinforced_dropper"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.ReinforcedDropperBlockEntity::new, REINFORCED_BY_NAME.get("reinforced_dropper")).build());
		REINFORCED_OBSERVER_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("reinforced_observer"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.ReinforcedObserverBlockEntity::new, REINFORCED_BY_NAME.get("reinforced_observer")).build());
		REINFORCED_MOVING_PISTON_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("reinforced_moving_piston"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.ReinforcedPistonMovingBlockEntity::new, REINFORCED_MOVING_PISTON).build());
		REINFORCED_CAULDRON_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("reinforced_cauldron"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.ReinforcedCauldronBlockEntity::new, REINFORCED_CAULDRON, REINFORCED_WATER_CAULDRON, REINFORCED_LAVA_CAULDRON, REINFORCED_POWDER_SNOW_CAULDRON).build());
		REINFORCED_LECTERN_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("reinforced_lectern"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.ReinforcedLecternBlockEntity::new, REINFORCED_LECTERN).build());
		REINFORCED_CHISELED_BOOKSHELF_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("reinforced_chiseled_bookshelf"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.ReinforcedChiseledBookshelfBlockEntity::new, REINFORCED_CHISELED_BOOKSHELF).build());
		REINFORCED_LECTERN_MENU = Registry.register(BuiltInRegistries.MENU, id("reinforced_lectern"), new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>((syncId, inv, buf) -> new net.geforcemods.securitycraft.inventory.ReinforcedLecternMenu(syncId, inv, buf)));
		SECRET_SIGN_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("secret_sign"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.SecretSignBlockEntity::new, SECRET_OAK_SIGN, SECRET_OAK_WALL_SIGN, SECRET_SPRUCE_SIGN, SECRET_SPRUCE_WALL_SIGN, SECRET_BIRCH_SIGN, SECRET_BIRCH_WALL_SIGN, SECRET_JUNGLE_SIGN, SECRET_JUNGLE_WALL_SIGN, SECRET_ACACIA_SIGN, SECRET_ACACIA_WALL_SIGN, SECRET_DARK_OAK_SIGN, SECRET_DARK_OAK_WALL_SIGN, SECRET_MANGROVE_SIGN, SECRET_MANGROVE_WALL_SIGN, SECRET_CHERRY_SIGN, SECRET_CHERRY_WALL_SIGN, SECRET_BAMBOO_SIGN, SECRET_BAMBOO_WALL_SIGN, SECRET_CRIMSON_SIGN, SECRET_CRIMSON_WALL_SIGN, SECRET_WARPED_SIGN, SECRET_WARPED_WALL_SIGN).build());
		SECRET_HANGING_SIGN_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("secret_hanging_sign"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.SecretHangingSignBlockEntity::new, SECRET_OAK_HANGING_SIGN, SECRET_OAK_WALL_HANGING_SIGN, SECRET_SPRUCE_HANGING_SIGN, SECRET_SPRUCE_WALL_HANGING_SIGN, SECRET_BIRCH_HANGING_SIGN, SECRET_BIRCH_WALL_HANGING_SIGN, SECRET_JUNGLE_HANGING_SIGN, SECRET_JUNGLE_WALL_HANGING_SIGN, SECRET_ACACIA_HANGING_SIGN, SECRET_ACACIA_WALL_HANGING_SIGN, SECRET_DARK_OAK_HANGING_SIGN, SECRET_DARK_OAK_WALL_HANGING_SIGN, SECRET_MANGROVE_HANGING_SIGN, SECRET_MANGROVE_WALL_HANGING_SIGN, SECRET_CHERRY_HANGING_SIGN, SECRET_CHERRY_WALL_HANGING_SIGN, SECRET_BAMBOO_HANGING_SIGN, SECRET_BAMBOO_WALL_HANGING_SIGN, SECRET_CRIMSON_HANGING_SIGN, SECRET_CRIMSON_WALL_HANGING_SIGN, SECRET_WARPED_HANGING_SIGN, SECRET_WARPED_WALL_HANGING_SIGN).build());
		MINE_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("mine"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.MineBlockEntity::new, MINE).build());
		BOUNCING_BETTY_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("bouncing_betty"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.BouncingBettyBlockEntity::new, BOUNCING_BETTY).build());
		CLAYMORE_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("claymore"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.ClaymoreBlockEntity::new, CLAYMORE).build());
		IMS_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("ims"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.IMSBlockEntity::new, IMS).build());
		TRACK_MINE_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("track_mine"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.TrackMineBlockEntity::new, TRACK_MINE).build());
		BRUSHABLE_MINE_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("brushable_mine"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.BrushableMineBlockEntity::new, SUSPICIOUS_SAND_MINE, SUSPICIOUS_GRAVEL_MINE).build());
		CREAKING_HEART_MINE_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("creaking_heart_mine"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.CreakingHeartMineBlockEntity::new, CREAKING_HEART_MINE).build());
		PORTABLE_RADAR_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("portable_radar"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.PortableRadarBlockEntity::new, PORTABLE_RADAR).build());
		BOUNCING_BETTY_ENTITY = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("bouncingbetty"), net.minecraft.world.entity.EntityType.Builder.<net.geforcemods.securitycraft.entity.BouncingBetty>of(net.geforcemods.securitycraft.entity.BouncingBetty::new, net.minecraft.world.entity.MobCategory.MISC).sized(0.5F, 0.2F).clientTrackingRange(8).updateInterval(1).build(ResourceKey.create(Registries.ENTITY_TYPE, id("bouncingbetty"))));
		IMS_BOMB_ENTITY = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("imsbomb"), net.minecraft.world.entity.EntityType.Builder.<net.geforcemods.securitycraft.entity.IMSBomb>of(net.geforcemods.securitycraft.entity.IMSBomb::new, net.minecraft.world.entity.MobCategory.MISC).sized(0.25F, 0.3F).clientTrackingRange(16).updateInterval(1).build(ResourceKey.create(Registries.ENTITY_TYPE, id("imsbomb"))));
		CUSTOMIZE_BLOCK_MENU = Registry.register(BuiltInRegistries.MENU, id("customize_block"), new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>((syncId, inv, pos) -> new net.geforcemods.securitycraft.inventory.CustomizeBlockMenu(syncId, inv.player.level(), pos, inv), net.minecraft.core.BlockPos.STREAM_CODEC));
		SINGLE_LENS_MENU = Registry.register(BuiltInRegistries.MENU, id("single_lens"), new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>((syncId, inv, pos) -> new net.geforcemods.securitycraft.inventory.SingleLensMenu(syncId, inv.player.level(), pos, inv), net.minecraft.core.BlockPos.STREAM_CODEC));
		COPY_MINE_REMOTE_ACCESS_TOOL_RECIPE_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("copy_mine_remote_access_tool_recipe"), new net.minecraft.world.item.crafting.CustomRecipe.Serializer<>(net.geforcemods.securitycraft.recipe.CopyPositionComponentItemRecipe::mineRemoteAccessTool));
		KEY_PANEL_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("key_panel"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.KeyPanelBlockEntity::new, KEY_PANEL).build());
		FRAME_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("keypad_frame"), FabricBlockEntityTypeBuilder.create(net.geforcemods.securitycraft.blockentities.FrameBlockEntity::new, FRAME).build());
		LASER_BLOCK_MENU = Registry.register(BuiltInRegistries.MENU, id("laser_block"), new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>((syncId, inv, data) -> new net.geforcemods.securitycraft.inventory.LaserBlockMenu(syncId, inv.player.level(), data.pos(), data.sideConfig(), inv), net.geforcemods.securitycraft.inventory.LaserBlockData.STREAM_CODEC));
		DISGUISE_MODULE_MENU = Registry.register(BuiltInRegistries.MENU, id("disguise_module"), new net.minecraft.world.inventory.MenuType<>(net.geforcemods.securitycraft.inventory.DisguiseModuleMenu::new, net.minecraft.world.flag.FeatureFlags.VANILLA_SET));
		net.geforcemods.securitycraft.misc.SCSounds.register();
		registerCreativeTab();
	}

	private static boolean isGlass(String name, String category) {
		return category.equals("glass") || (category.equals("pane") && name.contains("glass"));
	}

	private static boolean isCutout(String name, String category) {
		return category.equals("trapdoor") || name.equals("reinforced_iron_bars");
	}

	private static void registerReinforced(String name, String category) {
		//every shape uses its own reinforced subclass so the block carries an owner; see OwnershipUtils for why
		//this port keeps the vanilla class as the superclass instead of reimplementing it the way upstream does
		Block block = register(name, key -> {
			BlockBehaviour.Properties props = reinforcedProps(name, category).setId(key);

			return switch (category) {
				case "pillar" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedPillarBlock(props);
				case "glass" -> new BaseReinforcedBlock(props);
				case "pane" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedPaneBlock(props);
				case "fence" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedFenceBlock(props);
				case "fence_gate" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedFenceGateBlock(props, WoodType.OAK);
				case "wall" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedWallBlock(props);
				case "slab" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedSlabBlock(props);
				case "stairs" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedStairBlock(Blocks.STONE.defaultBlockState(), props);
				case "button" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedButtonBlock(props, BlockSetType.STONE, 20, false);
				case "pressure_plate" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedPressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, props, BlockSetType.STONE);
				case "trapdoor" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedTrapdoorBlock(BlockSetType.IRON, props);
				default -> new BaseReinforcedBlock(props);
			};
		});
		REINFORCED_BLOCKS.add(block);
		REINFORCED_BY_NAME.put(name, block);

		if (isGlass(name, category))
			GLASS_BLOCKS.add(block);

		if (isCutout(name, category))
			CUTOUT_BLOCKS.add(block);

		if (category.equals("pane") && name.contains("glass"))
			GLASS_PANE_BLOCKS.add(block);
	}

	/** Registers one of the {@link #REINFORCED_COPIES}: same shape class as vanilla, properties copied off it. */
	private static void registerReinforcedCopy(String name, Block vanilla, String category) {
		BlockBehaviour.Properties props = reinforcedCopy(vanilla);

		if (name.contains("crystal_quartz"))
			props.mapColor(MapColor.COLOR_CYAN);

		Block block = switch (category) {
			case "pillar" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedPillarBlock(props);
			case "stairs" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedStairBlock(Blocks.STONE.defaultBlockState(), props);
			case "slab" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedSlabBlock(props);
			case "carpet" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedCarpetBlock(props.forceSolidOn());
			case "glazed_terracotta" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedGlazedTerracottaBlock(props);
			case "redstone_lamp" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedRedstoneLampBlock(props);
			case "grass_block" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedGrassBlock(props);
			case "snowy_dirt" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedSnowyDirtBlock(props);
			case "cobweb" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedCobwebBlock(props);
			case "chain" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedChainBlock(props);
			case "end_rod" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedEndRodBlock(props);
			case "lantern" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedLanternBlock(props.pushReaction(PushReaction.BLOCK));
			case "ladder" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedLadderBlock(props.pushReaction(PushReaction.BLOCK));
			case "scaffolding" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedScaffoldingBlock(props.pushReaction(PushReaction.NORMAL));
			case "lever" -> new net.geforcemods.securitycraft.blocks.reinforced.ReinforcedLeverBlock(props.pushReaction(PushReaction.BLOCK).forceSolidOn());
			default -> new BaseReinforcedBlock(props);
		};

		//the scaffolding needs vanilla's own block item, which is what lets it be stacked downwards while held
		if (category.equals("scaffolding")) {
			registerBlockNoItem(name, block);
			Registry.register(BuiltInRegistries.ITEM, id(name), new net.minecraft.world.item.ScaffoldingBlockItem(block, new Item.Properties()));
		}
		else
			register(name, block);

		REINFORCED_BLOCKS.add(block);
		REINFORCED_BY_NAME.put(name, block);

		if (NON_SOLID_CATEGORIES.contains(category))
			CUTOUT_BLOCKS.add(block);
	}

	private static final java.util.Set<String> NON_SOLID_CATEGORIES = java.util.Set.of("cobweb", "chain", "end_rod", "lantern", "ladder", "scaffolding", "lever");

	/** A reinforced block that brings its own block entity, so it is not covered by either reinforced table. */
	private static void registerFunctionalReinforced(String name, Block block) {
		register(name, block);
		REINFORCED_BLOCKS.add(block);
		REINFORCED_BY_NAME.put(name, block);
	}

	/**
	 * The properties a reinforced block is registered with: copied straight off the vanilla block it reinforces,
	 * the way upstream's {@code reinforcedCopy} does it, so hardness, tool requirement, sound and light all match.
	 * Only when there is no vanilla counterpart does this fall back to the by-shape guess.
	 *
	 * <p>Upstream copies through Forge's {@code ofFullCopy}, which also carries the four state predicates vanilla's
	 * own {@code Properties#copy} leaves behind; those matter for glass, so they are re-applied here.
	 */
	private static BlockBehaviour.Properties reinforcedProps(String name, String category) {
		Block vanilla = BuiltInRegistries.BLOCK.getValue(ResourceLocation.withDefaultNamespace(name.substring("reinforced_".length())));

		if (vanilla == Blocks.AIR)
			return isGlass(name, category) ? glassProps() : category.equals("pane") ? paneProps() : shapeProps(name);

		BlockBehaviour.Properties props = reinforcedCopy(vanilla);

		if (isGlass(name, category))
			props.isValidSpawn((state, level, pos, type) -> false).isRedstoneConductor((state, level, pos) -> false).isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false);

		return props;
	}

	private static Block register(String name, java.util.function.Function<ResourceKey<Block>, Block> factory) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id(name));
		Block block = Registry.register(BuiltInRegistries.BLOCK, blockKey, factory.apply(blockKey));
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id(name));
		BlockItem item = Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey)));
		TAB_ITEMS.add(item);
		return block;
	}

	private static Block register(String name, Block block) {
		Registry.register(BuiltInRegistries.BLOCK, id(name), block);
		BlockItem item = new BlockItem(block, new Item.Properties());
		Registry.register(BuiltInRegistries.ITEM, id(name), item);
		TAB_ITEMS.add(item);
		return block;
	}

	private static Block registerBlockNoItem(String name, java.util.function.Function<ResourceKey<Block>, Block> factory) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id(name));
		return Registry.register(BuiltInRegistries.BLOCK, blockKey, factory.apply(blockKey));
	}

	private static void registerBlockItem(String name, Block block) {
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id(name));

		TAB_ITEMS.add(Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey))));
	}

	/** Registers a block mine disguised as the given vanilla block, copying that block's properties. */
	private static Block registerBlockMine(String name, Block disguisedAs, java.util.function.BiFunction<BlockBehaviour.Properties, Block, Block> factory) {
		return register(name, key -> factory.apply(reinforcedCopy(disguisedAs).setId(key), disguisedAs));
	}

	/** 1:1 with the upstream {@code SCContent#reinforcedCopy}. */
	private static BlockBehaviour.Properties reinforcedCopy(Block block) {
		return alwaysDrop(BlockBehaviour.Properties.ofFullCopy(block).explosionResistance(Float.MAX_VALUE));
	}

	/** 1:1 with the upstream {@code SCContent#prop(MapColor, float)}. */
	private static BlockBehaviour.Properties mineProp(net.minecraft.world.level.material.MapColor color, float hardness) {
		return alwaysDrop(BlockBehaviour.Properties.of().mapColor(color).strength(hardness, Float.MAX_VALUE));
	}

	private static void registerCreativeTab() {
		CreativeModeTab technical = FabricItemGroup.builder()
				.icon(() -> new ItemStack(KEYPAD))
				.title(Component.translatable("itemGroup.securitycraft.technical"))
				//upstream's SCCreativeModeTabs order, with the entries whose blocks this port does not have yet left out
				.displayItems((params, output) -> {
					output.accept(SC_MANUAL);
					output.accept(FRAME);
					output.accept(KEY_PANEL_ITEM);
					output.accept(KEYPAD);
					output.accept(KEYPAD_CHEST);
					output.accept(KEYPAD_BARREL);
					output.accept(KEYPAD_FURNACE);
					output.accept(KEYPAD_SMOKER);
					output.accept(KEYPAD_BLAST_FURNACE);
					output.accept(LASER_BLOCK);
					output.accept(PORTABLE_RADAR);
					output.accept(MINE_REMOTE_ACCESS_TOOL);
					output.accept(WIRE_CUTTERS);
					output.accept(ELECTRIFIED_IRON_FENCE);
					output.accept(ELECTRIFIED_IRON_FENCE_GATE);
					output.accept(REINFORCED_BY_NAME.get("reinforced_iron_trapdoor"));
					output.accept(REINFORCED_DOOR);
					output.accept(REINFORCED_BY_NAME.get("reinforced_dispenser"));
					output.accept(REINFORCED_BY_NAME.get("reinforced_dropper"));
					output.accept(REINFORCED_BY_NAME.get("reinforced_observer"));
					output.accept(REINFORCED_BY_NAME.get("reinforced_ladder"));
					output.accept(REINFORCED_BY_NAME.get("reinforced_hopper"));
					output.accept(LENS);
					output.accept(ALLOWLIST_MODULE);
					output.accept(DENYLIST_MODULE);
					output.accept(DISGUISE_MODULE);
					output.accept(REDSTONE_MODULE);
					output.accept(SPEED_MODULE);
					output.accept(SMART_MODULE);
					output.accept(STORAGE_MODULE);
					output.accept(HARMING_MODULE);
					output.accept(UNIVERSAL_BLOCK_MODIFIER);
					output.accept(UNIVERSAL_OWNER_CHANGER);
					output.accept(UNIVERSAL_BLOCK_REINFORCER_LVL1);
					output.accept(UNIVERSAL_BLOCK_REINFORCER_LVL2);
					output.accept(UNIVERSAL_BLOCK_REINFORCER_LVL3);
					output.accept(UNIVERSAL_BLOCK_REMOVER);
					output.accept(FAKE_WATER_BUCKET);
					output.accept(FAKE_LAVA_BUCKET);
				})
				.build();
		//Fabric's ItemGroupsMixin sorts every non-vanilla creative tab by its ResourceLocation and assigns the tab
		//positions from that order, so registration order has no effect. Upstream gets the order it wants from
		//NeoForge's withTabsBefore(); here the numeric prefixes are what put the tabs in upstream's order
		//(technical, explosives, decoration) instead of the alphabetical decoration/mine/technical.
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("1_technical"), technical);

		//upstream's MINE_TAB uses withTabsBefore(TECHNICAL_TAB). The 43 entries are upstream's resolved order: the four
		//non-block mines in declaration order, then the block mines sorted by the vanilla creative index of the block
		//they are disguised as, then the four entries upstream appends explicitly.
		CreativeModeTab mine = FabricItemGroup.builder()
				.icon(() -> new ItemStack(MINE))
				.title(Component.translatable("itemGroup.securitycraft.explosives"))
				.displayItems((params, output) -> {
					output.accept(MINE_REMOTE_ACCESS_TOOL);
					output.accept(WIRE_CUTTERS);
					output.accept(net.minecraft.world.item.Items.FLINT_AND_STEEL);
					output.accept(MINE);

					for (Block block : sortMineGroup(List.of(BOUNCING_BETTY, CLAYMORE, IMS, TRACK_MINE, STONE_MINE, DEEPSLATE_MINE, COBBLED_DEEPSLATE_MINE, DIRT_MINE, COBBLESTONE_MINE, SAND_MINE, GRAVEL_MINE, NETHERRACK_MINE, END_STONE_MINE, COAL_MINE, DEEPSLATE_COAL_MINE, IRON_MINE, DEEPSLATE_IRON_MINE, GOLD_MINE, DEEPSLATE_GOLD_MINE, COPPER_MINE, DEEPSLATE_COPPER_MINE, REDSTONE_MINE, DEEPSLATE_REDSTONE_MINE, EMERALD_MINE, DEEPSLATE_EMERALD_MINE, LAPIS_MINE, DEEPSLATE_LAPIS_MINE, DIAMOND_MINE, DEEPSLATE_DIAMOND_MINE, NETHER_GOLD_MINE, QUARTZ_MINE, GILDED_BLACKSTONE_MINE, SUSPICIOUS_SAND_MINE, SUSPICIOUS_GRAVEL_MINE, CREAKING_HEART_MINE)))
						output.accept(block);

					output.accept(ANCIENT_DEBRIS_MINE_ITEM);
					output.accept(FURNACE_MINE);
					output.accept(SMOKER_MINE);
					output.accept(BLAST_FURNACE_MINE);
				})
				.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("2_mine"), mine);

		CreativeModeTab decoration = FabricItemGroup.builder()
				.icon(() -> new ItemStack(REINFORCED_BY_NAME.getOrDefault("reinforced_oak_stairs", KEYPAD)))
				.title(Component.translatable("itemGroup.securitycraft.decoration"))
				.displayItems((params, output) -> {
					output.accept(UNIVERSAL_BLOCK_REINFORCER_LVL1);
					output.accept(UNIVERSAL_BLOCK_REINFORCER_LVL2);
					output.accept(UNIVERSAL_BLOCK_REINFORCER_LVL3);
					output.accept(UNIVERSAL_BLOCK_REMOVER);

					//upstream keeps the iron trapdoor out of the sorted run (its item group is MANUAL) and appends
					//it, the door and the two electrified blocks at the end of the tab instead
					for (Block block : sortByVanillaOrder(REINFORCED_BLOCKS)) {
						if (block != REINFORCED_BY_NAME.get("reinforced_iron_trapdoor"))
							output.accept(block);

						//the original slots the secret signs in right behind the reinforced lectern, which is what puts
						//them ahead of everything that sorts by the vanilla redstone tab - the pistons, lever and so on
						if (block == REINFORCED_LECTERN) {
							for (ItemLike sign : SECRET_SIGN_ITEMS)
								output.accept(sign);
						}
					}

					output.accept(ELECTRIFIED_IRON_FENCE);
					output.accept(ELECTRIFIED_IRON_FENCE_GATE);
					output.accept(REINFORCED_BY_NAME.get("reinforced_iron_trapdoor"));
					output.accept(REINFORCED_DOOR);
				})
				.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("3_decoration"), decoration);
	}

	/**
	 * Upstream's explosives-tab comparator: everything that is not a block mine keeps its declaration order and comes
	 * first, then the block mines follow, ordered by the vanilla creative index of the block they are disguised as.
	 */
	private static List<Block> sortMineGroup(List<Block> blocks) {
		Map<Item, Integer> index = vanillaOrderIndex();
		List<Block> sorted = new ArrayList<>(blocks);

		sorted.sort(java.util.Comparator.comparingInt(block -> {
			if (!(block instanceof net.geforcemods.securitycraft.api.IBlockMine blockMine))
				return -1;

			Integer i = index.get(blockMine.getBlockDisguisedAs().asItem());

			return i == null ? Integer.MAX_VALUE : i.intValue();
		}));
		return sorted;
	}

	private static Map<Item, Integer> vanillaOrderIndex() {
		List<Item> vanillaOrder = new ArrayList<>();

		for (ResourceKey<CreativeModeTab> key : List.of(CreativeModeTabs.BUILDING_BLOCKS, CreativeModeTabs.COLORED_BLOCKS, CreativeModeTabs.NATURAL_BLOCKS, CreativeModeTabs.FUNCTIONAL_BLOCKS, CreativeModeTabs.REDSTONE_BLOCKS)) {
			CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(key);

			if (tab != null)
				for (ItemStack stack : tab.getDisplayItems())
					vanillaOrder.add(stack.getItem());
		}

		Map<Item, Integer> index = new HashMap<>();

		for (int i = 0; i < vanillaOrder.size(); i++)
			index.putIfAbsent(vanillaOrder.get(i), i);

		return index;
	}

	private static List<Block> sortByVanillaOrder(List<Block> blocks) {
		List<Item> vanillaOrder = new ArrayList<>();

		for (ResourceKey<CreativeModeTab> key : List.of(CreativeModeTabs.BUILDING_BLOCKS, CreativeModeTabs.COLORED_BLOCKS, CreativeModeTabs.NATURAL_BLOCKS, CreativeModeTabs.FUNCTIONAL_BLOCKS, CreativeModeTabs.REDSTONE_BLOCKS)) {
			CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(key);

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

	/**
	 * The handful of reinforced blocks whose plain counterpart is not simply their own name minus the prefix. The
	 * crystal quartz set is the mod's own, so upstream hands the counterpart to the constructor; this port derives
	 * it from the name everywhere else, which needs these two spelled out.
	 */
	private static final Map<String, String> COUNTERPART_EXCEPTIONS = Map.of(
			"reinforced_crystal_quartz_block", "crystal_quartz",
			"reinforced_chiseled_crystal_quartz_block", "chiseled_crystal_quartz",
			"reinforced_grass_path", "dirt_path",
			"reinforced_hardened_clay", "terracotta",
			"reinforced_normal_stone_slab", "stone_slab",
			"reinforced_stone_slab", "smooth_stone_slab");

	public static Block vanillaCounterpart(Block reinforced) {
		ResourceLocation loc = BuiltInRegistries.BLOCK.getKey(reinforced);

		if (!loc.getNamespace().equals(SecurityCraft.MODID) || !loc.getPath().startsWith("reinforced_"))
			return null;

		String path = COUNTERPART_EXCEPTIONS.getOrDefault(loc.getPath(), loc.getPath().substring("reinforced_".length()));
		//the crystal quartz set is this mod's own, so its plain form lives in this namespace rather than vanilla's
		Block counterpart = BuiltInRegistries.BLOCK.getValue(id(path));

		if (counterpart == Blocks.AIR)
			counterpart = BuiltInRegistries.BLOCK.getValue(ResourceLocation.fromNamespaceAndPath("minecraft", path));

		return counterpart == Blocks.AIR ? null : counterpart;
	}

	public static Block reinforcedCounterpart(Block plain) {
		ResourceLocation loc = BuiltInRegistries.BLOCK.getKey(plain);

		if (!loc.getNamespace().equals("minecraft") && !loc.getNamespace().equals(SecurityCraft.MODID))
			return null;

		for (Map.Entry<String, String> exception : COUNTERPART_EXCEPTIONS.entrySet()) {
			if (exception.getValue().equals(loc.getPath()))
				return REINFORCED_BY_NAME.get(exception.getKey());
		}

		return REINFORCED_BY_NAME.get("reinforced_" + loc.getPath());
	}

	private static Item registerItem(String name, Item item) {
		Registry.register(BuiltInRegistries.ITEM, id(name), item);
		return item;
	}

	private static net.geforcemods.securitycraft.items.ModuleItem registerModule(String name, net.geforcemods.securitycraft.misc.ModuleType type, boolean containsCustomData, boolean canBeCustomized, boolean hasListData) {
		Item.Properties props = new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, id(name)));

		if (hasListData)
			props.component(LIST_MODULE_DATA, net.geforcemods.securitycraft.components.ListModuleData.EMPTY);

		if (type == net.geforcemods.securitycraft.misc.ModuleType.DISGUISE)
			props.component(net.minecraft.core.component.DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.EMPTY);

		net.geforcemods.securitycraft.items.ModuleItem item = new net.geforcemods.securitycraft.items.ModuleItem(props, type, containsCustomData, canBeCustomized);

		Registry.register(BuiltInRegistries.ITEM, id(name), item);
		TAB_ITEMS.add(item);
		return item;
	}

	private static Item registerConverterItem(String name, int durability, boolean reinforcing) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(name));
		Item.Properties props = new Item.Properties().setId(key);

		if (durability > 0)
			props.durability(durability);

		Item item = new BlockReinforcerItem(props, reinforcing);
		Registry.register(BuiltInRegistries.ITEM, key, item);
		return item;
	}


	private static boolean isWood(String name) {
		return name.matches(".*(oak|spruce|birch|jungle|acacia|mangrove|cherry|bamboo|crimson|warped|planks|mosaic).*");
	}

	/** 1:1 with upstream: the furnace family lights up at level 13 while it is smelting. */
	private static BlockBehaviour.Properties keypadFurnaceProps() {
		return alwaysDrop(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, Float.MAX_VALUE).sound(SoundType.METAL)).lightLevel(state -> state.getValue(net.geforcemods.securitycraft.blocks.AbstractKeypadFurnaceBlock.LIT) ? 13 : 0);
	}

	/**
	 * Clears the "needs the right tool to drop" flag. Upstream's {@code always_drop} config defaults to true and is
	 * honoured by overriding Forge's {@code canHarvestBlock}, so every SecurityCraft block drops itself no matter
	 * what it was mined with - and, because vanilla reads the same flag to pick the mining-speed penalty, breaks at
	 * the same speed as if the right tool were held. Fabric has no such hook, so the flag is cleared here instead.
	 */
	private static BlockBehaviour.Properties alwaysDrop(BlockBehaviour.Properties props) {
		if (ConfigHandler.alwaysDrop)
			props.requiresCorrectToolForDrops = false;

		return props;
	}

	/** The crystal quartz set is vanilla quartz in cyan, so each block copies the quartz block it is shaped after. */
	private static BlockBehaviour.Properties crystalQuartzProps(Block quartzBlock) {
		return alwaysDrop(BlockBehaviour.Properties.copy(quartzBlock).mapColor(MapColor.COLOR_CYAN));
	}

	private static BlockBehaviour.Properties shapeProps(String name) {
		boolean wood = isWood(name);
		boolean wool = name.contains("wool");
		BlockBehaviour.Properties p = BlockBehaviour.Properties.of().strength(wood || wool ? 2.0F : 5.0F, 12000.0F).sound(wood ? SoundType.WOOD : wool ? SoundType.WOOL : SoundType.STONE);

		return alwaysDrop(p);
	}

	private static BlockBehaviour.Properties glassProps() {
		return alwaysDrop(BlockBehaviour.Properties.of().strength(1.5F, 12000.0F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((state, level, pos, type) -> false).isRedstoneConductor((state, level, pos) -> false).isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false));
	}

	private static BlockBehaviour.Properties paneProps() {
		return alwaysDrop(BlockBehaviour.Properties.of().strength(5.0F, 12000.0F).sound(SoundType.METAL).noOcclusion());
	}
}
