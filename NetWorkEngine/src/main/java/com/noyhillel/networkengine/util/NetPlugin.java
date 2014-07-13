package com.noyhillel.networkengine.util;

import com.noyhillel.networkengine.command.CommandHandler;
import com.noyhillel.networkengine.command.CommandStatus;
import com.noyhillel.networkengine.command.CommandStructure;
import com.noyhillel.networkengine.command.NetCommandSenders;
import com.noyhillel.networkengine.newcommand.NetAbstractCommandHandler;
import com.noyhillel.networkengine.util.config.YAMLConfigurationFile;
import com.noyhillel.networkengine.util.effects.NetEnderHealthBarEffect;
import com.noyhillel.networkengine.util.player.NetPlayerManager;
import com.noyhillel.networkengine.util.player.NetPlayerManagerListener;
import com.noyhillel.networkengine.mongo.NetDatabase;
import com.noyhillel.networkengine.mongo.Provider;
import com.noyhillel.networkengine.util.utils.UtilListener;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Noy on 25/05/2014.
 * @author Noy Hillel
 */
public abstract class NetPlugin extends JavaPlugin implements CommandHandler {

    @Getter private static NetPlugin instance;
    @Getter public CommandStructure commandStructure = new CommandStructure(this);
    @Getter public static NetPlayerManager netPlayerManager;
    @Getter public static Random random = new Random();
    @Getter private static NetPlayerManager playerManager;
    @Getter protected YAMLConfigurationFile databaseConfiguration;
    @Getter protected NetDatabase netDatabase;
    @Getter protected Provider provider;
    @Getter private final MainClass meta = getClass().getAnnotation(MainClass.class);
    @Getter private final PluginDescriptionFile pluginDescriptionFile = getDescription();
    protected static final String RELOAD_MESSAGE = ChatColor.GRAY + "Server Reloading!";

    protected abstract void enable();
    protected abstract void disable();

    @Override
    public final void onEnable() {
        logInfoInColor(ChatColor.RED + "Attempting to link to engine...");
        try {
            NetPlugin.instance = this;
            NetPlugin.netPlayerManager = new NetPlayerManager();
            saveDefaultConfig();
            enable();
            registerListener(new UtilListener());
            this.commandStructure = new CommandStructure(this);
            NetPlugin.netPlayerManager = new NetPlayerManager();
            registerListener(new NetPlayerManagerListener(netPlayerManager));
            registerListener(new NetEnderHealthBarEffect.EnderBarListeners());
            registerPluginMeta();
        } catch (Exception ex) {
            ex.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
        logInfoInColor(ChatColor.GREEN + "Linked!");
    }

    @Override
    @SneakyThrows
    public final void onDisable() {
        reloadConfig();
        disable();
        this.commandStructure = null;
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        return this.commandStructure.onCommand(sender, command, s, strings);
    }

    @Override
    public final void onLoad() { load(); }

    protected void load() {}

    @SuppressWarnings({"ResultOfMethodCallIgnored", "UnusedDeclaration"})
    public final void deleteDirectory(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String s : children) {
                deleteDirectory(new File(file, s));
            }
        }
        file.delete();
    }

    public final <T extends Listener> T registerListener(T listener) {
        getServer().getPluginManager().registerEvents(listener, this);
        return listener;
    }

    protected final <T extends NetAbstractCommandHandler> T setupCommands(Class<T> sgCommand) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return sgCommand.getDeclaredConstructor().newInstance();
    }

    public final void registerCommands(CommandHandler handler) {
        getCommandStructure().registerHandler(handler);
    }

    @SafeVarargs
    public static <T> void logInfo(T... args) {
        for (T t : args) {
            Bukkit.getServer().getLogger().info(t.toString());
        }
    }

    @SafeVarargs
    public final <T> void logInfoInColor(T... args) {
        for (T msg : args) {
            getServer().getConsoleSender().sendMessage(msg.toString());
        }
    }

    @Override
    public void handleCommand(CommandStatus status, CommandSender sender, NetCommandSenders senders) {}

    private void registerPluginMeta() {
        registerDescription();
        registerWebsite();
        registerAuthors();
        registerName();
    }

    @SneakyThrows
    private void registerDescription() {
        Field description = pluginDescriptionFile.getClass().getDeclaredField("description");
        description.setAccessible(true);
        description.set(pluginDescriptionFile, meta.description());
    }

    @SneakyThrows
    private void registerWebsite() {
        Field website = pluginDescriptionFile.getClass().getDeclaredField("website");
        website.setAccessible(true);
        website.set(pluginDescriptionFile, meta.website());
    }

    @SneakyThrows
    private void registerAuthors() {
        Field authors = pluginDescriptionFile.getClass().getDeclaredField("authors");
        authors.setAccessible(true);
        authors.set(pluginDescriptionFile, Arrays.asList(meta.authors()));
    }

    @SneakyThrows
    private void registerName() {
        Field name = pluginDescriptionFile.getClass().getDeclaredField("name");
        name.setAccessible(true);
        name.set(pluginDescriptionFile, meta.name());
    }
}