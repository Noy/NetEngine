package com.noyhillel.paintball.game.arena;

import com.noyhillel.paintball.Paintball;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * Created by Armani on 04/12/2016.
 */
public final class ArenaSetupListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if ((!event.getPlayer().isOp() && Paintball.getInstance().isSetupOnly()))
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "The server is currently in setup only mode. You must be OP to access the server.");
    }
}