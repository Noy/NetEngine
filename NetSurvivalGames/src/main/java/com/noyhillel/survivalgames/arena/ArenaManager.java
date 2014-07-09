package com.noyhillel.survivalgames.arena;

import com.noyhillel.survivalgames.game.lobby.GameLobby;

import java.util.List;

public interface ArenaManager {
    List<Arena> getArenas();

    void saveArena(Arena arena) throws ArenaException;

    void reloadArenas() throws ArenaException;

    GameLobby getGameLobby();

    void saveGameLobby(GameLobby gameLobby) throws ArenaException;
}
