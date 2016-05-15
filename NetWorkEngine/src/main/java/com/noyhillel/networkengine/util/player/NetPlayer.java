package com.noyhillel.networkengine.util.player;

import com.noyhillel.networkengine.util.NetPlugin;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Noy on 25/05/2014.
 */
@Data
public final class NetPlayer {

    private final String playerName;
    private final UUID uuid;
    @Setter(AccessLevel.PRIVATE) private Scoreboard scoreboard;
    private Objective sidebar = null;
    private final NetPlayerManager playerManager = null;

    // Getting specific players ftw
    public NetPlayer(String name, UUID uuid) {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.playerName = name;
        this.uuid = uuid;
    }

    public static NetPlayer getPlayerFromPlayer(Player player) {
        return NetPlugin.getNetPlayerManager().getOnlinePlayer(player);
    }

    public Player getPlayer() {
        return Bukkit.getPlayerExact(playerName);
    }

    public void resetPlayer() {
        Player player = getPlayer();
        player.setFallDistance(0);
        player.setNoDamageTicks(5);
        player.setFireTicks(0);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setHealth(player.getMaxHealth());
        player.setSaturation(0);
        player.setFoodLevel(20);
        clearInventory();
    }

    public void restorePlayer() {
        resetPlayer();
        Player player = getPlayer();
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setExp(0F);
        player.setGameMode(GameMode.ADVENTURE);
        player.setLevel(0);
    }

    public void playSound(Sound sound, Float pitch) {
        this.playSound(sound, 10F, pitch);
    }

    public void playSound(Sound sound) {
        this.playSound(sound, 1F);
    }

    public void playSound(Sound sound, Float volume, Float pitch) {
        this.getPlayer().playSound(getPlayer().getLocation(), sound, volume, pitch);
    }

    public String getIP() {
        return getPlayer().getAddress().getHostString();
    }

    public void clearChat() {
        for (int x = 0; x <= 200; x++) {
            sendMessage(" ");
        }
    }

    public void turnOnFly() {
        getPlayer().setAllowFlight(true);
    }

    public void turnOffFly() {
        getPlayer().setAllowFlight(false);
    }

    public void addPotionEffect(PotionEffectType type, Integer length, Integer intensity, Boolean ambient) {
        PotionEffect toAdd = new PotionEffect(type, (length == Integer.MAX_VALUE ? Integer.MAX_VALUE : length*20), intensity, ambient);
        this.getPlayer().addPotionEffect(toAdd);
    }

    public void addPotionEffect(PotionEffectType type, Integer length, Integer intensity) {
        this.addPotionEffect(type, length, intensity, true);
    }

    public void addPotionEffect(PotionEffectType type, Integer length) {
        this.addPotionEffect(type, length, 0);
    }

    public void addPotionEffect(PotionEffectType type) {
        this.addPotionEffect(type, Integer.MAX_VALUE);
    }

    public void addInfinitePotionEffect(PotionEffectType type, Integer intensity) {
        this.addPotionEffect(type, Integer.MAX_VALUE, intensity);
    }

    public void removeAllPotionEffects(PotionEffectType... exclusions) {
        List<PotionEffectType> doNotRemove = Arrays.asList(exclusions);
        for (PotionEffect effect : this.getPlayer().getActivePotionEffects()) {
            if (doNotRemove.contains(effect.getType())) continue;
            this.getPlayer().removePotionEffect(effect.getType());
        }
    }

    public void removePotionEffects(PotionEffectType... potionEffects) {
        if (potionEffects.length < 1) return;
        List<PotionEffectType> potionEffectTypes = Arrays.asList(potionEffects);
        for (PotionEffect effect : this.getPlayer().getActivePotionEffects()) {
            if (potionEffectTypes.contains(effect.getType())) this.getPlayer().removePotionEffect(effect.getType());
        }
    }

    public Integer getCurrentPotionLevel(PotionEffectType effectType) {
        Integer level = -1;
        for (PotionEffect effect : this.getPlayer().getActivePotionEffects()) {
            if (!effect.getType().equals(effectType)) continue;
            level = effect.getAmplifier();
            break;
        }
        return level;
    }

    public boolean hasPotionEffect(PotionEffectType e) {
        return e != null && getPlayer().hasPotionEffect(e);
    }

    public ItemStack giveItem(Material type, Integer amount, Short value, String title, String[] lore, Integer slot) {
        if (type == null) return null;
        if (amount < 1) return null;
        ItemStack itemStack = new ItemStack(type, amount);
        if (value > 1) itemStack.setDurability(value);
        ItemMeta meta = itemStack.getItemMeta();
        if (title != null) meta.setDisplayName(title);
        if (lore != null) meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        if (slot < 1 || slot > 9) {
            Integer toGive = amount;
            while (toGive > 0) {
                itemStack.setAmount(Math.min(itemStack.getMaxStackSize(), toGive));
                this.getPlayer().getInventory().addItem(itemStack);
                toGive = toGive-itemStack.getAmount();
            }
        }
        else this.getPlayer().getInventory().setItem(slot-1, itemStack);
        return itemStack;
    }

