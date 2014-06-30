package com.noyhillel.networkhub.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;


/**
 * Created by Noy on 26/05/2014.
 */
public final class UtilListeners extends ModuleListener {

    public UtilListeners() {
        super("util-listeners");
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHungerLoss(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        event.setCancelled(true);
    }

//    @EventHandler
//    public void onInventoryMove(PlayerItemHeldEvent event) {
//        if (event.getPlayer().isOp()) return;
//        event.setCancelled(true);
//    }
}