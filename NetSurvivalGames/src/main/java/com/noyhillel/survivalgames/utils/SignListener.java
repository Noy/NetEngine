package com.noyhillel.survivalgames.utils;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by Noy on 10/07/2014.
 */
public final class SignListener implements Listener {

    @EventHandler
    public void onSignPlace(SignChangeEvent event) {
        if (event.getPlayer().hasPermission("survivalgames.hubsign") && event.getLine(0).equalsIgnoreCase("/hub")) {
            event.setLine(0,ChatColor.GRAY + "[" + ChatColor.YELLOW + "Hub" + ChatColor.GRAY + "]");
            event.setLine(1, "Right Click");
            event.setLine(2, "To Return to");
            event.setLine(3, "The Hub");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasBlock()) {
            switch (event.getClickedBlock().getType()) {
                case SIGN:
                case SIGN_POST:
                case WALL_SIGN:
                    Sign sign = (Sign) event.getClickedBlock().getState();
                    if (sign.getLine(0).equalsIgnoreCase(ChatColor.GRAY + "[" + ChatColor.YELLOW + "Hub" + ChatColor.GRAY + "]"));
                        //SurvivalGames.getInstance().sendToServer("hub", event.getPlayer());
            }
        }
    }
}
