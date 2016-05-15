package com.noyhillel.survivalgames.game;

import com.noyhillel.survivalgames.player.SGPlayer;

import java.util.HashMap;
import java.util.Map;

public final class PvPTracker {
    private Map<SGPlayer, SGPlayer> playerKills = new HashMap<>();

    public void logKill(SGPlayer killer, SGPlayer dead) {
        this.playerKills.put(dead, killer);
    }

    public void logDeath(SGPlayer dead) {
        this.playerKills.put(dead, null);
    }

    public SGPlayer getPlayersKiller(SGPlayer dead) {
        return this.playerKills.get(dead);
    }

    public void logGameKills() {
        for (Map.Entry<SGPlayer, SGPlayer> sgPlayerGPlayerEntry : playerKills.entrySet()) {
            SGPlayer key = sgPlayerGPlayerEntry.getKey();
            SGPlayer value = sgPlayerGPlayerEntry.getValue();
            if (value != null) value.setKills(value.getKills() + 1);
            key.setDeaths(key.getDeaths() + 1);
        }
    }
}
