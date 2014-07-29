package com.noyhillel.netkitpvp.game;

import com.noyhillel.netkitpvp.NetKitPVP;
import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;

/**
 * <p/>
 * Latest Change: 14/07/2014.
 * <p/>
 *
 * @author Noy
 * @since 14/07/2014.
 */
public final class KitPVPGame implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().clear();
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            NetPlayer netKiller = NetPlayer.getPlayerFromPlayer(killer);
            if (netKiller != null) {
                netKiller.addPotionEffect(PotionEffectType.getByName(NetKitPVP.getInstance().getConfig().getString("potion-effecs.killer")));
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(event.getPlayer().getWorld().getSpawnLocation());
    }
}
