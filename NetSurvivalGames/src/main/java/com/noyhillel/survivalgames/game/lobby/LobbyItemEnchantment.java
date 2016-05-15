package com.noyhillel.survivalgames.game.lobby;

import lombok.Data;
import org.bukkit.enchantments.Enchantment;

@Data(staticConstructor = "of")
final class LobbyItemEnchantment {
    private final Integer level;
    private final Enchantment enchantmentType;
}
