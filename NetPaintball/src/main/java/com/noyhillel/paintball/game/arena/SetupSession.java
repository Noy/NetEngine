package com.noyhillel.paintball.game.arena;

import com.noyhillel.networkengine.exceptions.ArenaException;
import org.bukkit.event.Listener;

/**
 * Created by Armani on 04/12/2016.
 */
public interface SetupSession extends Listener {
    void commit() throws ArenaException;
    void start();
}
