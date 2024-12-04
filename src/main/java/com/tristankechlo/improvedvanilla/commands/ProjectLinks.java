package com.tristankechlo.improvedvanilla.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ProjectLinks {

    GITHUB("Check out the source code on GitHub: ", "https://github.com/tristankechlo/ImprovedVanilla"),
    ISSUE("If you found an issue, submit it here: ", "https://github.com/tristankechlo/ImprovedVanilla/issues"),
    WIKI("The wiki can be found here: ", "https://github.com/tristankechlo/ImprovedVanilla/wiki"),
    DISCORD("Join the Discord here: ", "https://discord.gg/bhUaWhq"),
    CURSEFORGE("Check out the CurseForge page here: ", "https://curseforge.com/minecraft/mc-mods/improved-vanilla"),
    MODRINTH("Check out the Modrinth page here: ", "https://modrinth.com/mod/improved-vanilla");

    private final ITextComponent message;
    public static final List<String> ARGS = Stream.of(ProjectLinks.values()).map(e -> e.name().toLowerCase()).collect(Collectors.toList());

    ProjectLinks(String message, String link) {
        this.message = new TextComponentString(message);
        this.message.getStyle().setColor(TextFormatting.WHITE);
        this.message.appendSibling(ResponseHelper.clickableLink(link, link));
    }

    public void execute(MinecraftServer server, ICommandSender sender) {
        sender.sendMessage(ResponseHelper.start().appendSibling(message));
    }

}
