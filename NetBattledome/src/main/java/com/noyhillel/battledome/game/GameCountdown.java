package com.noyhillel.battledome.game;

import com.noyhillel.networkengine.util.NetPlugin;
import lombok.Data;
import org.bukkit.Bukkit;

@Data
public final class GameCountdown implements Runnable {

    private Integer seconds;
    private Integer passed;
    private GameCountdownHandler handler;
    private NetPlugin plugin;
    private boolean started;

    public GameCountdown(Integer seconds, GameCountdownHandler handler, NetPlugin plugin) {
        this.seconds = seconds;
        this.handler = handler;
        this.passed = 0;
        this.plugin = plugin;
        this.started = false;
    }

    public void start() {
        if (this.started) return;
        this.started = true;
        try {
            this.handler.onCountdownStart(this.seconds, this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        schedule();
    }

    @Override
    public void run() {
        passed++;
        try {
            handler.onCountdownChange(seconds-passed, seconds, this);
            if (passed.equals(seconds)) handler.onCountdownComplete(this);
            else schedule();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            schedule();
        }
    }

    private void schedule() {
        Bukkit.getScheduler().runTaskLater(this.plugin, this, 20);
    }
}