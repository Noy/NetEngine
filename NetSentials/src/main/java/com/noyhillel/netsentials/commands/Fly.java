package com.noyhillel.netsentials.commands;

import com.noyhillel.netsentials.MessageManager;
import com.noyhillel.networkengine.exceptions.NewNetCommandException;
import com.noyhillel.networkengine.newcommand.CommandMeta;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.newcommand.Permission;
import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.entity.Player;

/**
 * Created by Owen on 1/25/2017.
 */
@Permission("netsentials.fly")
@CommandMeta(name = "fly", usage = "/fly", description = "The Fly Command")
public class Fly extends NetAbstractCommandHandler {
    @Override
    protected void playerCommand(Player sender, String[] args) throws NewNetCommandException {
        NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer(sender);
        if (args.length > 0)throw new NewNetCommandException("Too many arguments", NewNetCommandException.ErrorType.MANY_ARGUMENTS);
        if (netPlayer.getPlayer().isFlying()) {
            netPlayer.turnOnFly();
            netPlayer.sendMessage(MessageManager.getFormats("formats.fly-on"));
        }else {
            netPlayer.turnOffFly();
            netPlayer.sendMessage(MessageManager.getFormats("formats.fly-off"));
        }
    }
}
