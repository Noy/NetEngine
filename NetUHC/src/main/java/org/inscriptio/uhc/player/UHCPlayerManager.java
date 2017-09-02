package org.inscriptio.uhc.player;

import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.inscriptio.uhc.NetUHC;
import org.inscriptio.uhc.storage.GStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public final class UHCPlayerManager {
    @NonNull private final GStorage storage;

    private Map<String, UHCPlayer> onlinePlayers = new HashMap<>();

    public void enable() throws StorageError {
        storage.startup();
    }

    public UHCOfflinePlayer getOfflinePlayer(UUID uuid) throws StorageError {
        try {
            return storage.getOfflinePlayerByUUID(uuid);
        } catch (PlayerNotFoundException e) {
            return null;
        }
    }

    public UHCPlayer getOnlinePlayer(String name) {
        return onlinePlayers.get(name);
    }

    public UHCPlayer getOnlinePlayer(Player p) {
        return getOnlinePlayer(p.getName());
    }

    @SneakyThrows
    void playerLoggedIn(Player player) throws StorageError {
        UHCPlayer onlinePlayer = storage.getPlayerAllowNew(player).getOnlinePlayer(player);
        onlinePlayers.put(player.getName(), onlinePlayer);
        //onlinePlayer.updateNick();
        //onlinePlayer.resetScoreboardSide();
    }


    void playerLoggedOut(Player player) {
        try {
            storage.savePlayer(onlinePlayers.get(player.getName()));
        } catch (StorageError | PlayerNotFoundException error) {
            error.printStackTrace();
            NetUHC.getInstance().getLogger().severe("Could not save player data in the database for " + player.getName() + "!");
        }
        onlinePlayers.remove(player.getName());
    }
}
