package com.noyhillel.battledome.game;

import com.noyhillel.battledome.Battledome;
import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Map;

/**
 * Created by Noy on 6/23/2014.
 */
public enum Phase {
    BUILD(new GameListenerDelegate() {
        @Override
        public boolean canPvp() {
            return false;
        }

        @Override
        public boolean makeSpectatorOnDeath() {
            return false;
        }

        @Override
        public boolean canBreakObsi() {
            return false;
        }

        @Override
        public boolean joinAsSpectator() {
            return true;
        }

        @Override
        public void update(BGame game) {

        }
    }, Battledome.getInstance().getConfig().getInt("formats.build-phase-time", 600), "Build Phase"),
    BATTLE(new GameListenerDelegate() {
        @Override
        public boolean canPvp() {
            return true;
        }

        @Override
        public boolean makeSpectatorOnDeath() {
            return true;
        }

        @Override
        public boolean canBreakObsi() {
            return true;
        }

        @Override
        public boolean joinAsSpectator() {
            return true;
        }

        @Override
        public void update(BGame game) {
            for (Map.Entry<Team, NetPlayer> netPlayerEntry : new HashSet<>(game.getObsidianHolders().entrySet())) {
                game.placeObsidian(netPlayerEntry.getKey(), netPlayerEntry.getValue().getPlayer().getLocation());
                netPlayerEntry.getValue().removeItem(Material.OBSIDIAN);
            }

        }
    }, -1, "Battle Phase");

    private final GameListenerDelegate gameListenerDelegate;
    private final Integer length;
    private final String name;

    Phase(GameListenerDelegate gameListenerDelegate, Integer length, String name) {
        this.gameListenerDelegate = gameListenerDelegate;
        this.length = length;
        this.name = name;
    }


    public GameListenerDelegate getGameListenerDelegate() {
        return gameListenerDelegate;
    }

    public Integer getLength() {
        return length;
    }

    public String getName() {
        return name;
    }
}