    public ItemStack giveItem(Material type, Integer amount, Short value, String title, String[] lore) {
        return giveItem(type, amount, value, title, lore, -1);
    }

    public ItemStack giveItem(Material type, Integer amount, Short value, String title) {
        return giveItem(type, amount, value, title, null);
    }

    public ItemStack giveItem(Material type, Integer amount, Short value) {
        return giveItem(type, amount, value, null);
    }

    public ItemStack giveItem(Material type, Integer amount, Integer slot) {
        return giveItem(type, amount, null, null, null, slot);
    }

    public ItemStack giveItem(Material type, Integer amount) {
        return giveItem(type, amount, (short)0);
    }

    public ItemStack giveItem(Material type) {
        return giveItem(type, 1);
    }

    public ItemStack giveItemAStackOf(Material type) {
        return giveItem(type, 64);
    }

    public boolean removeItem(Material material, Integer quantity) {
        if (!getPlayer().getInventory().contains(material, quantity)) return false;
        getPlayer().getInventory().removeItem(new ItemStack(material, quantity));
        return true;
    }

    public boolean removeItem(Material material) {
        return removeItem(material, 1);
    }

    @SafeVarargs
    public final <T> void sendMessage(T... message) {
        if (!this.isOnline()) return;
        for (T m : message) {
            this.getPlayer().sendMessage(m.toString());
        }
    }

    public void teleport(Location location) {
        this.playSound(Sound.ENTITY_ENDERMEN_TELEPORT);
        this.getPlayer().teleport(location);
    }

    public void clearInventory() {
        this.getPlayer().getInventory().clear();
        this.getPlayer().getInventory().setArmorContents(new ItemStack[4]);
    }

    public void resetScoreboard() {
        if (!this.isOnline()) return;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.sidebar = null;
        this.getPlayer().setScoreboard(this.scoreboard);
    }

    public void setScoreboardSideTitle(String title) {
        if (!isOnline()) return;
        if (this.sidebar == null) {
            String s = new BigInteger(13, NetPlugin.getRandom()).toString(5);
            this.sidebar = this.scoreboard.registerNewObjective(s.substring(0, Math.min(s.length(), 15)), "dummy");
            this.sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        this.sidebar.setDisplayName(title);
    }

    public void setScoreBoardSide(String key, Integer value) {
        if (!this.isOnline()) return;
        Score score = this.sidebar.getScore(Bukkit.getOfflinePlayer(key.substring(0, Math.min(key.length(), 15))));
        score.setScore(value);
        if (getPlayer() == null) return;
        if (!getPlayer().isOnline()) return;
        getPlayer().setScoreboard(this.scoreboard);
    }

    public void sendException(Throwable t) {
        getPlayer().sendMessage(ChatColor.RED + "Error: " + ChatColor.WHITE + t.getMessage());
    }

    public void kick(String s) {
        this.getPlayer().kickPlayer(s);
    }

    public void kill() {
        this.getPlayer().damage(this.getPlayer().getHealth());
    }

    public void setExperience(Float f) {
        this.getPlayer().setExp(f);
    }

    public boolean hasPermission(String s) {
        return this.getPlayer().hasPermission(s);
    }

    public boolean isOnline() {
        return Bukkit.getOfflinePlayer(this.playerName).isOnline() && getPlayer() != null;
    }

    public boolean isFirstTimeOnline() {
        return this.getPlayer().hasPlayedBefore();
    }

    public void setFlying(boolean allowFlight) {
        this.getPlayer().setAllowFlight(allowFlight);
    }

    public Double getHealth() {
        return this.getPlayer().getHealth();
    }

    public Integer getFoodLevel() {
        return this.getPlayer().getFoodLevel();
    }

    public String getName() {
        return this.getPlayer().getName();
    }

    public String getDisplayName() {
        return this.getPlayer().getDisplayName();
    }

    public boolean isOp() {
        return this.getPlayer().isOp();
    }

    @Deprecated
    public ItemStack getItemInHand() {
        return this.getPlayer().getItemInHand();
    }

    public void openInventory(Inventory inventory) {
        this.getPlayer().openInventory(inventory);
    }

    public void damage(double amount) {
        getPlayer().damage(amount);
    }

    public void strikeLightning(Location loc) {
        getPlayer().getWorld().strikeLightningEffect(loc);
    }

    public void closeInventory() {
        this.getPlayer().closeInventory();
    }

    public Location getLocation() {
        return this.getPlayer().getLocation();
    }
}