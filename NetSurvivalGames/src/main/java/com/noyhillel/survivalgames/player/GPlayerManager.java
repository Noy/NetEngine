package com.noyhillel.survivalgames.player;

import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.storage.GStorage;
import lombok.Data;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@Data
public final class GPlayerManager {
    @NonNull private final GStorage storage;

    private Map<String, GPlayer> onlinePlayers = new HashMap<>();

    public void enable() throws StorageError {
        storage.startup();
    }

    public GOfflinePlayer getOfflinePlayer(String uuid) throws StorageError {
        try {
            return storage.getOfflinePlayerByUUID(uuid);
        } catch (PlayerNotFoundException e) {
            return null;
        }
    }

    public GPlayer getOnlinePlayer(String name) {
        return onlinePlayers.get(name);
    }

    public GPlayer getOnlinePlayer(Player p) {
        return getOnlinePlayer(p.getName());
    }

    void playerLoggedIn(Player player) throws StorageError {
        GPlayer onlinePlayer = storage.getPlayerAllowNew(player).getOnlinePlayer(player);
        onlinePlayers.put(player.getName(), onlinePlayer);
        //onlinePlayer.updateNick();
        //onlinePlayer.resetScoreboardSide();
    }

    void playerLoggedOut(Player player) {
        try {
            storage.savePlayer(onlinePlayers.get(player.getName()));
        } catch (StorageError | PlayerNotFoundException error) {
            error.printStackTrace();
            SurvivalGames.getInstance().getLogger().severe("Could not save player data in the database for " + player.getName() + "!");
        }
        onlinePlayers.remove(player.getName());
    }
}
