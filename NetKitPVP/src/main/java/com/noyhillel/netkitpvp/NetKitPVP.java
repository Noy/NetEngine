package com.noyhillel.netkitpvp;

import com.noyhillel.netkitpvp.commands.DeleteKitCommand;
import com.noyhillel.netkitpvp.commands.KitCommand;
import com.noyhillel.netkitpvp.commands.SetKitCommand;
import com.noyhillel.netkitpvp.game.KitPVPGame;
import com.noyhillel.networkengine.util.MainClass;
import com.noyhillel.networkengine.util.NetPlugin;
import com.noyhillel.networkengine.util.utils.NetCoolDown;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;


/**
 * Created by Noy on 12/06/2014.
 */
@MainClass(name = "NetKitPVP", description = "The NetKitPVP Plugin")
public final class NetKitPVP extends NetPlugin {

    @Getter public static NetKitPVP instance;
    @Getter public static NetCoolDown coolDown = new NetCoolDown();

    @Override
    protected void enable() {
        for (Player player : this.getServer().getOnlinePlayers()) {
            player.kickPlayer(RELOAD_MESSAGE);
        }
        NetKitPVP.instance = this;
        registerAllCommands();
        instance.registAllListeners();
    }

    @Override
    protected void disable() {
        NetKitPVP.instance = null;
    }

    private void registerAllCommands() {
        try {
            setupCommands(KitCommand.class);
            setupCommands(SetKitCommand.class);
            setupCommands(DeleteKitCommand.class);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void registAllListeners() {
        registerListener(new KitPVPGame());
    }
}
