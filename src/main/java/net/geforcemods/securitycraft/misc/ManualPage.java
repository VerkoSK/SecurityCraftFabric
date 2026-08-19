package net.geforcemods.securitycraft.misc;

import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedButtonBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedFenceGateBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedPressurePlateBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedTrapdoorBlock;
import net.geforcemods.securitycraft.items.SCManualItem;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * Builds {@link SCManualItem#PAGES} explicitly, in upstream's manual order. Upstream instead reflects over
 * {@code @HasManualPage}-annotated {@code SCContent} fields (see {@code SecurityCraft#collectSCContentData}); this
 * port's {@code SCContent} carries no such annotations, so the page list is spelled out here by hand, limited to the
 * content this port actually registers.
 * <p>
 * Dropped versus upstream because the port lacks the underlying content: keycards, display cases, secret signs
 * (standing and hanging), security sea boats, and every reinforced-block manual page that upstream documents
 * individually (alarm, block change detector, block pocket manager/wall, cage trap, floor trap, inventory scanner
 * field, keycard lock/reader, keypad barrel/door/trapdoor/chest/furnace/smoker/blast furnace, motion activated
 * light, panic button, projector, scanner door, secure redstone interface, secure trading station, security camera,
 * trophy system, username logger, reinforced hopper, reinforced chiseled bookshelf, briefcase, camera monitor,
 * codebreaker, incognito mask, keycard holder, keypad door item, limited use keycard, portable tune player,
 * reinforced door item, rift stabilizer, scanner door item, sentry (+ remote access tool), taser, universal key
 * changer, admin tool). {@code LASER_FIELD} is also dropped: this port registers it with {@code registerBlockNoItem},
 * so it has no {@link Item} to build a page around.
 */
public class ManualPage {
	private ManualPage() {}

	public static void register() {
		//individual pages, in upstream's field-declaration order
		add(SCContent.BOUNCING_BETTY.asItem());
		add(SCContent.CLAYMORE.asItem());
		add(SCContent.FRAME.asItem());
		add(SCContent.IMS.asItem());
		add(SCContent.KEY_PANEL.asItem());
		add(SCContent.KEYPAD.asItem(), "", true);
		add(SCContent.KEYPAD_CHEST.asItem(), "", true);
		add(SCContent.KEYPAD_BARREL.asItem(), "", true);
		add(SCContent.KEYPAD_FURNACE.asItem(), "", true);
		add(SCContent.KEYPAD_SMOKER.asItem(), "", true);
		add(SCContent.KEYPAD_BLAST_FURNACE.asItem(), "", true);
		add(SCContent.ELECTRIFIED_IRON_FENCE.asItem());
		add(SCContent.REINFORCED_BY_NAME.get("reinforced_hopper").asItem());
		add(SCContent.PORTABLE_RADAR.asItem());
		add(SCContent.REINFORCED_DOOR.asItem());
		add(SCContent.ELECTRIFIED_IRON_FENCE_GATE.asItem());
		add(SCContent.TRACK_MINE.asItem());
		add(SCContent.REINFORCED_BY_NAME.get("reinforced_lever").asItem());
		add(SCContent.REINFORCED_BY_NAME.get("reinforced_iron_trapdoor").asItem(), "", true);
		add(SCContent.FAKE_LAVA_BUCKET, "", true);
		add(SCContent.FAKE_WATER_BUCKET, "", true);
		add(SCContent.KEY_PANEL_ITEM);
		add(SCContent.LENS);
		add(SCContent.MINE_REMOTE_ACCESS_TOOL);
		add(SCContent.SC_MANUAL);
		add(SCContent.UNIVERSAL_BLOCK_MODIFIER);
		add(SCContent.UNIVERSAL_BLOCK_REMOVER);
		add(SCContent.UNIVERSAL_OWNER_CHANGER);
		add(SCContent.WIRE_CUTTERS);
		add(SCContent.DENYLIST_MODULE);
		add(SCContent.DISGUISE_MODULE);
		add(SCContent.HARMING_MODULE);
		add(SCContent.REDSTONE_MODULE);
		add(SCContent.SMART_MODULE);
		add(SCContent.STORAGE_MODULE);
		add(SCContent.ALLOWLIST_MODULE);
		add(SCContent.SPEED_MODULE);

		//block mines, minus the furnace mines and the track mine (which has its own individual page above)
		addGroup(PageGroup.BLOCK_MINES, List.of(
				SCContent.STONE_MINE, SCContent.DEEPSLATE_MINE, SCContent.COBBLED_DEEPSLATE_MINE, SCContent.DIRT_MINE, SCContent.COBBLESTONE_MINE,
				SCContent.SAND_MINE, SCContent.GRAVEL_MINE, SCContent.NETHERRACK_MINE, SCContent.END_STONE_MINE, SCContent.COAL_MINE,
				SCContent.DEEPSLATE_COAL_MINE, SCContent.IRON_MINE, SCContent.DEEPSLATE_IRON_MINE, SCContent.GOLD_MINE, SCContent.DEEPSLATE_GOLD_MINE,
				SCContent.COPPER_MINE, SCContent.DEEPSLATE_COPPER_MINE, SCContent.REDSTONE_MINE, SCContent.DEEPSLATE_REDSTONE_MINE, SCContent.EMERALD_MINE,
				SCContent.DEEPSLATE_EMERALD_MINE, SCContent.LAPIS_MINE, SCContent.DEEPSLATE_LAPIS_MINE, SCContent.DIAMOND_MINE, SCContent.DEEPSLATE_DIAMOND_MINE,
				SCContent.NETHER_GOLD_MINE, SCContent.QUARTZ_MINE, SCContent.ANCIENT_DEBRIS_MINE, SCContent.GILDED_BLACKSTONE_MINE,
				SCContent.SUSPICIOUS_SAND_MINE, SCContent.SUSPICIOUS_GRAVEL_MINE));
		addGroup(PageGroup.FURNACE_MINES, List.of(SCContent.FURNACE_MINE, SCContent.SMOKER_MINE, SCContent.BLAST_FURNACE_MINE));

		//the universal block reinforcer's 3 levels
		addGroupItems(PageGroup.BLOCK_REINFORCERS, List.of(SCContent.UNIVERSAL_BLOCK_REINFORCER_LVL1, SCContent.UNIVERSAL_BLOCK_REINFORCER_LVL2, SCContent.UNIVERSAL_BLOCK_REINFORCER_LVL3));

		//the shapes that get their own dedicated group upstream instead of falling under the generic REINFORCED page
		//(reinforced_lever and reinforced_iron_trapdoor are excluded too: those got individual pages above)
		addGroup(PageGroup.BUTTONS, SCContent.REINFORCED_BLOCKS.stream().filter(ReinforcedButtonBlock.class::isInstance).toList());
		addGroup(PageGroup.PRESSURE_PLATES, SCContent.REINFORCED_BLOCKS.stream().filter(ReinforcedPressurePlateBlock.class::isInstance).toList());
		addGroup(PageGroup.FENCE_GATES, SCContent.REINFORCED_BLOCKS.stream().filter(ReinforcedFenceGateBlock.class::isInstance).toList());
		addGroup(PageGroup.REINFORCED, SCContent.REINFORCED_BLOCKS.stream()
				.filter(block -> block != SCContent.REINFORCED_BY_NAME.get("reinforced_lever"))
				.filter(block -> block != SCContent.REINFORCED_BY_NAME.get("reinforced_hopper"))
				.filter(block -> !(block instanceof ReinforcedButtonBlock || block instanceof ReinforcedPressurePlateBlock || block instanceof ReinforcedFenceGateBlock || block instanceof ReinforcedTrapdoorBlock))
				.toList());
	}

	private static void add(Item item) {
		add(item, "", false);
	}

	private static void add(Item item, String designedBy, boolean hasRecipeDescription) {
		SCManualItem.PAGES.add(new SCManualPage(item, PageGroup.NONE, title(item), helpInfo(item.getDescriptionId()), designedBy, hasRecipeDescription));
	}

	private static void addGroup(PageGroup group, List<Block> blocks) {
		addGroupItems(group, blocks.stream().map(Block::asItem).toList());
	}

	private static void addGroupItems(PageGroup group, List<Item> items) {
		if (items.isEmpty())
			return;

		group.setItems(Ingredient.of(items.stream().map(ItemStack::new)));
		SCManualItem.PAGES.add(new SCManualPage(items.get(0), group, Utils.localize(group.getTitle()), Utils.localize("help." + group.getSpecialInfoKey()), "", false));
	}

	private static Component title(Item item) {
		return Utils.localize(item.getDescriptionId());
	}

	//mirrors upstream's SecurityCraft#collectSCContentData key derivation: "help." + descriptionId.substring(5) + ".info"
	private static Component helpInfo(String descriptionId) {
		return Utils.localize(("help." + descriptionId.substring(5) + ".info").replace("..", "."));
	}
}
