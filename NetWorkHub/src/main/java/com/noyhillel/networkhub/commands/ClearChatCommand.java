package com.noyhillel.networkhub.commands;

import com.noyhillel.networkengine.command.*;
import com.noyhillel.networkhub.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Noy on 2013/12/23.
 */
public final class ClearChatCommand extends AbstractCommandHandler {

    @NetCommand(
            name = "clearchat",
            usage = "/clearchat",
            permission = "hub.clearchat",
            description = "The Clear Chat Command",
            senders = {NetCommandSenders.PLAYER, NetCommandSenders.CONSOLE}
    )
    public CommandStatus clearchat(CommandSender sender, NetCommandSenders type, NetCommand netCommand, Command command, String[] args) {
        if (args.length > 0) return CommandStatus.MANY_ARGUMENTS;
        for (Integer i = 0; i <= 200; i++) {
            silentBroadcast("", true);
        }
        silentBroadcast(MessageManager.getFormats("formats.clear-chat"), false);
        return CommandStatus.SUCCESS;
    }

    @NetCommand(
            name = "clearmychat",
            usage = "/clearmychat",
            permission = "hub.clearmychat",
            description = "Clear my chat command",
            senders = {NetCommandSenders.PLAYER, NetCommandSenders.CONSOLE}
    )
    public CommandStatus clearmychat(CommandSender sender, NetCommandSenders type, NetCommand meta, Command command, String[] args) {
        for (Integer i = 0; i <= 200; i++) {
            sender.sendMessage("");
        }
        return CommandStatus.SUCCESS;
    }

    private void silentBroadcast(String message, Boolean b) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if ((p.hasPermission("hub.clearchat-bypass")) && b) continue;
            p.sendMessage(message);
        }
    }
}
