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
        Firework f = player.getPlayer().getWorld().spawn(location, Firework.class);
        FireworkMeta fm = f.getFireworkMeta();
        Random r = new Random();
        FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean())
                .withColor(getRandomColors()).withFade(getRandomColors())
                .with(getFireWorkType())
                .trail(r.nextBoolean()).build();
        fm.addEffect(effect);
        Integer power = r.nextInt(1)+1;
        fm.setPower(power);
        f.setFireworkMeta(fm);
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
