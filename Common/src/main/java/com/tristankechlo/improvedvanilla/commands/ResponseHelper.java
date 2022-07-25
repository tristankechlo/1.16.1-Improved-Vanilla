package com.tristankechlo.improvedvanilla.commands;

import com.tristankechlo.improvedvanilla.Constants;
import com.tristankechlo.improvedvanilla.config.util.ConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ResponseHelper {

    public static void sendMessageConfigShow(CommandSourceStack source) {
        MutableComponent clickableFile = clickableConfig();
        MutableComponent message = Component.translatable("commands.improvedvanilla.config.show", clickableFile);
        sendMessage(source, message.withStyle(ChatFormatting.WHITE), false);
    }

    public static void sendMessageConfigReload(CommandSourceStack source) {
        MutableComponent message = Component.translatable("commands.improvedvanilla.config.reload");
        sendMessage(source, message.withStyle(ChatFormatting.WHITE), true);
    }

    public static void sendMessageConfigReset(CommandSourceStack source) {
        MutableComponent message = Component.translatable("commands.improvedvanilla.config.reset");
        sendMessage(source, message.withStyle(ChatFormatting.WHITE), true);
    }

    public static MutableComponent start() {
        return Component.literal("[" + Constants.MOD_NAME + "] ").withStyle(ChatFormatting.GOLD);
    }

    public static void sendMessage(CommandSourceStack source, Component message, boolean broadcastToOps) {
        MutableComponent start = start().append(message);
        source.sendSuccess(start, broadcastToOps);
    }

    public static MutableComponent clickableConfig() {
        String fileName = ConfigManager.FILE_NAME;
        String filePath = ConfigManager.getConfigPath();
        MutableComponent mutableComponent = Component.literal(fileName);
        mutableComponent.withStyle(ChatFormatting.GREEN, ChatFormatting.UNDERLINE);
        mutableComponent.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, filePath)));
        return mutableComponent;
    }

}