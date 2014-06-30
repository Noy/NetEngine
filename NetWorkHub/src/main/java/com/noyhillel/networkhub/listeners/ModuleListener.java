package com.noyhillel.networkhub.listeners;

import com.noyhillel.networkhub.NetWorkHub;
import org.bukkit.event.Listener;

/**
 * Created by Noy on 17/06/2014.
 */
public abstract class ModuleListener implements Listener {

    /**
     * Final field, the Configurable String.
     */
    private final String configString;

    /**
     * Constructor.
     * @param configString the configurable key which would either be true or false.
     */
    public ModuleListener(String configString) {
        this.configString = configString;
    }

    /**
     * Should the event be registered.
     * @return true
     */
    public boolean register() {
        if (!NetWorkHub.getInstance().getConfig().getBoolean("module-listener." + configString)) return false;
        NetWorkHub.getInstance().registerListener(this);
        return true;
    }
}