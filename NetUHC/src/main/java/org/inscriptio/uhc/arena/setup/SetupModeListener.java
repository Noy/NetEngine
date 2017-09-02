package org.inscriptio.uhc.arena.setup;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.inscriptio.uhc.NetUHC;

public final class SetupModeListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if ((!event.getPlayer().isOp() && NetUHC.getInstance().isSetupOnly()))
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "The server is currently in setup only mode. You must be OP to access the server.");
    }
}