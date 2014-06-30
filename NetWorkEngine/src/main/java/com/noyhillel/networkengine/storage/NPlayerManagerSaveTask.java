package com.noyhillel.networkengine.storage;

import com.noyhillel.networkengine.util.NetPlugin;
import com.noyhillel.networkengine.util.player.mongo.DatabaseConnectException;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Noy on 12/06/2014.
 */
@Data
public final class NPlayerManagerSaveTask implements Runnable {

    private final NPlayerManager manager;

    @Override
    public void run() {
        List<NPlayer> failedToSave = new ArrayList<>();
        int savedPlayers = 0;
        synchronized (manager) {
            for (NPlayer nPlayer : manager.getOnlinePlayers()) {
                try {
                    nPlayer.saveIntoDatabase();
                    savedPlayers++;
                } catch (DatabaseConnectException e) {
                    failedToSave.add(nPlayer);
                    e.printStackTrace();
                }
            }
        }
        for (NPlayer cPlayer : failedToSave) {
            NetPlugin.logInfo("Failed to save " + cPlayer.toString());
        }
        if (savedPlayers > 0) NetPlugin.logInfo("Saved " + savedPlayers + " players!");
    }
}
