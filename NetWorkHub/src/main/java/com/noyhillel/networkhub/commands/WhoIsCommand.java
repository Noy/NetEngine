package com.noyhillel.networkhub.commands;

import com.noyhillel.networkengine.command.AbstractCommandHandler;
import com.noyhillel.networkengine.command.CommandStatus;
import com.noyhillel.networkengine.command.NetCommand;
import com.noyhillel.networkengine.command.NetCommandSenders;
import com.noyhillel.networkhub.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Noy on 08/07/2014.
 */
public class WhoIsCommand extends AbstractCommandHandler {


    @SuppressWarnings("LoopStatementThatDoesntLoop")
    @NetCommand(
            name = "whois",
            usage = "/whois",
            description = "Find out who a player is from their nick name.",
            permission = "hub.nick",
            senders = {NetCommandSenders.PLAYER}
    )
    public CommandStatus whois(CommandSender sender, NetCommandSenders senders, NetCommand netCommand, Command command, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) return CommandStatus.FEW_ARGUMENTS;
        if (args.length > 1) return CommandStatus.MANY_ARGUMENTS;
        String nick = args[0];
        for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
            if (!nick.equalsIgnoreCase(onlinePlayers.getDisplayName())) {
                player.sendMessage(MessageManager.getFormats("formats.no-nick"));
                return CommandStatus.SUCCESS;
            }
            if (onlinePlayers.getDisplayName().equalsIgnoreCase(nick)) {
                player.sendMessage(MessageManager.getFormat("formats.nick-is-player", true, new String[]{"<nick>", onlinePlayers.getDisplayName()}, new String[]{"<player>", onlinePlayers.getName()}));
                return CommandStatus.SUCCESS;
            }
            return CommandStatus.SUCCESS;
        }
        return CommandStatus.SUCCESS;
    }
}
