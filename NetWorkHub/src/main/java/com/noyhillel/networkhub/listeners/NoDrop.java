package com.noyhillel.networkhub.listeners;

import com.noyhillel.networkengine.util.player.NetPlayer;
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
        NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (netPlayer.hasPermission("hub.drop-item")) return;
        netPlayer.sendMessage(MessageManager.getFormats("formats.cant-drop"));
        event.setCancelled(true);
    }
}