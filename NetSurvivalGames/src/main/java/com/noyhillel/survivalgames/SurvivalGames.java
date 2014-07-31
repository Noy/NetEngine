package com.noyhillel.survivalgames;

import com.noyhillel.networkengine.util.MainClass;
import com.noyhillel.networkengine.util.NetPlugin;
import com.noyhillel.networkengine.util.utils.RandomUtils;
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
import com.noyhillel.survivalgames.utils.SignListener;
import lilypad.client.connect.api.Connect;
import lilypad.client.connect.api.request.impl.RedirectRequest;
import lilypad.client.connect.api.result.FutureResultListener;
import lilypad.client.connect.api.result.StatusCode;
import lilypad.client.connect.api.result.impl.RedirectResult;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

@MainClass(name = "NetSG", description = "The Net SurvivalGames plugin!", authors = {"Twister915", "NoyHillel1"})
public final class SurvivalGames extends NetPlugin {

    @Getter private static SurvivalGames instance;
    @Getter private ArenaManager arenaManager;
    @Getter private GameManager gameManager;
    @Getter private GPlayerManager gPlayerManager;
    @Getter private SetupCommand setupCommand;
    @Getter private boolean isSetupOnly = false;

    /* constants */
    private static final String ARENA_DIRECTORY = "arenas";

    @Override
    protected void enable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.kickPlayer(RELOAD_MESSAGE);
        }
        logInfoInColor(ChatColor.YELLOW + "Enabling SurvivalGames...");
        try {
            saveDefaultConfig();
            SurvivalGames.instance = this;
            tryEnable();
            onSuccessfulEnable();
        } catch (Throwable t) {
            t.printStackTrace();
            SurvivalGames.instance = null;
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    protected void disable() {
        logInfoInColor(ChatColor.YELLOW + "Disabling SurvivalGames!");
        try {
            tryDisable();
            SurvivalGames.instance = null;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        logInfoInColor(ChatColor.GREEN + "SurvivalGames is disabled completely!");
    }

    private void tryDisable() throws StorageError, ArenaException {
        this.gPlayerManager.getStorage().shutdown();
        gameManager.disable();
    }

    private void tryEnable() throws ArenaException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.arenaManager = new JSONArenaManager(new File(getDataFolder(), ARENA_DIRECTORY), getLogger());
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
        else this.gPlayerManager = new GPlayerManager(storage);
        try {
            this.gPlayerManager.enable();
        } catch (StorageError error) {
            error.printStackTrace();
            fallbackStorage();
        }
        registerListener(new GPlayerManagerListener(this.gPlayerManager));
        registerListener(new SignListener());
        registerListener(new SetupModeListener());
        setupCommand = registerListener(setupCommands(SetupCommand.class));
        setupCommands(VoteCommand.class);
        setupCommands(MapCommand.class);
        setupCommands(HealCommand.class);
        setupCommands(LinkChestsCommand.class);
        setupCommands(NickCommand.class);
        setupCommands(StatsCommand.class);
        setupCommands(SpawnCommand.class);
        setupCommands(SetSpawnCommand.class);
        setupCommands(HubCommand.class);
        setupCommands(SpectateCommand.class);
        logInfoInColor(ChatColor.translateAlternateColorCodes('&', "&eSurvivalGames&a has been fully enabled!"));
    }

    private void fallbackStorage() {
        getLogger().severe("Could not connect to MySQL, defaulting to no-save storage!");
        this.gPlayerManager = new GPlayerManager(new ForgetfulStorage());
        try {
            this.gPlayerManager.enable();
        } catch (StorageError error) {
            error.printStackTrace();
        }
    }

    private void onSuccessfulEnable() {
        if (isSetupOnly) {
            getLogger().warning("SurvivalGames is in SETUP MODE only. This will prevent all non-OPs from logging in.");
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

    public Connect getBukkitConnect() {
        return getServer().getServicesManager().getRegistration(Connect.class).getProvider();
    }

    public void sendToServer(String server, final Player player) {
        try {
            Connect c = getBukkitConnect();
            c.request(new RedirectRequest(server, player.getName())).registerListener(new FutureResultListener<RedirectResult>() {
                @Override
                public void onResult(RedirectResult redirectResult) {
                    if (redirectResult.getStatusCode() == StatusCode.SUCCESS) {
                        return;
                    }
                    player.sendMessage("Could not connect");
                }
            });
        } catch (Exception exception) {
            player.sendMessage("Could not connect");
        }
    }
}
