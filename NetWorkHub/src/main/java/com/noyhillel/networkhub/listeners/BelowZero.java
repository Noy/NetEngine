package com.noyhillel.networkhub.listeners;

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
        Player player = event.getPlayer();
        if (event.getPlayer().hasPermission("hub.below-zero")) return;
        if (player.getLocation().getY() < 0) {
            player.teleport(SpawnCommand.getLocation("spawn"));
            player.playSound(SpawnCommand.getLocation("spawn"), Sound.CHICKEN_EGG_POP, 20, 1);
            player.sendMessage(MessageManager.getFormats("formats.tpd-spawn"));
        }
    }
}