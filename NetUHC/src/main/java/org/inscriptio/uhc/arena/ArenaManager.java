package org.inscriptio.uhc.arena;

import com.noyhillel.networkengine.exceptions.ArenaException;
import com.noyhillel.networkengine.game.arena.lobby.GameLobby;

import java.util.List;

public interface ArenaManager {
    List<Arena> getArenas();

    void saveArena(Arena arena) throws ArenaException;

    void reloadArenas() throws ArenaException;

    GameLobby getGameLobby();

    void saveGameLobby(GameLobby gameLobby) throws ArenaException;
}
