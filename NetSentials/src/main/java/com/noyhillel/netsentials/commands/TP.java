package com.noyhillel.netsentials.commands;

import com.noyhillel.netsentials.MessageManager;
import com.noyhillel.netsentials.NetSentials;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@Permission("netsentials.tp")
@CommandMeta(name = "tp", usage = "/tp <player> [target] or x y z", description = "The TP Command")
public final class TP extends NetAbstractCommandHandler {


    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        if (args.length == 0) throw new NewNetCommandException(NetSentials.getPrefix() + "Not enough arguments.", NewNetCommandException.ErrorType.FEW_ARGUMENTS);
        if (args.length > 4) throw new NewNetCommandException(NetSentials.getPrefix() + "Too many arguments.", NewNetCommandException.ErrorType.FEW_ARGUMENTS);
        //NetPlayer player = NetPlayer.getPlayerFromPlayer(sender);
        Location loc;
        if (args.length == 1) {
            NetPlayer tp1 = NetPlayer.getPlayerFromPlayer(Bukkit.getPlayer(args[0]));
            if (tp1 == null) throw new NewNetCommandException(NetSentials.getPrefix() + "Could not find player", NewNetCommandException.ErrorType.NULL);
            sender.teleport(tp1.getLocation());
            sender.sendMessage(MessageManager.getFormat("formats.you-tpd", true, new String[]{"<target>", tp1.getName()}));
            return;
        }
        if (args.length == 2) {
            NetPlayer tp1 = NetPlayer.getPlayerFromPlayer(Bukkit.getPlayer(args[0]));
            NetPlayer tp2 = NetPlayer.getPlayerFromPlayer(Bukkit.getPlayer(args[1]));
            if (tp1 == null) throw new NewNetCommandException(NetSentials.getPrefix() + "Could not find player", NewNetCommandException.ErrorType.NULL);
            if (tp2 == null) throw new NewNetCommandException(NetSentials.getPrefix() + "Could not find player", NewNetCommandException.ErrorType.NULL);
            Location tp2Loc = tp2.getLocation();
            tp1.teleport(tp2Loc);
            tp1.sendMessage(MessageManager.getFormat("formats.tpd-you", true, new String[]{"<player>", sender.getName()}, new String[]{"<target>", tp2.getName()}));
            sender.sendMessage(MessageManager.getFormat("formats.you-tpd", true, new String[]{"<player>", tp1.getName()}, new String[]{"<target>", tp2.getName()}));
            return;
        }
        if (args.length == 3) {
            try {
                loc = new Location(sender.getPlayer().getWorld(), Double.valueOf(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]));
                sender.teleport(loc);
                sender.sendMessage(MessageManager.getFormat("formats.cord-tpd", true, new String[]{"<player>", sender.getName()},
                        new String[]{"<x>", args[0]},new String[]{"<y>", args[1]},new String[]{"<z>", args[2]}));

            } catch (NumberFormatException e) {
                throw new NewNetCommandException(NetSentials.getPrefix() + "Co-ordinates not found.", NewNetCommandException.ErrorType.NULL);
            }
        }
        if (args.length == 4) {
            try {
                NetPlayer tp1 = NetPlayer.getPlayerFromPlayer(Bukkit.getPlayer(args[0]));
                if (tp1 == null) throw new NewNetCommandException(NetSentials.getPrefix() + "Could not find player", NewNetCommandException.ErrorType.NULL);
                loc = new Location(tp1.getPlayer().getWorld(), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
                tp1.teleport(loc);
                sender.sendMessage(MessageManager.getFormat("formats.tpd-cords", true, new String[]{"<target>", tp1.getName()}));
                tp1.sendMessage(MessageManager.getFormat("formats.cord-tpd", true, new String[]{"<player>", sender.getName()},
                        new String[]{"<x>", args[1]},new String[]{"<y>", args[2]}, new String[]{"<z>", args[3]}));

            } catch (NumberFormatException e) {
                throw new NewNetCommandException(NetSentials.getPrefix() + "Co-ordinates not found.", NewNetCommandException.ErrorType.NULL);
            }
            return;
        }
    }

    @Override
    protected List<String> completeArgs(CommandSender sender, String[] args) {
        return Message.tabComplete(sender, args);
    }
}
