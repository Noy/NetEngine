package com.noyhillel.networkengine.util.effects;

import com.noyhillel.networkengine.util.packets.FakeEntity;
import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Noy on 26/05/13.
 */
public final class NetEnderHealthBarEffect {

    static {
        resetPlayers();
    }

    private static Map<NetPlayer, NetEnderHealthBarEffect> playerEnderBarMap;
    private FakeEntity enderDragon;

    private NetEnderHealthBarEffect(NetPlayer player) {
        this.enderDragon = new FakeEntity(
                player.getPlayer(),
                EntityType.ENDER_DRAGON,
                200,
                player.getPlayer().getLocation().clone().subtract(0, 100, 0),
                FakeEntity.EntityFlags.INVISIBLE
        );
    }

    private static void showDragonFor(NetPlayer player) {
        NetEnderHealthBarEffect enderBarFor = getEnderBarFor(player);
        enderBarFor.enderDragon.create();
    }

    public static void setTextFor(NetPlayer player, String string) {
        showDragonFor(player);
        NetEnderHealthBarEffect enderBarFor = getEnderBarFor(player);
        enderBarFor.enderDragon.setCustomName(string.substring(0, Math.min(string.length(), 63)));
    }

    private static NetEnderHealthBarEffect getEnderBarFor(NetPlayer player) {
        if (!NetEnderHealthBarEffect.playerEnderBarMap.containsKey(player)) {
            NetEnderHealthBarEffect enderBar = new NetEnderHealthBarEffect(player);
            NetEnderHealthBarEffect.playerEnderBarMap.put(player, enderBar);
            return enderBar;
        }
        return NetEnderHealthBarEffect.playerEnderBarMap.get(player);
    }

    private static boolean hasEnderBarFor(NetPlayer player) {
        return NetEnderHealthBarEffect.playerEnderBarMap.containsKey(player);
    }

    public static void setHealthPercent(NetPlayer player, Float health) {
        NetEnderHealthBarEffect enderBarFor = getEnderBarFor(player);
        Integer finHealth = Float.valueOf(health * 200).intValue();
        enderBarFor.enderDragon.setHealth(finHealth);
    }

    public static void resetPlayers() {
        playerEnderBarMap = null;
        playerEnderBarMap = new HashMap<>();
    }

    public static void refreshForPlayer(NetPlayer player) {
        NetEnderHealthBarEffect enderBarFor = getEnderBarFor(player);
        enderBarFor.enderDragon.create();
    }

    public static void remove(NetPlayer player) {
        NetEnderHealthBarEffect enderBarFor = getEnderBarFor(player);
        enderBarFor.enderDragon.destroy();
        playerEnderBarMap.remove(player);
    }

    public static class EnderBarListeners implements Listener {

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer(event.getPlayer());
            if (!hasEnderBarFor(netPlayer)) return;
            NetEnderHealthBarEffect enderBarFor = getEnderBarFor(netPlayer);
            Location enderLocation = enderBarFor.enderDragon.getLocation().clone();
            Location playerLocation = event.getPlayer().getLocation().clone();
            enderLocation.setY(0);
            playerLocation.setY(0);
            if (!enderLocation.getWorld().equals(playerLocation.getWorld()) || enderLocation.distance(playerLocation) >= 25d) {
                enderBarFor.enderDragon.setLocation(playerLocation.subtract(0, 100, 0));
            }
        }
//        @EventHandler
//        public void onPlayerRespawn(PlayerRespawnEvent event) {
//            final NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
//            if (!hasEnderBarFor(player)) return;
//            Bukkit.getScheduler().runTaskLater(NetWorkEngine.getInstance(), new Runnable() {
//                @Override
//                public void run() {
//                    NetEnderHealthBarEffect.refreshForPlayer(player);
//                }
//            }, 2L);
//        }
    }
}
