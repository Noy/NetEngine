package com.noyhillel.netsentials.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public final class Join extends ModuleListener {

    public Join() {
        super("join");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

    }
}
