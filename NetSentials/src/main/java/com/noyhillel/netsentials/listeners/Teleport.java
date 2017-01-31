package com.noyhillel.netsentials.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;

import static com.noyhillel.netsentials.listeners.Death.back;

/**
 * Created by Armani on 31/01/2017.
 */
public final class Teleport extends ModuleListener {

    public Teleport() {
        super("teleport");
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        back.put(player.getUniqueId(), player.getLocation());
    }
}

