package org.inscriptio.uhc.arena.setup;

import com.noyhillel.networkengine.exceptions.ArenaException;
import org.bukkit.event.Listener;

public interface SetupSession extends Listener {
    void commit() throws ArenaException;
    void start();
}