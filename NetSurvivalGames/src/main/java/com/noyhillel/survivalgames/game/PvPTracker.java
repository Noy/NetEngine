package com.noyhillel.survivalgames.game;

import com.noyhillel.survivalgames.player.GPlayer;

import java.util.HashMap;
import java.util.Map;

public final class PvPTracker {
    private Map<GPlayer, GPlayer> playerKills = new HashMap<>();

    public void logKill(GPlayer killer, GPlayer dead) {
        this.playerKills.put(dead, killer);
    }

    public void logDeath(GPlayer dead) {
        this.playerKills.put(dead, null);
    }

    public GPlayer getPlayersKiller(GPlayer dead) {
        return this.playerKills.get(dead);
    }

    public void logGameKills() {
        for (Map.Entry<GPlayer, GPlayer> gPlayerGPlayerEntry : playerKills.entrySet()) {
            GPlayer key = gPlayerGPlayerEntry.getKey();
            GPlayer value = gPlayerGPlayerEntry.getValue();
            if (value != null) value.setKills(value.getKills() + 1);
            key.setDeaths(key.getDeaths() + 1);
        }
    }
}
