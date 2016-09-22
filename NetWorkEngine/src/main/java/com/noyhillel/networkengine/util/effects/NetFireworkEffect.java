package com.noyhillel.networkengine.util.effects;

import com.noyhillel.networkengine.util.NetPlugin;
import com.noyhillel.networkengine.util.player.NetPlayer;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

/**
 * Created by Noy on 28/05/2014.
 */
public final class NetFireworkEffect {

    public static void shootFireWorks(NetPlayer player, Location location) {
        if (player == null) return;
        Firework fireworks = player.getPlayer().getWorld().spawn(location, Firework.class);
        FireworkMeta fm = fireworks.getFireworkMeta();
        Random random = new Random();
        FireworkEffect effect = FireworkEffect.builder().flicker(random.nextBoolean())
                .withColor(getRandomColors()).withFade(getRandomColors())
                .with(getFireWorkType())
                .trail(random.nextBoolean()).build();
        fm.addEffect(effect);
        Integer power = random.nextInt(1)+1;
        fm.setPower(power);
        fireworks.setFireworkMeta(fm);
    }

    private static Color getRandomColors() {
        DyeColor[] dyeColors = DyeColor.values();
        return dyeColors[NetPlugin.getRandom().nextInt(dyeColors.length)].getColor();
    }

    private static FireworkEffect.Type getFireWorkType() {
        FireworkEffect.Type[] fireworkType = FireworkEffect.Type.values();
        return fireworkType[NetPlugin.getRandom().nextInt(fireworkType.length)];
    }
}
