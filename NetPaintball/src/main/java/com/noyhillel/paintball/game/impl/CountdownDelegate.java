package com.noyhillel.paintball.game.impl;

/**
 * Created by Armani on 04/12/2016.
 */
public interface CountdownDelegate {

    void countdownStarting(Integer maxSeconds, GameCountdown countdown);

    void countdownChanged(Integer maxSeconds, Integer secondsRemaining, GameCountdown countdown);

    void countdownComplete(Integer maxSeconds, GameCountdown countdow);
}

