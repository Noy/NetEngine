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

@Permission("netsentials.heal")
@CommandMeta(name = "heal", usage = "/heal", description = "The heal command.")
public final class Heal extends NetAbstractCommandHandler {

    @Override
    protected void playerCommand(Player player, String[] args) throws NewNetCommandException {
        if (args.length > 1) throw new NewNetCommandException(NetSentials.getPrefix() + "Too many arguments.", NewNetCommandException.ErrorType.NULL);
        NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer(player);
        if (args.length == 0) {
            netPlayer.refreshPlayer();
            netPlayer.sendMessage(MessageManager.getFormats("formats.healed"));
        }
        if (args.length == 1) {
            NetPlayer target = NetPlayer.getPlayerFromPlayer(Bukkit.getPlayer(args[0]));
            if (target == null) {
                netPlayer.sendMessage(MessageManager.getFormats("formats.not-found"));
                return;
            }
            target.refreshPlayer();
            target.sendMessage(MessageManager.getFormat("formats.player-healed", true, new String[]{"<player>", netPlayer.getName()}));
            netPlayer.sendMessage(MessageManager.getFormat("formats.healed-player", true, new String[]{"<target>", target.getName()}));
        }
    }

    @Override
    protected List<String> completeArgs(CommandSender sender, String[] args) {
        return Message.tabComplete(sender, args);
    }
}

