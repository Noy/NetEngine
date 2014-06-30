package com.noyhillel.networkhub.listeners;

import com.noyhillel.networkhub.MessageManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Created by Noy on 26/05/2014.
 */
public final class NoDrop extends ModuleListener {

    public NoDrop() {
        super("no-drop");
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().hasPermission("hub.drop-item")) return;
        event.getPlayer().sendMessage(MessageManager.getFormats("formats.cant-drop"));
        event.setCancelled(true);
    }
}
