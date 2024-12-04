package com.tristankechlo.improvedvanilla.commands;

import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.config.ConfigManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public final class ResponseHelper {

    public static void sendMessageConfigShow(ICommandSender source) {
        ITextComponent clickableFile = clickableConfig();
        ITextComponent message = new TextComponentString("Config-file can be found here: ");
        message.getStyle().setColor(TextFormatting.WHITE);
        message.appendSibling(clickableFile);
        source.sendMessage(start().appendSibling(message));
    }

    public static void sendMessageConfigReload(ICommandSender source) {
        ITextComponent message = new TextComponentString("Config was successfully reloaded.");
        message.getStyle().setColor(TextFormatting.WHITE);
        source.sendMessage(start().appendSibling(message));
    }

    public static void sendMessageConfigReset(ICommandSender source) {
        ITextComponent message = new TextComponentString("Config was successfully set to default.");
        message.getStyle().setColor(TextFormatting.WHITE);
        source.sendMessage(start().appendSibling(message));
    }

    public static ITextComponent start() {
        ITextComponent message = new TextComponentString("[" + ImprovedVanilla.MOD_NAME + "] ");
        message.getStyle().setColor(TextFormatting.GOLD);
        return message;
    }

    public static ITextComponent clickableConfig() {
        ITextComponent mutableComponent = new TextComponentString(ConfigManager.FILE_NAME);
        mutableComponent.getStyle().setColor(TextFormatting.GREEN);
        mutableComponent.getStyle().setUnderlined(true);
        mutableComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, ConfigManager.getConfigPath()));
        return mutableComponent;
    }

    public static ITextComponent clickableLink(String url, String displayText) {
        ITextComponent mutableComponent = new TextComponentString(displayText);
        mutableComponent.getStyle().setColor(TextFormatting.GREEN);
        mutableComponent.getStyle().setUnderlined(true);
        mutableComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        return mutableComponent;
    }

}
