package com.noyhillel.survivalgames;

import com.noyhillel.networkengine.util.NetPlugin;
import com.noyhillel.survivalgames.arena.ArenaException;
import com.noyhillel.survivalgames.arena.ArenaManager;
import com.noyhillel.survivalgames.arena.JSONArenaManager;
import com.noyhillel.survivalgames.arena.setup.SetupModeListener;
import com.noyhillel.survivalgames.command.*;
import com.noyhillel.survivalgames.game.GameException;
import com.noyhillel.survivalgames.game.GameManager;
import com.noyhillel.survivalgames.game.GameManagerListener;
import com.noyhillel.survivalgames.player.GPlayerManager;
import com.noyhillel.survivalgames.player.GPlayerManagerListener;
import com.noyhillel.survivalgames.player.StorageError;
import com.noyhillel.survivalgames.storage.ForgetfulStorage;
import com.noyhillel.survivalgames.storage.GStorage;
import com.noyhillel.survivalgames.storage.GStorageKey;
import com.noyhillel.survivalgames.storage.StorageTypes;
import com.noyhillel.survivalgames.utils.RandomUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

public final class SurvivalGames extends NetPlugin {

    @Getter private static SurvivalGames instance;

    @Getter private ArenaManager arenaManager;
    @Getter private GameManager gameManager;
    @Getter private GPlayerManager playerManager;
    @Getter private SetupCommand setupCommand;
    @Getter private static Random random = new Random();

    @Getter private boolean isSetupOnly = false;

    private static final String arena_directory = "arenas";

    @Override
    public void enable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.kickPlayer(ChatColor.RED + "Server reloading!");
        }
        getLogger().info("Bootstrapping SurvivalGames!");
        try {
            saveDefaultConfig();
            SurvivalGames.instance = this;
            enableTry();
            onSuccessfulEnable();
        } catch (Throwable t) {
            t.printStackTrace();
            SurvivalGames.instance = null;
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("SurvivalGames has been enabled successfully!");
    }

    @Override
    public void disable() {
        getLogger().info("Disabling SurvivalGames!");
        try {
            disableTry();
            SurvivalGames.instance = null;
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }
        getLogger().info("SurvivalGames is disabled completely!");
    }

    private void disableTry() throws StorageError, ArenaException {
        this.playerManager.getStorage().shutdown();
        if (this.gameManager != null) gameManager.disable();
    }

    // So we can catch errors like a boss
    private void enableTry() throws ArenaException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.arenaManager = new JSONArenaManager(new File(getDataFolder(), arena_directory), getLogger());
        try {
            this.gameManager = new GameManager();
            registerListener(new GameManagerListener(this, gameManager));
        } catch (GameException ex) {
            getLogger().severe("Error from GameManager setup: " + ex.getMessage());
            ex.getCause().printStackTrace();
            isSetupOnly = true; //This means that we can only setup arenas now because there was a problem setting up the game manager, which normally means that you could not load the lobby
        }
        GStorage storage = getStorage();
        if (storage == null) fallbackStorage();
        else this.playerManager = new GPlayerManager(storage);
        try {
            this.playerManager.enable();
        } catch (StorageError error) {
            error.printStackTrace();
            fallbackStorage();
        }
        registerListener(new GPlayerManagerListener(this.playerManager));
        setupCommands(GameCommand.class);
        setupCommands(VoteCommand.class);
        setupCommands(MapCommand.class);
        setupCommands(LinkChestsCommand.class);
        setupCommand = registerListener(setupCommands(SetupCommand.class));
        registerListener(new SetupModeListener());
        setupCommands(NickCommand.class);
        setupCommands(StatsCommand.class);
        ConsoleCommandSender consoleSender = getServer().getConsoleSender();
        consoleSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cSurvivalGames&e has been fully enabled!"));
    }

    private void fallbackStorage() {
        getLogger().severe("Could not connect to MySQL, defaulting to no-save storage!");
        this.playerManager = new GPlayerManager(new ForgetfulStorage());
        try {
            this.playerManager.enable();
        } catch (StorageError error) {
            error.printStackTrace();
        }
    }

    private void onSuccessfulEnable() {
        if (isSetupOnly) {
            getLogger().warning("SurvivalGames is in SETUP MODE only. This will prevent all non-OPs from logging in.");
//            registerListener(new ArenaSetup());
        }
    }


    private GStorage getStorage() {
        String storageType = getConfig().getString("storage");
        for (StorageTypes storageTypes : StorageTypes.values()) {
            GStorageKey annotation = storageTypes.getClazz().getAnnotation(GStorageKey.class);
            if (RandomUtils.contains(storageType, annotation.value())) return storageTypes.getSetupDelegate().getStorage();
        }
        return null;
    }

    public String getFileData(String filename) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getResource(filename)));
        String line;
        StringBuilder builder = new StringBuilder();
        try {
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return builder.toString();
    }
}
