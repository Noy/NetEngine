package com.noyhillel.survivalgames.storage;

import com.noyhillel.survivalgames.player.GOfflinePlayer;
import com.noyhillel.survivalgames.player.GPlayer;
import com.noyhillel.survivalgames.player.PlayerNotFoundException;
import com.noyhillel.survivalgames.player.StorageError;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@GStorageKey({"forgetful", "debug"})
public final class ForgetfulStorage implements GStorage {
    @Override
    public GOfflinePlayer getOfflinePlayerByUUID(String uuid) throws PlayerNotFoundException, StorageError {
        return new GOfflinePlayer(uuid, new ArrayList<String>(), 0, 0, 0, 0, 0, 0, null);
    }

    @Override
    public GOfflinePlayer getPlayerAllowNew(Player player) throws StorageError {
        ArrayList<String> strings = new ArrayList<>();
        strings.add(player.getName());
        return new GOfflinePlayer(player.getUniqueId().toString(), strings, 0, 0, 0, 0, 0, 0, null);
    }

    @Override
    public void savePlayer(GPlayer player) throws StorageError {}

    @Override
    public void startup() throws StorageError {}

    @Override
    public void shutdown() throws StorageError {}
}
