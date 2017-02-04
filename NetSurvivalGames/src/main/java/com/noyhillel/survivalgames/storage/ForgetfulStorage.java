package com.noyhillel.survivalgames.storage;

import com.noyhillel.survivalgames.player.SGOfflinePlayer;
import com.noyhillel.survivalgames.player.SGPlayer;
import com.noyhillel.survivalgames.player.PlayerNotFoundException;
import com.noyhillel.survivalgames.player.StorageError;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

@GStorageKey({"forgetful", "debug"})
public final class ForgetfulStorage implements GStorage {
    @Override
    public SGOfflinePlayer getOfflinePlayerByUUID(UUID uuid) throws PlayerNotFoundException, StorageError {
        return new SGOfflinePlayer(uuid, new ArrayList<>(), 0, 0, 0, 0, /*0,*/ 0, null);
    }

    @Override
    public SGOfflinePlayer getPlayerAllowNew(Player player) throws StorageError {
        ArrayList<String> strings = new ArrayList<>();
        strings.add(player.getName());
        return new SGOfflinePlayer(player.getUniqueId(), strings, 0, 0, 0, 0, /*0,*/ 0, null);
    }

    @Override
    public void savePlayer(SGPlayer player) throws StorageError {}

    @Override
    public void startup() throws StorageError {}

    @Override
    public void shutdown() throws StorageError {}
}
