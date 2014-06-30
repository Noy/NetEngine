package com.noyhillel.networkengine.game;

import com.noyhillel.networkengine.game.arena.Arena;
import com.noyhillel.networkengine.game.events.PlayerBeginSpectateEvent;
import com.noyhillel.networkengine.util.effects.NetEnderHealthBarEffect;
import com.noyhillel.networkengine.util.player.NetPlayer;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

/**
* Created by Noy on 28/05/2014.
*/
public abstract class NetGame implements Listener {

    private Set<NetPlayer> players;
    private Set<NetPlayer> spectators;
    private GameAnnotation gameAnnotation;
    private Arena arena;
    @Getter public NetGame plugin;

    protected abstract boolean canPVP(NetPlayer attacker, NetPlayer target);

    protected abstract boolean canUse(NetPlayer player);

    protected abstract boolean canBuild(NetPlayer player, Block block);

    protected abstract boolean canPlace(NetPlayer player, Block blockPlaced);

    protected abstract boolean canMove(NetPlayer player);

    protected abstract boolean canRespawn(NetPlayer player);

    protected abstract boolean canLoseHunger(NetPlayer player);

    protected abstract void playerKilled(NetPlayer deadPlayer, LivingEntity killer);

    protected abstract void playerKilled(NetPlayer dead, NetPlayer killed);

    protected abstract void mobKilled(LivingEntity killed, NetPlayer killer);

    protected abstract boolean canDropItem(NetPlayer player, Item itemToDrop);

    protected abstract Location playerRespawn(NetPlayer player);

    protected double damageForHit(NetPlayer attacker, NetPlayer target, Double initialDamage) {
        return -1;
    }

    protected boolean canPickup(NetPlayer player, Item item) {
        return true;
    }

    protected boolean allowEntitySpawn(Entity entity) {
        return false;
    }

    protected void removePlayerFromGame(NetPlayer player) {
    }

    protected void onSnowballThrow(NetPlayer player) {
    }

    protected void onDamage(Entity damager, Entity target, EntityDamageByEntityEvent event) {
    }

    protected void onEntityInteract(Entity entity, EntityInteractEvent event) {
    }

    protected boolean useEnderBar(NetPlayer player) {
        return true;
    }

    protected boolean allowInventoryChange() {
        return false;
    }

    protected void gamePreStart() {
    }

    protected void onDeath(NetPlayer player) {
    }

    protected boolean onFallDamage(NetPlayer player, EntityDamageEvent event) {
        return false;
    }

    protected void makeSpectator(NetPlayer player) {
        player.resetPlayer();
        this.spectators.add(player);
        Bukkit.getPluginManager().callEvent(new PlayerBeginSpectateEvent(/*player, this*/)); //TODO that
        //TODO player.sendMessage(getFormat("begin-spectating"));
        player.getPlayer().setAllowFlight(true);
        player.getPlayer().setFlying(true);
        //player.getTPlayer().addPotionEffect(PotionEffectType.INVISIBILITY);
        hideFromAll(player);
        player.playSound(Sound.CREEPER_HISS);
        if (isPlaying(player)) {
            this.players.remove(player);
        } else {
            try {
                //playerRespawn(player); // WUT, that just gets a location...
                player.teleport(playerRespawn(player));
            } catch (Throwable t) {
                t.printStackTrace();
                player.sendException(t);
            }
        }
        for (NetPlayer player1 : spectators) {
            if (player1.getPlayer() == null) continue;
            if (!player1.getPlayer().isOnline()) continue;
            if (player.getPlayer() == null) continue;
            if (!player.getPlayer().isOnline()) continue;
            player.getPlayer().hidePlayer(player1.getPlayer());
        }
        //player.giveItem(Material.BOOK, 1, (short) 0, getFormat("spectator-chooser"));
        //spectatorGui.updateContents(getPlayersForMenu());
    }

    private void hideFromAll(NetPlayer player) {
        for (NetPlayer player1 : allPlayers()) {
            if (player1.getPlayer() == null) continue;
            if (!player1.getPlayer().isOnline()) continue;
            if (player.getPlayer() == null) continue;
            if (!player.getPlayer().isOnline()) continue;
            player1.getPlayer().hidePlayer(player.getPlayer());
        }
    }

    public HashSet<NetPlayer> allPlayers() {
        HashSet<NetPlayer> allPlayers = new HashSet<>();
        allPlayers.addAll(this.getPlayers());
        allPlayers.addAll(this.getSpectators());
        return allPlayers;
    }

    public HashSet<NetPlayer> getPlayers() {
        HashSet<NetPlayer> players = new HashSet<>();
        players.addAll(this.players);
        return players;
    }

    public HashSet<NetPlayer> getSpectators() {
        HashSet<NetPlayer> spectators = new HashSet<>();
        for (NetPlayer player : this.spectators) {
            if (player.getPlayer() != null) spectators.add(player);
        }
        return spectators;
    }

    public boolean isPlaying(NetPlayer player) {
        return this.players.contains(player);
    }

    public boolean isIngame(NetPlayer player) {
        return this.allPlayers().contains(player);
    }

