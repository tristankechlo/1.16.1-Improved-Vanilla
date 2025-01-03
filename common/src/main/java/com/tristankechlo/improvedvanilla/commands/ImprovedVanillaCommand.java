package com.tristankechlo.improvedvanilla.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ConfigManager;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public final class ImprovedVanillaCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = literal(ImprovedVanilla.MOD_ID)
                .then(literal("config").requires((source) -> source.hasPermission(3))
                        .then(literal("reload").executes(ImprovedVanillaCommand::configReload))
                        .then(literal("show").executes(ImprovedVanillaCommand::configShow))
                        .then(literal("reset").executes(ImprovedVanillaCommand::configReset)))
                .then(literal("github").executes(ProjectLinks.GITHUB::execute))
                .then(literal("issue").executes(ProjectLinks.ISSUE::execute))
                .then(literal("wiki").executes(ProjectLinks.WIKI::execute))
                .then(literal("discord").executes(ProjectLinks.DISCORD::execute))
                .then(literal("curseforge").executes(ProjectLinks.CURSEFORGE::execute))
                .then(literal("modrinth").executes(ProjectLinks.MODRINTH::execute));
        dispatcher.register(command);
        ImprovedVanilla.LOGGER.info("Command '/{}' registered", ImprovedVanilla.MOD_ID);
    }

    private static int configReload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        boolean success = ConfigManager.reloadConfig();
        ResponseHelper.sendMessageConfigReload(source, success);
        return 1;
    }

    private static int configShow(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ResponseHelper.sendMessageConfigShow(source);
        return 1;
    }

    private static int configReset(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        boolean success = ConfigManager.resetConfig();
        ResponseHelper.sendMessageConfigReset(source, success);
        return 1;
    }

}
