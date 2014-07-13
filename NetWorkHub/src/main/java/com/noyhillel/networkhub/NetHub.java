package com.noyhillel.networkhub;

import com.noyhillel.networkengine.util.MainClass;
import com.noyhillel.networkengine.util.NetPlugin;
import com.noyhillel.networkengine.util.config.YAMLConfigurationFile;
import com.noyhillel.networkengine.exceptions.DatabaseConnectException;
import com.noyhillel.networkengine.mongo.DefaultProvider;
import com.noyhillel.networkengine.mongo.Provider;
import com.noyhillel.networkengine.util.utils.NetCoolDown;
import com.noyhillel.networkhub.commands.*;
import com.noyhillel.networkhub.items.HubItemJoinListener;
import com.noyhillel.networkhub.commands.WarpItemCommands;
import com.noyhillel.networkhub.listeners.*;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Noy on 26/05/2014.
 */
@MainClass(name = "NetHub", description = "NetHub Plugin.")
public final class NetHub extends NetPlugin {

    @Getter private static NetHub instance;
    @Getter private static NetCoolDown cooldown;

    @Override
    protected void enable() {
        try {
            NetHub.instance = this;
            Integer delay = getConfig().getInt("announcer.delay", 60);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new Announcer(this), 20 * delay, 20 * delay);
            logInfoInColor(ChatColor.GREEN + "Hub Plugin >> Successfully enabled!");
            registerAllListeners();
            registerAllNetCommands();
            registerOtherCommands();
            setupDatabase();
            cooldown = new NetCoolDown();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.getPlayer().kickPlayer(RELOAD_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void disable() {
        logInfoInColor(ChatColor.DARK_RED + "Hub Plugin >> Successfully disabled!");
    }

    private void registerAllListeners() {
        new UtilListeners().register();
        new NoDrop().register();
        new LeaveListener().register();
        new HubItemJoinListener().register();
        new CommandSpyListener().register();
        new ColoredSigns().register();
        new ChatListener().register();
        new BouncyPads().register();
        new BelowZero().register();
    }

    private void registerAllNetCommands() {
        registerCommands(new WarpItemCommands());
        registerCommands(new SpawnCommand());
        registerCommands(new ClearChatCommand());
        registerCommands(new CommandSpyCommand());
        registerCommands(new NickNameCommand());
        registerCommands(new FlyCommand());
        registerCommands(new WhoIsCommand());
    }

    @SneakyThrows
    private void registerOtherCommands() {
        setupCommands(GiveMeItemCommand.class);
        setupCommands(GetInfoCommand.class);
    }

    private void setupDatabase() throws DatabaseConnectException {
        try {
            provider = (Provider) Class.forName(getConfig().getString("provider")).newInstance();
        } catch (Exception e) {
            provider = new DefaultProvider();
        }
        databaseConfiguration = new YAMLConfigurationFile(this, "database.yml");
        databaseConfiguration.reloadConfig();
        databaseConfiguration.saveDefaultConfig();
        this.netDatabase = provider.getNewDatabase(this);
        this.netDatabase.connect();
    }
}