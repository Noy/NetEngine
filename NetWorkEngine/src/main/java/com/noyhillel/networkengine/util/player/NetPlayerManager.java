package com.noyhillel.networkengine.util.player;

import lombok.Data;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Noy on 15/05/2014.
 */
@Data
public final class NetPlayerManager {

    private Map<String, NetPlayer> onlinePlayers = new HashMap<>();

    public NetPlayer getOnlinePlayer(String name) {
        return onlinePlayers.get(name);
    }

    public NetPlayer getPlayer(Player player) {
        return onlinePlayers.get(player.getName());
    }

    public NetPlayer getOnlinePlayer(Player p) {
        if (p == null) return null;
        return getOnlinePlayer(p.getName());
    }

    void playerLoggedIn(Player player) {
        NetPlayer onlinePlayer = new NetPlayer(player.getName(), player.getUniqueId());
        onlinePlayers.put(player.getName(), onlinePlayer);
    }

    void playerLoggedOut(Player player) {
        onlinePlayers.remove(player.getName());
    }
}
