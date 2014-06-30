package com.noyhillel.battledome.game;

/**
 * Created by Noy on 6/23/2014.
 */
interface GameListenerDelegate {
    boolean canPvp();
    boolean makeSpectatorOnDeath();
    boolean canBreakObsi();
    boolean joinAsSpectator();
    void update(BGame game);
}