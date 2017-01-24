package com.noyhillel.netsentials.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class Chat extends ModuleListener {

    public Chat() {
        super("chat");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        event.setFormat(ChatColor.GRAY + "%s" + ChatColor.BLUE + ": " + ChatColor.WHITE + "%s");
        if (player.isOp()) {
            event.setFormat(ChatColor.RED + "%s" + ChatColor.BLUE + ": " + ChatColor.WHITE + "%s");
        }
        if (player.hasPermission("netsentials.color-chat")) {
            event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));
            if (event.getMessage().startsWith(">")) event.setMessage(ChatColor.GREEN + event.getMessage()); // lol
        }
    }
}