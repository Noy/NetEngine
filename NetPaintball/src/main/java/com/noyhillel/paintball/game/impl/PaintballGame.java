package com.noyhillel.paintball.game.impl;

import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.paintball.Paintball;
import com.noyhillel.paintball.game.GameException;
import com.noyhillel.paintball.game.arena.Arena;
import lombok.*;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * Created by Lacoste on 9/17/2016.
 */
@Data
public class PaintballGame implements Listener {

    public enum GameState {
        WARM_UP,
        GAME,
        END
    }

    private final Arena arena;
    private final Set<NetPlayer> initialPlayers;
    private final Paintball plugin;
    private Map<NetPlayer, Team> teams;

    public PaintballGame(Arena arena, Set<NetPlayer> initialPlayers, Paintball plugin) {
        this.arena = arena;
        this.initialPlayers = initialPlayers;
        this.plugin = plugin;
        players.addAll(initialPlayers);
        plugin.registerListener(this);
    }


    public static GameState gameState = GameState.WARM_UP;

    @Setter(AccessLevel.NONE) @Getter(AccessLevel.NONE) private World arenaWorld;

    private final Set<NetPlayer> players = new HashSet<>();

    private Team winningTeam = null;

    public void start() throws GameException {
        if (!arena.isLoaded()) throw new GameException(null, this, "The world for the arena is not loaded!");
        arenaWorld = arena.getLoadedWorld();
        for (Entity mob : this.arenaWorld.getEntities()) {
            EntityType type = mob.getType();
            if (type == EntityType.SKELETON || type == EntityType.ZOMBIE || type == EntityType.SPIDER || type== EntityType.CREEPER ||
                    type == EntityType.ENDERMAN || type == EntityType.WITCH || type == EntityType.SLIME) {
                mob.remove();
            }
        }
        putPlayerInTeam();
        for (NetPlayer initialPlayer : initialPlayers) {
            players.add(initialPlayer);
            initialPlayer.resetPlayer();
        }
        updateInterfaces();
        Paintball.logInfo("Starting game with " + players.size() + " players!");
        updateState();
    }

    public void playerLeftServer(NetPlayer player) {
        this.players.remove(player);
        if (gameState == GameState.END) return;
        updateInterfaces();
    }

    private void putPlayerInTeam() {
        this.teams = new HashMap<>();
        ArrayList<NetPlayer> netPlayers = new ArrayList<>(players);
        Collections.shuffle(netPlayers);
        for (NetPlayer player : netPlayers) {
            teams.put(player, smallestTeam());
        }
    }

    private Team smallestTeam() {
        Team t = null;
        for (Team team : Team.values()) {
            if (t == null || getPlayersForTeam(team).size() < getPlayersForTeam(t).size()) t = team;
        }
        return t;
    }

    private List<NetPlayer> getPlayersForTeam(Team team) {
        List<NetPlayer> playerList = new ArrayList<>();
        for (Map.Entry<NetPlayer, Team> netPlayerTeamEntry : teams.entrySet()) {
            if (netPlayerTeamEntry.getValue().equals(team)) playerList.add(netPlayerTeamEntry.getKey());
        }
        return playerList;
    }

    private void broadcast(String... messages) {
        for (String message : messages) {
            for (NetPlayer sgPlayer : getAllPlayers()) {
                sgPlayer.sendMessage(message);
            }
            plugin.logInfoInColor(messages);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player1 = event.getPlayer();
        NetPlayer player = NetPlayer.getPlayerFromPlayer(player1);
        playerDied(player);
    }

    @EventHandler
    public void onPlayerLeave(PlayerKickEvent event) {
        Player player1 = event.getPlayer();
        NetPlayer player = NetPlayer.getPlayerFromPlayer(player1);
        playerDied(player);
    }


    void updateInterfaces() {

    }

    void broadcastSound(Sound sound, float v) {
        for (NetPlayer p : getPlayers()) {
            p.playSound(sound);
        }
    }

    public void updateState() {
    }

    public Team getTeamFor(NetPlayer player) {
        return this.teams.get(player);
    }

    void playerDied(NetPlayer player) {
        if (!(gameState == GameState.GAME || !(gameState == GameState.WARM_UP))) return;
        if (!players.contains(player)) return;
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer != null) arenaWorld.strikeLightningEffect(bukkitPlayer.getLocation());
        if (getTeamFor(player) == Team.RED) {
            player.teleport(arena.getRedTeamSpawn());
            return;
        } else if (getTeamFor(player) == Team.BLUE) {
            player.teleport(arena.getBlueTeamSpawn());
            return;
        }
        updateInterfaces();
    }

    // util methods

    private Set<NetPlayer> getAllPlayers() {
        Set<NetPlayer> players = new HashSet<>();
        players.addAll(this.players);
        return players;
    }
}
