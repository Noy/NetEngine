package com.noyhillel.survivalgames.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = {"uuid"})
public class GOfflinePlayer {
    private final String uuid;
    private List<String> usernames;

    /* Stats to store */
    private Integer kills;
    private Integer deaths;
    private Integer wins;
    private Integer totalGames;

    /* Credits/Game stuff */
    private Integer mutationCredits;
    private Integer points;

    /* Utils */
    protected String nick;

    public GPlayer getOnlinePlayer() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            GPlayer onlinePlayer = getOnlinePlayer(player);
            if (onlinePlayer != null) return onlinePlayer;
        }
        return null;
    }

    public GPlayer getOnlinePlayer(Player player) {
        if (player.getUniqueId().toString().equals(uuid)) return new GPlayer(player.getName(), uuid, usernames, kills, deaths, wins, totalGames, mutationCredits, points, nick);
        return null;
    }
}
