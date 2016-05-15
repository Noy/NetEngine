package com.noyhillel.battledome.game;

import org.bukkit.ChatColor;

/**
 * Created by Noy on 6/23/2014.
 */
public enum Team {
    AQUA(ChatColor.AQUA, "Aqua"),
    GREEN(ChatColor.GREEN, "Green");

    private final String name;
    private final ChatColor color;

    Team(ChatColor color, String name) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return this.color + this.name + ChatColor.RESET;
    }
}
