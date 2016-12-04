package com.noyhillel.paintball;

import com.noyhillel.networkengine.exceptions.ArenaException;
import com.noyhillel.networkengine.util.MainClass;
import com.noyhillel.networkengine.util.NetPlugin;
import com.noyhillel.paintball.command.SetupCommand;
import com.noyhillel.paintball.game.arena.ArenaManager;
import com.noyhillel.paintball.game.arena.ArenaSetupListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Lacoste on 9/17/2016.
 */
@MainClass(name = "NetPaintball", description = "The Net Paintball Plugin", authors = "NoyHillel1")
public class Paintball extends NetPlugin {

    @Getter private static Paintball instance;
    @Getter private ArenaManager arenaManager;
    @Getter private boolean isSetupOnly = false;
    @Getter private SetupCommand setupCommand;


    protected void enable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer(RELOAD_MESSAGE);
        }
        if (new File(getDataFolder(), "SETUP_LOCK").exists()) isSetupOnly = true;
        logInfoInColor(ChatColor.YELLOW + "Enabling Paintball..");
        try {
            Paintball.instance = this;
            tryEnable();
        }catch (Throwable t) {
            t.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
        logInfoInColor(ChatColor.YELLOW + "Enabled!");
    }

    protected void disable() {
        logInfoInColor(ChatColor.GREEN + "Disabling Paintball");
        try {
            Paintball.instance = null;
            tryDisable();
        }catch (Throwable t) {
            t.printStackTrace();
        }
        logInfoInColor(ChatColor.GREEN + "Paintball is disabled completely.");
    }

    private void tryEnable() throws ArenaException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        try {
            //this.gameManager = new GameManager();
            //registerListener(new GameManagerListener(this, gameManager));
        } catch (Exception ex) {
            isSetupOnly = true;
        }
        registerListener(new ArenaSetupListener());
        setupCommand = registerListener(setupCommands(SetupCommand.class));
    }

    private void tryDisable() {

    }
}
