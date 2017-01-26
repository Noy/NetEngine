package com.noyhillel.netsentials.commands;

import com.noyhillel.netsentials.MessageManager;
import com.noyhillel.netsentials.NetSentials;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Owen on 1/25/2017.
 */
@Permission("netsentials.fly")
@CommandMeta(name = "fly", usage = "/fly", description = "The Fly Command")
public final class Fly extends NetAbstractCommandHandler {
    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer(sender);
        if (args.length > 1)throw new NewNetCommandException(NetSentials.getPrefix() + "Too many arguments", NewNetCommandException.ErrorType.MANY_ARGUMENTS);
        if (args.length == 0) {
            if (!netPlayer.getPlayer().getAllowFlight()) {
                netPlayer.turnOnFly();
                netPlayer.sendMessage(MessageManager.getFormats("formats.fly-on"));
            } else {
                netPlayer.turnOffFly();
                netPlayer.sendMessage(MessageManager.getFormats("formats.fly-off"));
            }
        }
        if (args.length == 1) {
            NetPlayer target = NetPlayer.getPlayerFromPlayer(Bukkit.getPlayer(args[0]));
            if (target == null) throw new NewNetCommandException(NetSentials.getPrefix() + "Could not find player", NewNetCommandException.ErrorType.NULL);
            if (!target.getPlayer().getAllowFlight()) {
                target.turnOnFly();
                target.sendMessage(MessageManager.getFormats("formats.fly-on"));
                netPlayer.sendMessage(MessageManager.getFormat("formats.fly-on-target", true, new String[]{"<target>", target.getName()}));
            } else {
                target.turnOffFly();
                target.sendMessage(MessageManager.getFormats("formats.fly-off"));
                netPlayer.sendMessage(MessageManager.getFormat("formats.fly-off-target", true, new String[]{"<target>", target.getName()}));
            }
        }

    }

    @Override
    protected List<String> completeArgs(CommandSender sender, String[] args) {
        return Message.tabComplete(sender, args);
    }
}
