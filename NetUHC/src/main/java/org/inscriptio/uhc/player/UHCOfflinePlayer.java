package org.inscriptio.uhc.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = {"uuid"})
public class UHCOfflinePlayer {
    private final UUID uuid;
    private List<String> usernames;

    /* Stats to store */
    private Integer kills;
    private Integer deaths;
    private Integer wins;
    private Integer totalGames;

    /* Credits/Game stuff */
    //private Integer mutationCredits;
    private Integer points;

    /* Utils */
    protected String nick;

    public UHCPlayer getOnlinePlayer() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UHCPlayer onlinePlayer = getOnlinePlayer(player);
            if (onlinePlayer != null) return onlinePlayer;
        }
        return null;
    }

    public UHCPlayer getOnlinePlayer(Player player) {
        if (player.getUniqueId().equals(uuid)) return new UHCPlayer(player.getName(), uuid, usernames, kills, deaths, wins, totalGames, /*mutationCredits,*/ points, nick);
        return null;
    }
}
