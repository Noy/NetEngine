package com.noyhillel.networkhub.listeners;

import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.networkhub.MessageManager;
import com.noyhillel.networkhub.items.HidePlayersItem;
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
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (CommandSpyListener.commandListeners.contains(player)) {
            CommandSpyListener.commandListeners.remove(player);
        }
        event.setQuitMessage(MessageManager.getFormat("formats.leave-message", false));
        if (HidePlayersItem.hidingPlayers.contains(player.getUuid())) {
            HidePlayersItem.hidingPlayers.remove(player.getUuid());
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (CommandSpyListener.commandListeners.contains(player)) {
            CommandSpyListener.commandListeners.remove(player);
        }
        if (HidePlayersItem.hidingPlayers.contains(player.getUuid())) {
            HidePlayersItem.hidingPlayers.remove(player.getUuid());
        }
    }
}