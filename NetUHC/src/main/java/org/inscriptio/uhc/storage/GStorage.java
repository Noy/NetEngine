package org.inscriptio.uhc.storage;

import org.bukkit.entity.Player;
import org.inscriptio.uhc.player.PlayerNotFoundException;
import org.inscriptio.uhc.player.UHCOfflinePlayer;
import org.inscriptio.uhc.player.UHCPlayer;
import org.inscriptio.uhc.player.StorageError;

import java.util.UUID;

public interface GStorage {
    UHCOfflinePlayer getOfflinePlayerByUUID(UUID uuid) throws PlayerNotFoundException, StorageError;
    UHCOfflinePlayer getPlayerAllowNew(Player player) throws StorageError;

    void savePlayer(UHCPlayer player) throws StorageError, PlayerNotFoundException;

    void startup() throws StorageError;
    void shutdown() throws StorageError;

//    boolean isInDatabase(UHCPlayer player) throws SQLException;
}
