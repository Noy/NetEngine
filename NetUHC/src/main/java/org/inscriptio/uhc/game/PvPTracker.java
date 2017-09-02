package org.inscriptio.uhc.game;


import org.inscriptio.uhc.player.UHCPlayer;

import java.util.HashMap;
import java.util.Map;

public final class PvPTracker {
    private Map<UHCPlayer, UHCPlayer> playerKills = new HashMap<>();

    public void logKill(UHCPlayer killer, UHCPlayer dead) {
        this.playerKills.put(dead, killer);
    }

    public void logDeath(UHCPlayer dead) {
        this.playerKills.put(dead, null);
    }

    public UHCPlayer getPlayersKiller(UHCPlayer dead) {
        return this.playerKills.get(dead);
    }

    public void logGameKills() {
        for (Map.Entry<UHCPlayer, UHCPlayer> sgPlayerGPlayerEntry : playerKills.entrySet()) {
            UHCPlayer key = sgPlayerGPlayerEntry.getKey();
            UHCPlayer value = sgPlayerGPlayerEntry.getValue();
            if (value != null) value.setKills(value.getKills() + 1);
            key.setDeaths(key.getDeaths() + 1);
        }
    }
}
