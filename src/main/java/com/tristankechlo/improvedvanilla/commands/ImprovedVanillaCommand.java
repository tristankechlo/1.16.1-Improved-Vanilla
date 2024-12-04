package com.tristankechlo.improvedvanilla.commands;

import com.tristankechlo.improvedvanilla.config.ConfigManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImprovedVanillaCommand extends CommandBase {

    @Override
    public String getName() {
        return "improvedvanilla";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/improvedvanilla <config|github|issue|wiki|discord|curseforge|modrinth>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException("Must provide at least one argument!");
        }
        if (Arrays.asList(ProjectLinks.ARGS).contains(args[0].toLowerCase())) {
            ProjectLinks projectLinks = ProjectLinks.valueOf(args[0]);
            projectLinks.execute(server, sender);
            return;
        }
        if (args[0].equalsIgnoreCase("config")) {
            if (args.length != 2) {
                throw new WrongUsageException("/improvedvanilla config <reset|reload|show>");
            }
            switch (args[1].toLowerCase()) {
                case "reset":
                    ConfigManager.resetConfig();
                    ResponseHelper.sendMessageConfigReset(sender);
                    break;
                case "reload":
                    ConfigManager.reloadConfig();
                    ResponseHelper.sendMessageConfigReload(sender);
                    break;
                case "show":
                    ResponseHelper.sendMessageConfigShow(sender);
                    break;
                default:
                    throw new WrongUsageException("/improvedvanilla config <reset|reload|show>");
            }
        }
        throw new WrongUsageException(this.getUsage(sender));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos blockPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "config", "github", "issue", "wiki", "discord", "curseforge", "modrinth");
        }
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, "reset", "reload", "show");
        }
        return Collections.emptyList();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

}
