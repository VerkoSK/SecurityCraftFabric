package net.geforcemods.securitycraft.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.geforcemods.securitycraft.SecurityCraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** The {@code /securitycraft} command. Slice port: {@code help} and {@code version} subcommands. */
public final class SCCommand {
	private SCCommand() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("securitycraft")
				.executes(SCCommand::help)
				.then(Commands.literal("help").executes(SCCommand::help))
				.then(Commands.literal("version").executes(SCCommand::version)));
	}

	private static int help(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSuccess(() -> Component.translatable("commands.securitycraft:help"), false);
		return 1;
	}

	private static int version(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSuccess(() -> Component.literal("SecurityCraft (Fabric port) v" + SecurityCraft.VERSION), false);
		return 1;
	}
}
