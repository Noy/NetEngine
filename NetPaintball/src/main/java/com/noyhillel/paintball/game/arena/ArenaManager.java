package com.noyhillel.paintball.game.arena;

import com.noyhillel.networkengine.exceptions.ArenaException;

import java.util.List;

/**
 * Created by Armani on 04/12/2016.
 */
public interface ArenaManager {
    List<Arena> getArenas();

    void saveArena(Arena arena) throws ArenaException;

    void reloadArenas() throws ArenaException;
}

