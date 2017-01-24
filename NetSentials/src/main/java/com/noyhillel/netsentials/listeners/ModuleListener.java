package com.noyhillel.netsentials.listeners;

import com.noyhillel.netsentials.NetSentials;
import org.bukkit.event.Listener;

public abstract class ModuleListener implements Listener {

    private final String configString;

    protected ModuleListener(String configString) {
        this.configString = configString;
    }

    public boolean register() {
        if (!NetSentials.getInstance().getConfig().getBoolean("module-listener." + configString)) return false;
        NetSentials.getInstance().registerListener(this);
        return true;
    }
}
