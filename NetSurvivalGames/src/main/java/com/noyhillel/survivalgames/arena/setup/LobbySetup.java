package com.noyhillel.survivalgames.arena.setup;

import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.arena.ArenaException;
import com.noyhillel.survivalgames.arena.Point;
import com.noyhillel.survivalgames.game.lobby.GameLobby;
import com.noyhillel.survivalgames.player.GPlayer;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

import static com.noyhillel.survivalgames.arena.setup.ArenaSetup.stackWithName;

@Data
public final class LobbySetup implements SetupSession {
    private final GPlayer player;
    private final World world;
    private List<Point> lobbySpawns = new ArrayList<>();

    public LobbySetup(GPlayer player, World world) {
        this.player = player;
        this.world = world;
        Bukkit.getPluginManager().registerEvents(this, SurvivalGames.getInstance());
    }

    @Override
    public void commit() throws ArenaException {
        if (lobbySpawns.size() == 0) throw new ArenaException(null, null, "You must setup a spawn point!");
        GameLobby gameLobby = new GameLobby(lobbySpawns, world);
        SurvivalGames.getInstance().getArenaManager().saveGameLobby(gameLobby);
    }

    @Override
    public void start() {
        Player p = player.getPlayer();
        player.resetPlayer();
        p.setAllowFlight(true);
        p.setGameMode(GameMode.CREATIVE);
        player.playSound(Sound.LEVEL_UP);
        p.getInventory().addItem(stackWithName(Material.WOOD_SPADE, ChatColor.RED + "Spawn point selector")); // Tier 1
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player1 = event.getPlayer();
        if (player1.getItemInHand() == null) return;
        if (player1.getItemInHand().getItemMeta() == null) return;
        if (!player1.getName().equals(player.getPlayer().getName())) return;
        if (action == Action.PHYSICAL) return;
        Point actualLocation = Point.of(player1.getLocation());
        switch (event.getItem().getType()) {
            case WOOD_SPADE:
                lobbySpawns.add(actualLocation);
                player.sendMessage(ChatColor.GREEN + "Selected.");
                break;
            default:
                return;
        }
        event.setCancelled(true);
    }
}