package com.noyhillel.networkengine.util.effects;

import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

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

    private final BossBar bossBar;

    private NetEnderHealthBarEffect() {
        this.bossBar = Bukkit.createBossBar("", BarColor.PURPLE, BarStyle.SOLID);
    }

    private static void showDragonFor(NetPlayer player) {
        NetEnderHealthBarEffect enderBarFor = getEnderBarFor(player);
        enderBarFor.bossBar.addPlayer(player.getPlayer());
    }

    public static void setTextFor(NetPlayer player, String string) {
        showDragonFor(player);
        NetEnderHealthBarEffect enderBarFor = getEnderBarFor(player);
        enderBarFor.bossBar.setTitle(string.substring(0, Math.min(string.length(), 63)));
    }

    @Deprecated
    public static void setHealthStatus(NetPlayer player, Float health) {
        NetEnderHealthBarEffect enderBarFor = getEnderBarFor(player);
        Integer finHealth = Double.valueOf(health * 200).intValue();
        enderBarFor.bossBar.setProgress(finHealth);
    }

    public static void setHealthPercent(NetPlayer player, Double health) {
        NetEnderHealthBarEffect enderBarFor = getEnderBarFor(player);
        enderBarFor.bossBar.setProgress(health);
    }

    private static NetEnderHealthBarEffect getEnderBarFor(NetPlayer player) {
        if (!NetEnderHealthBarEffect.playerEnderBarMap.containsKey(player)) {
            NetEnderHealthBarEffect enderBar = new NetEnderHealthBarEffect();
            NetEnderHealthBarEffect.playerEnderBarMap.put(player, enderBar);
            return enderBar;
        }
        return NetEnderHealthBarEffect.playerEnderBarMap.get(player);
    }

    private static boolean hasEnderBarFor(NetPlayer player) {
        return NetEnderHealthBarEffect.playerEnderBarMap.containsKey(player);
    }

    private static void resetPlayers() {
        playerEnderBarMap = null;
        playerEnderBarMap = new HashMap<>();
    }

    public static void refreshForPlayer(NetPlayer player) {
        NetEnderHealthBarEffect enderBarFor = getEnderBarFor(player);
        enderBarFor.bossBar.removePlayer(player.getPlayer());
    }

    public static void remove(NetPlayer player) {
        NetEnderHealthBarEffect enderBarFor = getEnderBarFor(player);
        enderBarFor.bossBar.removeAll();
        playerEnderBarMap.remove(player);
    }

//    public static class EnderBarListeners implements Listener {

//        @EventHandler
//        public void onPlayerMove(PlayerMoveEvent event) {
//            NetPlayer netPlayer = NetPlayer.getPlayerFromPlayer(event.getPlayer());
//            if (!hasEnderBarFor(netPlayer)) return;
//            NetEnderHealthBarEffect enderBarFor = getEnderBarFor(netPlayer);
//            Location enderLocation = enderBarFor.enderDragon.getLocation().clone();
//            Location playerLocation = event.getPlayer().getLocation().clone();
//            enderLocation.setY(0);
//            playerLocation.setY(0);
//            if (!enderLocation.getWorld().equals(playerLocation.getWorld()) || enderLocation.distance(playerLocation) >= 25d) {
//                enderBarFor.bossBar.setLocation(playerLocation.subtract(0, 100, 0));
//            }
//        }
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
//    }
}
