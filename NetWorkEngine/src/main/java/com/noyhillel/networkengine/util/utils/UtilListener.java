package com.noyhillel.networkengine.util.utils;

import com.noyhillel.networkengine.util.NetPlugin;
import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * <p/>
 * Latest Change: 13/07/2014.
 * <p/>
 *
 * @author Noy
 * @since 13/07/2014.
 */
public final class UtilListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        final PluginDescriptionFile pluginDescriptionFile = NetPlugin.getInstance().getPluginDescriptionFile();
        if (player.isOp()) {
            Bukkit.getScheduler().runTaskLater(NetPlugin.getInstance(), new Runnable() {
                @Override
                public void run() {
                    player.sendMessage(ChatColor.GOLD + "[Net] " + ChatColor.RED + "NetEngineAPI linked! Version: " + ChatColor.GREEN + pluginDescriptionFile.getVersion());
                }
            }, 60L);
        }
    }

}
