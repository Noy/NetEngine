package com.noyhillel.battledome.game;

public interface GameCountdownHandler {
    void onCountdownStart(Integer max, GameCountdown countdown);
    void onCountdownChange(Integer seconds, Integer max, GameCountdown countdown);
    void onCountdownComplete(GameCountdown countdown);
}