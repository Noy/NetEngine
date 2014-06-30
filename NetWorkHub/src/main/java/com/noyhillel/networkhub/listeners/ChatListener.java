package com.noyhillel.networkhub.listeners;

import com.noyhillel.networkengine.exceptions.CooldownUnexpiredException;
import com.noyhillel.networkhub.MessageManager;
import com.noyhillel.networkhub.NetWorkHub;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Created by Noy on 28/05/2014.
 */
public class ChatListener extends ModuleListener {

    public ChatListener() {
        super("chat-listener");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        try {
            if (!player.hasPermission("hub.anti-spam")) {
                NetWorkHub.getCooldown().testCooldown(player.getName(), NetWorkHub.getInstance().getConfig().getLong("cooldown.chat-time"));
            }
        } catch (CooldownUnexpiredException e) {
            player.sendMessage(MessageManager.getFormat("cooldown.chat", true, new String[]{"<time>", String.valueOf(NetWorkHub.getInstance().getConfig().getLong("formats.cooldown-time"))}));
            event.setCancelled(true);
            return;
        }
        event.setFormat(ChatColor.GRAY + "%s" + ChatColor.BLUE + ": " + ChatColor.WHITE + "%s");
        if (player.hasPermission("hub.color-chat")) {
            event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));
        }
    }
}