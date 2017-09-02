package org.inscriptio.uhc;

import com.noyhillel.networkengine.exceptions.ArenaException;
import com.noyhillel.networkengine.util.MainClass;
import com.noyhillel.networkengine.util.NetPlugin;
import com.noyhillel.networkengine.util.utils.NetCoolDown;
import com.noyhillel.networkengine.util.utils.RandomUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.inscriptio.uhc.arena.ArenaManager;
import org.inscriptio.uhc.arena.JSONArenaManager;
import org.inscriptio.uhc.arena.setup.SetupModeListener;
import org.inscriptio.uhc.command.*;
import org.inscriptio.uhc.command.setcommands.SetPointsCommand;
import org.inscriptio.uhc.command.setcommands.SetSpawnCommand;
import org.inscriptio.uhc.game.GameException;
import org.inscriptio.uhc.game.GameManager;
import org.inscriptio.uhc.game.GameManagerListener;
import org.inscriptio.uhc.player.UHCPlayerManager;
import org.inscriptio.uhc.player.UHCPlayerManagerListener;
import org.inscriptio.uhc.player.StorageError;
import org.inscriptio.uhc.storage.ForgetfulStorage;
import org.inscriptio.uhc.storage.GStorage;
import org.inscriptio.uhc.storage.GStorageKey;
import org.inscriptio.uhc.storage.StorageTypes;
import org.inscriptio.uhc.utils.SignListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Noy on 25/04/2017.
 */
@MainClass(name = "NetUHC", description = "The Net UHC Plugin", website = "https://inscriptio.org")
public final class NetUHC extends NetPlugin {

    @Getter
    private static NetUHC instance;
    @Getter private ArenaManager arenaManager;
    @Getter private NetCoolDown coolDown;
    @Getter private GameManager gameManager;
    @Getter private UHCPlayerManager uhcPlayerManager;
    @Getter private SetupCommand setupCommand;
    @Getter private boolean isSetupOnly = false;

    /* constants */
    private static final String ARENA_DIRECTORY = "arenas";

    @Override
    protected void enable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.kickPlayer(RELOAD_MESSAGE);
        }
        if (new File(getDataFolder(), "SETUP_LOCK").exists()) isSetupOnly = true;
        logInfoInColor(ChatColor.YELLOW + "Enabling SurvivalGames...");
        try {
            this.coolDown = new NetCoolDown();
            saveDefaultConfig();
            NetUHC.instance = this;
            tryEnable();
            onSuccessfulEnable();
        } catch (Throwable t) {
            t.printStackTrace();
            NetUHC.instance = null;
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    protected void disable() {
        logInfoInColor(ChatColor.YELLOW + "Disabling SurvivalGames!");
        try {
            tryDisable();
            NetUHC.instance = null;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        logInfoInColor(ChatColor.GREEN + "SurvivalGames is disabled completely!");
    }

    private void tryDisable() throws StorageError, ArenaException {
        this.uhcPlayerManager.getStorage().shutdown();
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
        else this.uhcPlayerManager = new UHCPlayerManager(storage);
        try {
            this.uhcPlayerManager.enable();
        } catch (StorageError error) {
            error.printStackTrace();
            fallbackStorage();
        }
        registerListener(new UHCPlayerManagerListener(this.uhcPlayerManager));
        registerListener(new SignListener());
        registerListener(new SetupModeListener());
        setupCommand = registerListener(setupCommands(SetupCommand.class));
        setupCommands(VoteCommand.class);
        setupCommands(MapCommand.class);
        setupCommands(LinkChestsCommand.class);
        setupCommands(NickCommand.class);
        setupCommands(StatsCommand.class);
        setupCommands(SpawnCommand.class);
        setupCommands(SetSpawnCommand.class);
        setupCommands(HubCommand.class);
        setupCommands(SpectateCommand.class);
        setupCommands(SetPointsCommand.class);
        logInfoInColor(ChatColor.translateAlternateColorCodes('&', "&eSurvivalGames&a has been fully enabled!"));
    }

    private void fallbackStorage() {
        getLogger().severe("Could not connect to MySQL, defaulting to no-save storage!");
        this.uhcPlayerManager = new UHCPlayerManager(new ForgetfulStorage());
        try {
            this.uhcPlayerManager.enable();
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

    /*
     * @Deprecated
     * Lilypad
     */
//    private Connect getBukkitConnect() {
//        return getServer().getServicesManager().getRegistration(Connect.class).getProvider();
//    }
//
//    public void sendToServer(String server, final Player player) {
//        try {
//            Connect c = getBukkitConnect();
//            c.request(new RedirectRequest(server, player.getName())).registerListener(redirectResult -> {
//                if (redirectResult.getStatusCode() == StatusCode.SUCCESS) {
//                    return;
//                }
//                player.sendMessage("Could not connect");
//            });
//        } catch (Exception exception) {
//            player.sendMessage("Could not connect");
//        }
//    }
}
