package com.tristankechlo.improvedvanilla.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.tristankechlo.improvedvanilla.Constants;
import com.tristankechlo.improvedvanilla.config.util.ConfigManager;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public class ImprovedVanillaCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = literal(Constants.MOD_ID)
                .then(literal("config").requires((source) -> source.hasPermission(3))
                        .then(literal("reload").executes(ImprovedVanillaCommand::configReload))
                        .then(literal("show").executes(ImprovedVanillaCommand::configShow))
                        .then(literal("reset").executes(ImprovedVanillaCommand::configReset)));
        dispatcher.register(command);
        Constants.LOGGER.info("Command '/{}' registered", Constants.MOD_ID);
    }

    private static int configReload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ConfigManager.reloadConfig();
        ResponseHelper.sendMessageConfigReload(source);
        return 1;
    }

    private static int configShow(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ResponseHelper.sendMessageConfigShow(source);
        return 1;
    }

    private static int configReset(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ConfigManager.resetConfig();
        ResponseHelper.sendMessageConfigReset(source);
        return 1;
    }


}
