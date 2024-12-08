package com.tristankechlo.improvedvanilla.commands;


import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.util.ConfigManager;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public final class ResponseHelper {

    public static void sendMessageConfigShow(CommandSource source) {
        ITextComponent clickableFile = clickableConfig();
        ITextComponent message = new StringTextComponent("Config-file can be found here: ").append(clickableFile);
        sendMessage(source, message.withStyle(TextFormatting.WHITE), false);
    }

    public static void sendMessageConfigReload(CommandSource source) {
        ITextComponent message = new StringTextComponent("Config was successfully reloaded.");
        sendMessage(source, message.withStyle(TextFormatting.WHITE), true);
    }

    public static void sendMessageConfigReset(CommandSource source) {
        ITextComponent message = new StringTextComponent("Config was successfully set to default.");
        sendMessage(source, message.withStyle(TextFormatting.WHITE), true);
    }

    public static ITextComponent start() {
        return new StringTextComponent("[" + ImprovedVanilla.MOD_NAME + "] ").withStyle(TextFormatting.GOLD);
    }

    public static void sendMessage(CommandSource source, ITextComponent message, boolean broadcastToOps) {
        ITextComponent start = start().append(message);
        source.sendSuccess(start, broadcastToOps);
    }

    public static ITextComponent clickableConfig() {
        String fileName = ConfigManager.FILE_NAME;
        String filePath = ConfigManager.getConfigPath();
        ITextComponent mutableComponent = new StringTextComponent(fileName);
        mutableComponent.withStyle(TextFormatting.GREEN, TextFormatting.UNDERLINE);
        mutableComponent.withStyle(style -> style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, filePath)));
        return mutableComponent;
    }

    public static ITextComponent clickableLink(String url, String displayText) {
        ITextComponent mutableComponent = new StringTextComponent(displayText);
        mutableComponent.withStyle(TextFormatting.GREEN, TextFormatting.UNDERLINE);
        mutableComponent.withStyle(style -> style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
        return mutableComponent;
    }

    public static ITextComponent clickableLink(String url) {
        return clickableLink(url, url);
    }

}
