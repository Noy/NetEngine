package com.noyhillel.survivalgames.storage;

import com.noyhillel.survivalgames.player.SGOfflinePlayer;
import com.noyhillel.survivalgames.player.SGPlayer;
import com.noyhillel.survivalgames.player.PlayerNotFoundException;
import com.noyhillel.survivalgames.player.StorageError;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface GStorage {
    SGOfflinePlayer getOfflinePlayerByUUID(UUID uuid) throws PlayerNotFoundException, StorageError;
    SGOfflinePlayer getPlayerAllowNew(Player player) throws StorageError;

    void savePlayer(SGPlayer player) throws StorageError, PlayerNotFoundException;

    void startup() throws StorageError;
    void shutdown() throws StorageError;

//    boolean isInDatabase(SGPlayer player) throws SQLException;
}
