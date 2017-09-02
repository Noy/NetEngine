package org.inscriptio.uhc.storage;

import org.bukkit.entity.Player;
import org.inscriptio.uhc.player.PlayerNotFoundException;
import org.inscriptio.uhc.player.UHCOfflinePlayer;
import org.inscriptio.uhc.player.UHCPlayer;
import org.inscriptio.uhc.player.StorageError;

import java.util.ArrayList;
import java.util.UUID;

@GStorageKey({"forgetful", "debug"})
public final class ForgetfulStorage implements GStorage {
    @Override
    public UHCOfflinePlayer getOfflinePlayerByUUID(UUID uuid) throws PlayerNotFoundException, StorageError {
        return new UHCOfflinePlayer(uuid, new ArrayList<>(), 0, 0, 0, 0, /*0,*/ 0, null);
    }

    @Override
    public UHCOfflinePlayer getPlayerAllowNew(Player player) throws StorageError {
        ArrayList<String> strings = new ArrayList<>();
        strings.add(player.getName());
        return new UHCOfflinePlayer(player.getUniqueId(), strings, 0, 0, 0, 0, /*0,*/ 0, null);
    }

    @Override
    public void savePlayer(UHCPlayer player) throws StorageError {}

    @Override
    public void startup() throws StorageError {}

    @Override
    public void shutdown() throws StorageError {}
}
