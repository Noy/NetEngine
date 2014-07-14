package com.noyhillel.survivalgames.player;

import com.noyhillel.networkengine.util.utils.RandomUtils;
import com.noyhillel.networkengine.util.player.NetPlayer;
import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.utils.MessageManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.util.List;

@EqualsAndHashCode(callSuper = true, of = {"username"})
@Data
public final class GPlayer extends GOfflinePlayer {

    private final String username;
    private Scoreboard playerScoreboard;
    private Objective currentScoreboardObjective;

    public GPlayer(String username, String uuid, List<String> usernames, Integer kills, Integer deaths, Integer wins, Integer plays, Integer mutation_credits, Integer points, String nick) {
        super(uuid, usernames, kills, deaths, wins, plays, mutation_credits, points, nick);
        this.username = username;
        this.playerScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
//        updateNick();
    }

    public void updateNick() {
        Player player = getPlayer();
        if (nick == null || nick.equalsIgnoreCase(player.getName())) {
            player.setPlayerListName(player.getName());
            if (!(DisguiseAPI.getDisguise(player) instanceof PlayerDisguise)) return;
            DisguiseAPI.undisguiseToAll(player);
            return;
        }
        PlayerDisguise nickDisguise = new PlayerDisguise(nick, true);
        DisguiseAPI.disguiseToAll(player, nickDisguise);
        player.setPlayerListName(nick);
    }

    public Player getPlayer() {
        return Bukkit.getPlayerExact(this.username);
    }

    public NetPlayer getPlayerFromNetPlayer() {
        return NetPlayer.getPlayerFromPlayer(getPlayer());
    }

    public void resetPlayer() {
        Player player = getPlayer();
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setVelocity(new Vector());
        player.setFallDistance(0);
        player.setNoDamageTicks(5);
        player.setFireTicks(0);
        removePotionEffects();
        player.setHealth(player.getMaxHealth());
        player.setSaturation(0);
        player.setFoodLevel(20);
        player.setTotalExperience(0);
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
    }

    public void heal() {
        Player player = getPlayer();
        player.setHealth(player.getMaxHealth());
        player.setFireTicks(0);
        player.setFoodLevel(20);
        removePotionEffects();
        sendMessage(MessageManager.getFormat("formats.heal-msg", true));
    }

    public void playSound(Sound sound) {
        playSound(sound, 0F);
    }

    public void playSound(Sound sound, Float pitch) {
        Player player = getPlayer();
        player.playSound(player.getLocation(), sound, 10, pitch);
    }

    public void teleport(Location location) {
        playSound(Sound.ENDERMAN_TELEPORT);
        getPlayer().teleport(location);
    }

    @SafeVarargs
    public final <T> void sendMessage(T... args) {
        for (T t : args) {
            getPlayer().sendMessage(t.toString());
        }
    }

    public void setScoreboardSide(String key, Integer value) {
        validateScoreboard();
        this.currentScoreboardObjective.getScore(Bukkit.getOfflinePlayer(key)).setScore(value);
    }

    public void unsetScoreboardSide(String key) {
        validateScoreboard();
        this.playerScoreboard.resetScores(Bukkit.getOfflinePlayer(key));
    }

    public Integer getScoreboardSide(String key) {
        validateScoreboard();
        return this.currentScoreboardObjective.getScore(Bukkit.getOfflinePlayer(key)).getScore();
    }

    public void resetScoreboardSide() {
        validateScoreboard();
        this.currentScoreboardObjective.unregister();
        this.currentScoreboardObjective = null;
    }

    public void addPotionEffect(PotionEffectType effectType, Integer level, Integer length, boolean ambient) {
        if (!getPlayer().
                addPotionEffect(
                        new PotionEffect(
                                effectType,
                                (length == Integer.MAX_VALUE ? length : length * 20),
                                Math.max(0, level - 1)), ambient)
                )
            throw new IllegalArgumentException("The potion effect data supplied is inaccurate");
    }

    public void addPotionEffect(PotionEffectType effectType, Integer level, Integer length) {
        addPotionEffect(effectType, level, length, true);
    }

    public void addPotionEffect(PotionEffectType effectType, Integer level) {
        addPotionEffect(effectType, level, Integer.MAX_VALUE);
    }

    public void addPotionEffect(PotionEffectType effectType) {
        addPotionEffect(effectType, 1);
    }

    public void removePotionEffects() {
        for (PotionEffect effect : getPlayer().getActivePotionEffects()) {
            getPlayer().removePotionEffect(effect.getType());
        }
    }

    public synchronized void setScoreboardTitle(String title) {
        if (this.currentScoreboardObjective == null) {
            this.currentScoreboardObjective = this.playerScoreboard.registerNewObjective(RandomUtils.getRandomString(5).substring(0, 5), "dummy");
            this.currentScoreboardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        this.currentScoreboardObjective.setDisplayName(title);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void validateScoreboard() {
        if (this.currentScoreboardObjective == null || this.playerScoreboard == null) {}// throw new IllegalStateException("You cannot set a scoreboard value when there is no objective!");
    }

    public void save() throws StorageError, PlayerNotFoundException {
        SurvivalGames.getInstance().getGPlayerManager().getStorage().savePlayer(this);
    }

    @Override
    public void setNick(String nick) {
        super.nick = nick;
        updateNick();
    }

    public String getDisplayableName() {
        return this.nick == null ? this.getUsername() : this.nick;
    }
}
