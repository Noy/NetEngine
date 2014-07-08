package com.noyhillel.survivalgames.arena.setup;

import com.noyhillel.survivalgames.arena.ArenaException;
import org.bukkit.event.Listener;


/**
 * Created by Noy on 06/06/2014.
 */
public interface SetupSession extends Listener {
    void commit() throws ArenaException;
    void start();
}