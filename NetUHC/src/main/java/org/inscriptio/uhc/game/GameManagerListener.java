package org.inscriptio.uhc.game;

import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.inscriptio.uhc.NetUHC;
import org.inscriptio.uhc.game.impl.UHCGame;
import org.inscriptio.uhc.player.UHCPlayer;
import org.inscriptio.uhc.utils.MessageManager;

import java.util.Set;

@Data
public final class GameManagerListener implements Listener {

    private final NetUHC plugin;
    private final GameManager gameManager;

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Set<UHCPlayer> players = gameManager.getPlayers();
        String permission = "survivalgames.vip";
        if (((players.size()+4) == gameManager.getMaxPlayers()) && !event.getPlayer().hasPermission(permission)) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(MessageManager.getFormat("formats.vip-kick", false));
            return;
        }
        if((players.size()+1) == gameManager.getMaxPlayers()) {
            if (event.getPlayer().hasPermission(permission)) {
                for (UHCPlayer uhcPlayer : players) {
                    Player player = uhcPlayer.getPlayer();
                    if(player.hasPermission(permission)) continue;
                    player.kickPlayer(MessageManager.getFormat("formats.kicked-for-vip", false));
                    break;
                }
            } else {
                event.setResult(PlayerLoginEvent.Result.KICK_FULL);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(MessageManager.getFormat("formats.join-msg", false, new String[]{"<player>", event.getPlayer().getName()}));
        try {
            gameManager.playerJoined(resolveGPlayer(event.getPlayer()));
        } catch (Exception e) {
            e.printStackTrace();
            event.getPlayer().kickPlayer(ChatColor.RED + "Error handing your login, please consult the server administrator!\n" + e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.setQuitMessage(MessageManager.getFormat("formats.quit-msg", false, new String[]{"<player>", event.getPlayer().getName()}));
        gameManager.playerLeft(resolveGPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerFoodDecrease(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        UHCGame.GameState gameState = UHCGame.gameState;
        if (gameState == UHCGame.GameState.GAMEPLAY) return;
        event.setCancelled(true);
        //if (!players.contains(getGPlayer((Player) event.getEntity()))) event.setCancelled(true);
    }

    @EventHandler
    public void onLeafDecay(LeavesDecayEvent event) {
        if (event.getBlock().getLocation().getWorld().equals(gameManager.getLobby().getLoadedWorld())) event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) { event.getEntity().remove(); }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        if (event.getPlayer().isOp()) {
            if (event.getBlock().getType() == Material.SIGN) return;
            if (event.getBlock().getType() == Material.SIGN_POST) return;
            if (event.getBlock().getType() == Material.WALL_SIGN) return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UHCPlayer uhcPlayer = resolveGPlayer(event.getPlayer());
        String formatName = "chat.lobby-chat";
        event.setCancelled(true);
        String s = MessageManager.getFormat(formatName, false, new String[]{"<player>", uhcPlayer.getDisplayableName()}) + event.getMessage();
        for (UHCPlayer player : NetUHC.getInstance().getUhcPlayerManager().getOnlinePlayers().values()) {
            player.sendMessage(s);
        }
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        if (event.getPlayer().isOp()) {
            if (event.getBlock().getType() == Material.SIGN_POST) return;
            if (event.getBlock().getType() == Material.WALL_SIGN) return;
            if (event.getBlock().getType() == Material.SIGN) return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (gameManager.isPlayingGame()) return;
        Player p = event.getPlayer();
        if (p.getLocation().getBlockY() < 0) {
            gameManager.spawnPlayer(p);
        }
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (gameManager.isPlayingGame()) return;
        if (!(event.getEntity() instanceof Player)) return;
        switch (event.getCause()) {
            case VOID:
            case SUICIDE:
            case CUSTOM:
                event.setCancelled(false);
                return;
            default:
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (gameManager.isPlayingGame()) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemMoveInventory(InventoryClickEvent event) {
        if (gameManager.isPlayingGame() && !UHCGame.getSpectators().contains(resolveGPlayer((Player) event.getWhoClicked()))) return;
        event.setCancelled(true);
    }

    private UHCPlayer resolveGPlayer(Player player) {
        return plugin.getUhcPlayerManager().getOnlinePlayer(player);
    }
}
