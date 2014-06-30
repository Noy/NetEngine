package com.noyhillel.survivalgames.game.impl;

import com.noyhillel.survivalgames.SurvivalGames;
import com.noyhillel.survivalgames.player.GPlayer;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

@Data
public final class MutatedPlayer {
    private final GPlayer player;
    private final GPlayer target;

    @Setter(AccessLevel.PRIVATE) private boolean mutated = false;

    void mutate() {
        if (mutated) throw new IllegalStateException("You cannot re-mutate!");
        DisguiseType disguiseType = DisguiseType.valueOf(SurvivalGames.getInstance().getConfig().getString("mutation.disguise"));
        DisguiseAPI.disguiseToAll(player.getPlayer(), new MobDisguise(disguiseType));
        player.resetPlayer();
        player.addPotionEffect(PotionEffectType.SPEED, SurvivalGames.getInstance().getConfig().getInt("mutation.potion-effect-level"));
        ItemStack sword = new ItemStack(Material.WOOD_SWORD);
        sword.addEnchantment(Enchantment.DURABILITY, 1);
        player.getPlayer().getInventory().addItem(sword);
        this.mutated = true;
    }

    void gameEnded() { unMutate(); }

    void unMutate() {
        if (!mutated) throw new IllegalStateException("You cannot re-un-mutate!");
        DisguiseAPI.undisguiseToAll(player.getPlayer());
        player.updateNick();
        this.mutated = false;
    }
}
