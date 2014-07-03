package com.noyhillel.survivalgames.player;

import com.noyhillel.survivalgames.SurvivalGames;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.Random;

@Data
public final class GPlayerManagerListener implements Listener {
    private final GPlayerManager manager;

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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent event) {
        manager.playerLoggedOut(event.getPlayer());
    }
}
