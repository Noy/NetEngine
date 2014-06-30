package com.noyhillel.networkhub.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Noy on 27/05/2014.
 */
public final class CommandSpyListener extends ModuleListener {

    public CommandSpyListener() {
        super("command-spy");
    }

    public static List<Player> commandListeners = new ArrayList<>();

    @EventHandler
    public void onCommandProcess(PlayerCommandPreprocessEvent event) {
        Player playerSending = event.getPlayer();
        String s = event.getMessage();
        for (Player playerSentTo : commandListeners) {
            playerSentTo.sendMessage(ChatColor.DARK_AQUA + playerSending.getName() + ": " + s);
        }
    }
}