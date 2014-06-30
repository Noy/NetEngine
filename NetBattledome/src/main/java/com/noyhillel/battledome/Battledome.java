package com.noyhillel.battledome;

import com.noyhillel.battledome.commands.BGameUtils;
import com.noyhillel.networkengine.util.MainClass;
import com.noyhillel.networkengine.util.NetPlugin;
import lombok.Getter;

/**
 * Created by Noy on 6/23/2014.
 */
@MainClass(name = "NetBattledome", description = "The Net Battledome Mini-Game")
public final class Battledome extends NetPlugin {

    @Getter private static Battledome instance;

    @Override
    protected void enable() {
        Battledome.instance = this;
        BGameUtils bGameUtils = new BGameUtils();
        registerListener(bGameUtils);
        registerCommands(bGameUtils);
    }

    @Override
    protected void disable() {
        Battledome.instance = null;
    }
}