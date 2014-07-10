package com.noyhillel.netkitpvp;

import com.noyhillel.networkengine.util.NetPlugin;
import lombok.Getter;


/**
 * Created by Noy on 12/06/2014.
 */
public final class NetKitPVP extends NetPlugin {

    @Getter public static NetKitPVP instance;

    @Override
    protected void enable() {
        NetKitPVP.instance = this;
        //registerAllCommands();
    }


    @Override
    protected void disable() {
        NetKitPVP.instance = null;
    }

//    private void registerAllCommands() {
//        try {
//            setupCommands(KitCommandHandler.class);
//        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
//            e.printStackTrace();
//        }
//    }
}
