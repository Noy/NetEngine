package com.noyhillel.netsentials.commands;

import com.noyhillel.netsentials.MessageManager;
import com.noyhillel.netsentials.NetSentials;
import com.noyhillel.netsentials.listeners.Death;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.entity.Player;

/**
 * Created by Armani on 31/01/2017.
 */
@Permission("netsentials.back")
@CommandMeta(name = "back", usage = "/back", description = "The Back Command")
public final class Back extends NetAbstractCommandHandler {


    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        if (args.length > 0)throw new NewNetCommandException(NetSentials.getPrefix() + "Not enough arguments", NewNetCommandException.ErrorType.MANY_ARGUMENTS);
        NetPlayer player = NetPlayer.getPlayerFromPlayer(sender);
        if (Death.back.containsKey(player.getUuid())) {
            player.teleport(Death.back.get(player.getUuid()));
            player.sendMessage(MessageManager.getFormats("formats.gone-back"));
            //Death.back.remove(player.getUuid());
        } else {
            throw new NewNetCommandException(NetSentials.getPrefix() + "You did not have a previous location.", NewNetCommandException.ErrorType.NULL);
        }
    }
}

