package com.noyhillel.netsentials;

import com.noyhillel.netsentials.commands.Home;
import com.noyhillel.netsentials.commands.Message;
import com.noyhillel.netsentials.commands.Reply;
import com.noyhillel.netsentials.commands.Spawn;
import com.noyhillel.netsentials.listeners.Chat;
import com.noyhillel.netsentials.listeners.Join;
import com.noyhillel.netsentials.listeners.Leave;
import com.noyhillel.networkengine.util.MainClass;
import com.noyhillel.networkengine.util.NetPlugin;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@MainClass(name = "NetSentials", description = "The Net-Essentials esk Plugin")
public final class NetSentials extends NetPlugin {

     @Getter public static NetSentials instance;

    @Override
    protected void enable() {
        try {
            NetSentials.instance = this;
            registerAllCommands();
            registerAllListeners();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.getPlayer().kickPlayer(RELOAD_MESSAGE);
            }
        }catch (Exception e) {
            instance = null;
        }
    }

    @Override
    protected void disable() {

    }

    @SneakyThrows
    private void registerAllCommands() {
        registerCommands(new Spawn());
        registerCommands(new Home());
        setupCommands(Message.class);
        setupCommands(Reply.class);
    }

    private void registerAllListeners() {
        new Join().register();
        new Chat().register();
        new Leave().register();
    }
}
