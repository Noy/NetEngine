package com.noyhillel.networkhub.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Created by Noy on 26/05/2014.
 */
public final class ColoredSigns extends ModuleListener {

    public ColoredSigns() {
        super("colored-signs");
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!event.getPlayer().hasPermission("hub.coloredsigns")) return;
        for (int x = 0; x < event.getLines().length; x++) {
            event.setLine(x, ChatColor.translateAlternateColorCodes('&', event.getLine(x)));
        }
    }
}