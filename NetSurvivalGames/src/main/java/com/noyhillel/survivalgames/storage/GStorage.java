package com.noyhillel.survivalgames.storage;

import com.noyhillel.survivalgames.player.GOfflinePlayer;
import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.player.PlayerNotFoundException;
import com.noyhillel.survivalgames.player.StorageError;
import org.bukkit.entity.Player;

public interface GStorage {
    GOfflinePlayer getOfflinePlayerByUUID(String uuid) throws PlayerNotFoundException, StorageError;
    GOfflinePlayer getPlayerAllowNew(Player player) throws StorageError;

    void savePlayer(GPlayer player) throws StorageError, PlayerNotFoundException;

    void startup() throws StorageError;
    void shutdown() throws StorageError;
}
