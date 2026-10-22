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
				.executes(SCCommand::info)
				.then(Commands.literal("help").executes(SCCommand::help))
				.then(Commands.literal("version").executes(SCCommand::version)));
	}

	private static int info(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSuccess(() -> Component.literal("§6§lSecurityCraft §r§e(Fabric Port by §b" + SecurityCraft.PORT_AUTHOR + "§e) §7v" + SecurityCraft.VERSION), false);
		ctx.getSource().sendSuccess(() -> Component.literal("§7Source: §f" + SecurityCraft.PORT_REPO), false);
		ctx.getSource().sendSuccess(() -> Component.translatable("commands.securitycraft:help"), false);
		return 1;
	}

	private static int help(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSuccess(() -> Component.literal("§6SecurityCraft §7v" + SecurityCraft.VERSION + " §e(Fabric Port by §b" + SecurityCraft.PORT_AUTHOR + "§e)"), false);
		ctx.getSource().sendSuccess(() -> Component.translatable("commands.securitycraft:help"), false);
		return 1;
	}

	private static int version(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSuccess(() -> Component.literal("§6SecurityCraft §eFabric Port §7v" + SecurityCraft.VERSION + " §aby §b" + SecurityCraft.PORT_AUTHOR), false);
		ctx.getSource().sendSuccess(() -> Component.literal("§7Repository: §f" + SecurityCraft.PORT_REPO), false);
		ctx.getSource().sendSuccess(() -> Component.literal("§7Original Forge mod by Geforce, bl4ckscor3, Redstone_Dubstep, ChainmailPickaxe"), false);
		return 1;
	}
}
