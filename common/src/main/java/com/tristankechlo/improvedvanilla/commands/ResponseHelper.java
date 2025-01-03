package com.tristankechlo.improvedvanilla.commands;


import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ResponseHelper {

    public static void sendMessageConfigShow(CommandSourceStack source) {
        MutableComponent clickableFile = clickableConfig();
        MutableComponent message = Component.literal("Config-file can be found here: ").append(clickableFile);
        sendMessage(source, message.withStyle(ChatFormatting.WHITE), false);
    }

    public static void sendMessageConfigReload(CommandSourceStack source, boolean success) {
        String text = success ? "Config was successfully reloaded." : "Error while reloading config. Check the logs for further details.";
        MutableComponent message = Component.literal(text).withStyle(ChatFormatting.WHITE);
        sendMessage(source, message.withStyle(ChatFormatting.WHITE), true);
    }

    public static void sendMessageConfigReset(CommandSourceStack source, boolean success) {
        String text = success ? "Config was successfully reset." : "Error while saving the default config.";
        MutableComponent message = Component.literal(text).withStyle(ChatFormatting.WHITE);
        sendMessage(source, message, true);
    }

    public static MutableComponent start() {
        return Component.literal("[" + ImprovedVanilla.MOD_NAME + "] ").withStyle(ChatFormatting.GOLD);
    }

    public static void sendMessage(CommandSourceStack source, MutableComponent message, boolean broadcastToOps) {
        MutableComponent start = start().append(message);
        source.sendSuccess(() -> start, broadcastToOps);
    }

    public static MutableComponent clickableConfig() {
        String fileName = ConfigManager.FILE_NAME;
        String filePath = ConfigManager.getConfigPath();
        MutableComponent mutableComponent = Component.literal(fileName);
        mutableComponent.withStyle(ChatFormatting.GREEN, ChatFormatting.UNDERLINE);
        mutableComponent.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, filePath)));
        return mutableComponent;
    }

    public static MutableComponent clickableLink(String url, String displayText) {
        MutableComponent mutableComponent = Component.literal(displayText);
        mutableComponent.withStyle(ChatFormatting.GREEN, ChatFormatting.UNDERLINE);
        mutableComponent.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
        return mutableComponent;
    }

}
