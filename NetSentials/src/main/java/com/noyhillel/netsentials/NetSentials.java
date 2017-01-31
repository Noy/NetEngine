package com.noyhillel.netsentials;

import com.noyhillel.netsentials.commands.*;
import com.noyhillel.netsentials.listeners.*;
import com.noyhillel.networkengine.util.MainClass;
import com.noyhillel.networkengine.util.NetPlugin;
import com.noyhillel.networkengine.util.utils.NetCoolDown;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@MainClass(name = "NetSentials", description = "The Net-Essentials esk Plugin")
public final class NetSentials extends NetPlugin {

    @Getter public static NetSentials instance;
    @Getter private static String prefix;
    @Getter private static NetCoolDown cooldown;

    @Override
    protected void enable() {
        try {
            NetSentials.instance = this;
            prefix = MessageManager.getFormats("");
            registerAllCommands();
            registerAllListeners();
            cooldown = new NetCoolDown();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.getPlayer().kickPlayer(RELOAD_MESSAGE);
            }
        }catch (Exception e) {
            instance = null;
        }
    }

    @Override
    protected void disable() {}

    @SneakyThrows
    private void registerAllCommands() {
        registerCommands(new Spawn());
        registerCommands(new Home());
        setupCommands(Message.class);
        setupCommands(Reply.class);
        setupCommands(I.class);
        setupCommands(Fly.class);
        setupCommands(Heal.class);
        setupCommands(Feed.class);
        setupCommands(Back.class);
        setupCommands(TP.class);
    }

    private void registerAllListeners() {
        new Join().register();
        new Chat().register();
        new Leave().register();
        new Death().register();
        new Teleport().register();
    }
}
