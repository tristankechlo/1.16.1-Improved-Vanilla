package com.tristankechlo.improvedvanilla.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.util.ConfigManager;
import net.minecraft.command.CommandSource;

import static net.minecraft.command.Commands.literal;


public final class ImprovedVanillaCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> command = literal(ImprovedVanilla.MOD_ID)
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

    private static int configReload(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        ConfigManager.reloadConfig();
        ResponseHelper.sendMessageConfigReload(source);
        return 1;
    }

    private static int configShow(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        ResponseHelper.sendMessageConfigShow(source);
        return 1;
    }

    private static int configReset(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        ConfigManager.resetConfig();
        ResponseHelper.sendMessageConfigReset(source);
        return 1;
    }

}
