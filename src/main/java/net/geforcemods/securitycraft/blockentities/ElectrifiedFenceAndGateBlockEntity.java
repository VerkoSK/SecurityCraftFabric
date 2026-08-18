package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The electrified iron fence's and fence gate's block entity: it holds the owner and the one module they take,
 * an allowlist that spares the listed players from being shocked. 1:1 with upstream's block entity of the same
 * name, which reaches this shape through its shared {@code AllowlistOnlyBlockEntity} (no other block in this
 * port needs that base, so it is folded in here).
 */
public class ElectrifiedFenceAndGateBlockEntity extends CustomizableBlockEntity {
	public ElectrifiedFenceAndGateBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.ELECTRIFIED_FENCE_AND_GATE_BLOCK_ENTITY, pos, state);
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[0];
	}

	@Override
	public String getModuleDescriptionId(String denotation, ModuleType module) {
		return "module.generic.electrified_fence_and_gate.whitelist_module.description";
	}
}