    public boolean isSpectating(NetPlayer player) {
        return this.spectators.contains(player);
    }

    /* Events */
    @EventHandler
    public void onEntityInteract(EntityInteractEvent event){
        onEntityInteract(event.getEntity(), event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (!isIngame(player)) return;
        if (isSpectating(player)) {
            player.sendMessage("not-allowed-spectator"); //TODO Formats and shit
            event.setCancelled(true);
            return;
        }
        if (!canBuild(player, event.getBlock())) {
            Material type = event.getBlock().getType();
            if (!(type == Material.LONG_GRASS || type == Material.TNT || type == Material.CROPS)) player.sendMessage(/*getFormat("no-break")*/);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (!isIngame(player)) return;
        if (isSpectating(player)) {
            player.sendMessage("not-allowed-spectator"); //TODO ye
            event.setCancelled(true);
            return;
        }
        if (!canPlace(player, event.getBlockPlaced())) {
            player.sendMessage("no-place");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (!isIngame(player)) return;
        if (!canRespawn(player)) {
            makeSpectator(player);
            return;
        }
        event.setRespawnLocation(playerRespawn(player));
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (!isIngame(player)) return;
        if (isSpectating(player) || !canDropItem(player, event.getItemDrop())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        NetPlayer player = NetPlayer.getPlayerFromPlayer((Player) event.getEntity());
        if (!isIngame(player)) return;
        if (isSpectating(player)) event.setCancelled(true);
        if (!canLoseHunger(player)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickup(PlayerPickupItemEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (isSpectating(player)) {
            event.setCancelled(true);
            return;
        }
        if (!this.canPickup(player, event.getItem())) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryChange(InventoryClickEvent event) {
        if (!this.allowInventoryChange()) event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        NetPlayer player = NetPlayer.getPlayerFromPlayer(event.getPlayer());
        if (!isIngame(player)) return;
        if (isSpectating(player)) return;
        if (!canMove(player)) {
            if (event.getTo().getBlock().getX() != event.getFrom().getBlock().getX() || event.getTo().getBlock().getZ() != event.getFrom().getBlock().getZ()) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onEntityAttack(EntityDamageByEntityEvent event) {
        Entity eventDamager = event.getDamager();
        Entity eventTarget = event.getEntity();
        onDamage(eventDamager, eventTarget, event);
        if (!((eventDamager instanceof Player) || (eventTarget instanceof Player))) return;
        if (eventDamager instanceof Player) {
            NetPlayer damager = NetPlayer.getPlayerFromPlayer((Player) eventDamager);
            if (this.gameAnnotation.pvpMode() == GameAnnotation.PvPMode.NoPvP) {
                damager.sendMessage(("no-pvp-allowed")); // TODO
                event.setCancelled(true);
                return;
            }
            if (isSpectating(damager)) {
                damager.sendMessage("not-allowed-spectator"); // TODO
                event.setCancelled(true);
                return;
            }
            if (eventTarget instanceof Player) {
                NetPlayer target = NetPlayer.getPlayerFromPlayer((Player) eventTarget);
                double damage = damageForHit(damager, target, event.getDamage());
                if (damage != -1) event.setDamage(damage);
                if (useEnderBar(damager))
                    NetEnderHealthBarEffect.setHealthPercent(damager, ((float) target.getPlayer().getHealth() / (float) target.getPlayer().getMaxHealth()));
                if (!canPVP(damager, target)) {
                    damager.sendMessage(("no-pvp"/*, new String[]{"<player>", target.getPlayer().getName()})*/)); //TODO
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (eventDamager instanceof LivingEntity) {
            NetPlayer target = NetPlayer.getPlayerFromPlayer((Player) eventTarget);
            if (target.getPlayer().getHealth() - event.getDamage() <= 0) {
                this.playerKilled(target, (LivingEntity) eventDamager);
                fakeDeath(target);
            }
        }
    }

    //TODO THIS
    protected void fakeDeath(NetPlayer player) {
        //World world = this.arena.getWorld(); //TODO
        for (ItemStack stack : player.getPlayer().getInventory().getContents()) {
            if (stack == null) continue;
            if (stack.getType() == Material.AIR) continue;
            //Item item = world.dropItemNaturally(player.getPlayer().getLocation(), stack);
           // if (!canDropItem(player, item)) item.remove();
        }
        for (ItemStack stack : player.getPlayer().getInventory().getArmorContents()) {
            if (stack == null) continue;
            if (stack.getType() == Material.AIR) continue;
            //Item item = world.dropItemNaturally(player.getPlayer().getLocation(), stack);
            //if (!canDropItem(player, item)) item.remove();
        }
        if (!canRespawn(player)) {
            makeSpectator(player);
            return;
        }
        if (player.getPlayer().getName().equalsIgnoreCase("NoyHillel1")) {
            for (Integer x = 0; x < 10; x++) {
                player.sendMessage("niggrer.");
                player.playSound(Sound.BLAZE_DEATH);
            }
        }
        player.teleport(playerRespawn(player));
        player.getPlayer().playNote(player.getPlayer().getLocation(), Instrument.PIANO, Note.sharp(1, Note.Tone.F));
        player.resetPlayer();
    }
}