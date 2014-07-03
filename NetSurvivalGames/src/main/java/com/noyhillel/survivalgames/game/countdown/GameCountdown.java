package com.noyhillel.survivalgames.game.countdown;

import com.noyhillel.survivalgames.SurvivalGames;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

@Data
public final class GameCountdown implements Runnable {

    private final CountdownDelegate countdownDelegate;
    private final Integer seconds;

    @Setter(AccessLevel.NONE) private Integer secondsPassed = 0; //Increases, number of seconds that have passed so far.
    @Setter(AccessLevel.NONE) private boolean running = false;
    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) private BukkitTask runningTask = null;

    @Override
    public void run() {
        secondsPassed++;
        this.countdownDelegate.countdownChanged(seconds, seconds-secondsPassed, this);
        if (this.secondsPassed >= seconds) {
            this.countdownDelegate.countdownComplete(seconds, this);
            reset();
            return;
        }
        if (this.running) reschedule();
    }

    public void start() {
        reset();
        this.running = true;
        this.countdownDelegate.countdownStarting(seconds, this);
        this.countdownDelegate.countdownChanged(seconds, seconds-secondsPassed, this);
        reschedule();
    }

    public void pause() {
        this.running = false;
    }


    public void reset() {
        pause();
        secondsPassed = 0;
    }

    private void reschedule() {
        runningTask = Bukkit.getScheduler().runTaskLater(SurvivalGames.getInstance(), this, 20L);
    }
}
