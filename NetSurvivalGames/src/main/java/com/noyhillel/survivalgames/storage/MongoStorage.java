package com.noyhillel.survivalgames.storage;

import com.noyhillel.survivalgames.player.GOfflinePlayer;
import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.player.PlayerNotFoundException;
import com.noyhillel.survivalgames.player.StorageError;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

//TODO Mongo
@GStorageKey({"mongo", "mango"})
@RequiredArgsConstructor
public class MongoStorage implements GStorage {

    private static enum DatabaseKeys {
        PLAYERS_TABLE("players"),
        UUID("id"),
        USERNAMES("usernames"),
        POINTS("points"),
        KILLS("kills"),
        DEATHS("deaths"),
        WINS("wins"),
        TOTAL_GAMES("totalgames"),
        MUTATION_CREDITS("mutationcredits"),
        NICK("nick");
        private String key;
        DatabaseKeys(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return this.key;
        }

        public String getKey() {
            return this.key;
        }
    }

    private final String host;
    private final Integer port;
    private final String database;
    private final String username;
    private final String password;

    @Override
    public GOfflinePlayer getOfflinePlayerByUUID(String uuid) throws PlayerNotFoundException, StorageError {
        return null;
    }

    @Override
    public GOfflinePlayer getPlayerAllowNew(Player player) throws StorageError {
        return null;
    }

    @Override
    public void savePlayer(GPlayer player) throws StorageError, PlayerNotFoundException {

    }

    @Override
    public void startup() throws StorageError {

    }

    @Override
    public void shutdown() throws StorageError {

    }
}
