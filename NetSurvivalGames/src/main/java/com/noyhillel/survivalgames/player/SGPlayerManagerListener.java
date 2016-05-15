package com.noyhillel.survivalgames.player;

import com.noyhillel.survivalgames.SurvivalGames;
import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;


@Data
public final class SGPlayerManagerListener implements Listener {
    private final SGPlayerManager manager;

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerJoin(PlayerLoginEvent event) {
        try {
            manager.playerLoggedIn(event.getPlayer());
        } catch (StorageError error) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(ChatColor.RED + "Failed to load your player from the database!\nError Message: " + error.getMessage());
            error.printStackTrace();
            SurvivalGames.getInstance().getLogger().severe("Could not login player " + event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            manager.playerLoggedIn(event.getPlayer());
        } catch (StorageError error) {
            error.printStackTrace();
            SurvivalGames.getInstance().getLogger().severe("Could not login player " + event.getPlayer().getName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent event) {
        manager.playerLoggedOut(event.getPlayer());
    }
}
