package com.noyhillel.networkengine.util.player;

import lombok.Data;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by Noy on 27/05/2014.
 */
@Data
public final class NetPlayerManagerListener implements Listener {

    private final NetPlayerManager netPlayerManager;

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        netPlayerManager.playerLoggedIn(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        netPlayerManager.playerLoggedIn(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        netPlayerManager.playerLoggedOut(event.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        netPlayerManager.playerLoggedOut(event.getPlayer());
    }
}