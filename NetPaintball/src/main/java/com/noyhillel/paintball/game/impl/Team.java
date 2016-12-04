package com.noyhillel.paintball.game.impl;

import org.bukkit.ChatColor;

public enum Team {
    RED("Red Team", ChatColor.RED),
    BLUE("Blue Team", ChatColor.AQUA);

    private final String name;
    private final ChatColor color;

    Team(String name, ChatColor color) {
        this.name = name;
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.color + this.name + ChatColor.RESET;
    }
}
