package com.noyhillel.survivalgames.game.countdown;

public interface CountdownDelegate {

    void countdownStarting(Integer maxSeconds, GameCountdown countdown);

    void countdownChanged(Integer maxSeconds, Integer secondsRemaining, GameCountdown countdown);

    void countdownComplete(Integer maxSeconds, GameCountdown countdown);
}
