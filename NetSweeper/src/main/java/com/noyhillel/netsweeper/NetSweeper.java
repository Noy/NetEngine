package com.noyhillel.netsweeper;

import com.noyhillel.networkengine.util.MainClass;
import com.noyhillel.networkengine.util.NetPlugin;
import lombok.Getter;

/**
 * Created by Armani on 01/02/2017.
 */
@MainClass(name = "NetSweeper", description = "The Net Minesweeper minigame")
public final class NetSweeper extends NetPlugin {

    @Getter private static NetSweeper instance;

    @Override
    protected void enable() {
        try {
            NetSweeper.instance = this;
        }catch (Exception e) {

        }
    }

    @Override
    protected void disable() {

    }
}
