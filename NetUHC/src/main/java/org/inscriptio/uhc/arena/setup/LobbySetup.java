package org.inscriptio.uhc.arena.setup;

import com.noyhillel.networkengine.exceptions.ArenaException;
import com.noyhillel.networkengine.game.arena.Point;
import com.noyhillel.networkengine.game.arena.lobby.GameLobby;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.inscriptio.uhc.NetUHC;
import org.inscriptio.uhc.player.UHCPlayer;

import java.util.ArrayList;
import java.util.List;

import static org.inscriptio.uhc.arena.setup.ArenaSetup.itemStackWithName;

@Data
public final class LobbySetup implements SetupSession {
    private final UHCPlayer player;
    private final World world;
    private List<Point> lobbySpawns = new ArrayList<>();

    public LobbySetup(UHCPlayer player, World world) {
        this.player = player;
        this.world = world;
        Bukkit.getPluginManager().registerEvents(this, NetUHC.getInstance());
    }

    @Override
    public void commit() throws ArenaException {
        if (lobbySpawns.size() == 0) throw new ArenaException(null, null, "You must setup a spawn point!");
        GameLobby gameLobby = new GameLobby(lobbySpawns, world);
        NetUHC.getInstance().getArenaManager().saveGameLobby(gameLobby);
    }

    @Override
    public void start() {
        Player p = player.getPlayer();
        player.resetPlayer();
        p.setAllowFlight(true);
        p.setGameMode(GameMode.CREATIVE);
        player.playSound(Sound.ENTITY_PLAYER_LEVELUP, 0.1F);
        p.getInventory().addItem(itemStackWithName(Material.WOOD_SPADE, ChatColor.RED + "Spawn point selector"));
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