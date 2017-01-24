package com.noyhillel.netsentials.commands;

import com.noyhillel.netsentials.MessageManager;
import com.noyhillel.networkengine.command.AbstractCommandHandler;
import com.noyhillel.networkengine.command.CommandStatus;
import com.noyhillel.networkengine.command.NetCommand;
import com.noyhillel.networkengine.command.NetCommandSenders;
import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class Home extends AbstractCommandHandler {

    private Map<NetPlayer, Location> homeLoc = new HashMap<>();

    @NetCommand(
            name = "home",
            permission = "netsentials.home",
            senders = NetCommandSenders.PLAYER,
            description = "Home command"
    )
    public CommandStatus home(CommandSender sender, NetCommandSenders senders, NetCommand meta, Command command, String[] args) {
        Player player = (Player) sender;
        NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer(player);
        if (args.length == 0) {
            if (!homeLoc.containsKey(netPlayer)) {
                netPlayer.sendMessage(MessageManager.getFormats("formats.no-home"));
                return CommandStatus.SUCCESS;
            }
            Location location = homeLoc.get(netPlayer);
            player.teleport(location);
            netPlayer.sendMessage(MessageManager.getFormats("formats.tp-home"));
            return CommandStatus.SUCCESS;
        }
        if (args[0].equalsIgnoreCase("set")) {
            homeLoc.put(netPlayer, netPlayer.getLocation());
            player.sendMessage(MessageManager.getFormats("formats.home-set"));
            return CommandStatus.SUCCESS;
        }
        if (args[0].equalsIgnoreCase("del")) {
            Location location = homeLoc.get(netPlayer);
            homeLoc.remove(netPlayer, location);
            player.sendMessage(MessageManager.getFormats("formats.home-del"));
            return CommandStatus.SUCCESS;
        }
        if (args[0] != null) return CommandStatus.NULL;
        return CommandStatus.SUCCESS;
    }
}
