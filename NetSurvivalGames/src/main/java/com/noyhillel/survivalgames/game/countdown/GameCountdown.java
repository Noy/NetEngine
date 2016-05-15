package com.noyhillel.survivalgames.game.countdown;

import com.noyhillel.survivalgames.SurvivalGames;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

@Data
@ToString(of = "runningTask")
public final class GameCountdown implements Runnable {

    private final CountdownDelegate countdownDelegate;
    private final Integer seconds;

    @Setter(AccessLevel.NONE) private Integer secondsPassed = 0; //Increases, number of seconds that have passed so far.
    @Setter(AccessLevel.NONE) private boolean running = false;
    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) private BukkitTask runningTask;

    @Override
    public void run() {
        secondsPassed++;
        this.countdownDelegate.countdownChanged(seconds, seconds-secondsPassed, this);
        if (this.secondsPassed >= seconds) {
            this.countdownDelegate.countdownComplete(seconds, this);
            reset();
            return;
        }
        if (this.running) runningTask = Bukkit.getScheduler().runTaskLater(SurvivalGames.getInstance(), this, 20L);
    }

    public void start() {
        reset();
        this.running = true;
        this.countdownDelegate.countdownStarting(seconds, this);
        this.countdownDelegate.countdownChanged(seconds, seconds-secondsPassed, this);
        runningTask = Bukkit.getScheduler().runTaskLater(SurvivalGames.getInstance(), this, 20L);
    }

    private void pause() {
        this.running = false;
    }

    private void reset() {
        pause();
        secondsPassed = 0;
    }

    public void cancel() {
        if (runningTask != null) runningTask.cancel();
        runningTask = null;
        this.running = false;
    }
}
