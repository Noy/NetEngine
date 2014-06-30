package com.noyhillel.networkengine;

import com.noyhillel.networkengine.util.MainClass;
import com.noyhillel.networkengine.util.NetPlugin;
import com.noyhillel.networkengine.util.player.NetPlayer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Noy on 25/05/2014.
 */
@MainClass(name = "NetEngine", description = "The Net Engine Plugin")
public final class NetWorkEngine extends NetPlugin {

    @Getter private static NetWorkEngine instance;
    private final static String RELOAD_MESSAGE = ChatColor.RED + "Server is reloading!";

    @Override
    protected void enable() {
        this.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Enabled");
        NetWorkEngine.instance = this;
        saveDefaultConfig();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer(RELOAD_MESSAGE);
        }
    }

    @Override
    protected void disable() {
        NetWorkEngine.instance = null;
        this.getServer().getPluginManager().disablePlugin(this);
    }
}