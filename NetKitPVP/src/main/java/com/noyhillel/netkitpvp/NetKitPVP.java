package com.noyhillel.netkitpvp;

import com.noyhillel.netkitpvp.commands.DeleteKitCommand;
import com.noyhillel.netkitpvp.commands.KitCommand;
import com.noyhillel.netkitpvp.commands.SetKitCommand;
import com.noyhillel.networkengine.util.MainClass;
import com.noyhillel.networkengine.util.NetPlugin;
import com.noyhillel.networkengine.util.utils.NetWorkCoolDown;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;


/**
 * Created by Noy on 12/06/2014.
 */
@MainClass(name = "NetKitPVP", description = "The NetKitPVP Plugin")
public final class NetKitPVP extends NetPlugin {

    @Getter public static NetKitPVP instance;
    @Getter public static NetWorkCoolDown netWorkCoolDown = new NetWorkCoolDown();

    @Override
    protected void enable() {
        NetKitPVP.instance = this;
        registerAllCommands();
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
}
