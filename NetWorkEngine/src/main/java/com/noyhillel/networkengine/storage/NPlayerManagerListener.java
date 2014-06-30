package com.noyhillel.networkengine.storage;

import com.noyhillel.networkengine.exceptions.NPlayerJoinException;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by Noy on 12/06/2014.
 */
@Data
public final class NPlayerManagerListener implements Listener {
    private final NPlayerManager playerManager;

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerJoin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) return; //Prevent whitelist from causing memory leaks.
        Player player = event.getPlayer();
        try {
            playerManager.playerLoggedIn(player, event.getAddress());
        } catch (NPlayerJoinException e) {
            event.setKickMessage(e.getDisconectMessage());
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (playerManager.getNPlayerForPlayer(event.getPlayer()) != null) playerManager.playerLoggedOut(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKick(PlayerKickEvent event) {
        if (playerManager.getNPlayerForPlayer(event.getPlayer()) != null) playerManager.playerLoggedOut(event.getPlayer());
    }
}
