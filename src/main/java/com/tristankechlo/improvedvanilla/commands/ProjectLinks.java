package com.tristankechlo.improvedvanilla.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
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

    private final IFormattableTextComponent message;
    public static final List<String> ARGS = Stream.of(ProjectLinks.values()).map(e -> e.name().toLowerCase()).collect(Collectors.toList());

    ProjectLinks(String message, String link) {
        this.message = new StringTextComponent(message);
        this.message.withStyle(TextFormatting.WHITE);
        this.message.append(ResponseHelper.clickableLink(link, link));
    }

    public int execute(CommandContext<CommandSource> sender) {
        sender.getSource().sendSuccess(ResponseHelper.start().append(message), false);
        return 0;
    }

}
