package com.noyhillel.netsentials.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Armani on 31/01/2017.
 */
public final class Death extends ModuleListener {

    public Death() {
        super("death");
    }

    public static Map<UUID, Location> back = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        back.put(player.getUniqueId(), player.getLocation());
    }
}
