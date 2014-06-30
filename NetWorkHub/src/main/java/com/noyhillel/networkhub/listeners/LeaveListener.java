package com.noyhillel.networkhub.listeners;

import com.noyhillel.networkhub.MessageManager;
import com.noyhillel.networkhub.items.HidePlayersItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by Noy on 26/05/2014.
 */
public final class LeaveListener extends ModuleListener {

    public LeaveListener() {
        super("leave-listener");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        //NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        Player player = event.getPlayer();
        if (CommandSpyListener.commandListeners.contains(player)) {
            CommandSpyListener.commandListeners.remove(player);
        }
        event.setQuitMessage(MessageManager.getFormat("formats.leave-message", false));
        HidePlayersItem.hidingPlayers.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if (CommandSpyListener.commandListeners.contains(player)) {
            CommandSpyListener.commandListeners.remove(player);
        }
        HidePlayersItem.hidingPlayers.remove(player.getUniqueId());
    }
}