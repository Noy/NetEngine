package com.noyhillel.networkhub.listeners;

import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.networkhub.MessageManager;
import com.noyhillel.networkhub.commands.SpawnCommand;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Created by Noy on 26/05/2014.
 */
public final class BelowZero extends ModuleListener {

    public BelowZero() {
        super("below-zero");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (netPlayer.hasPermission("hub.below-zero")) return;
        if (netPlayer.getLocation().getY() < 0) {
            netPlayer.teleport(SpawnCommand.getLocation("spawn"));
            netPlayer.playSound(Sound.CHICKEN_EGG_POP);
            netPlayer.sendMessage(MessageManager.getFormats("formats.tpd-spawn"));
        }
    }
}