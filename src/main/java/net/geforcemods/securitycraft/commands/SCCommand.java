package net.geforcemods.securitycraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.geforcemods.securitycraft.SecurityCraft;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/** The {@code /securitycraft} command. Slice port: {@code help} and {@code version} subcommands. */
public final class SCCommand {
	private SCCommand() {}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("securitycraft")
				.executes(SCCommand::help)
				.then(CommandManager.literal("help").executes(SCCommand::help))
				.then(CommandManager.literal("version").executes(SCCommand::version)));
	}

	private static int help(CommandContext<ServerCommandSource> ctx) {
		ctx.getSource().sendFeedback(() -> Text.translatable("commands.securitycraft:help"), false);
		return 1;
	}

	private static int version(CommandContext<ServerCommandSource> ctx) {
		ctx.getSource().sendFeedback(() -> Text.literal("SecurityCraft (Fabric port) v" + SecurityCraft.VERSION), false);
		return 1;
	}
}
