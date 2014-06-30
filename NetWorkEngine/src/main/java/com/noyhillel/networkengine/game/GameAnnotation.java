package com.noyhillel.networkengine.game;

import org.bukkit.ChatColor;

/**
 * Created by Noy on 28/05/2014.
 */
public @interface GameAnnotation {

    public String longName();

    public String shortName();

    public String version();

    public String author() default "NoyHillel1";

    public String description();

    public PvPMode pvpMode() default PvPMode.FreeForAll;

    public ChatColor mainColor();

    public ChatColor secondaryColor();

    public String key();

    public int minPlayers();

    public int maxPlayers();

    public PlayerCountMode playerCountMode() default PlayerCountMode.Any;

    public static enum PlayerCountMode {
        Odd,
        Even,
        Any
    }

    public static enum PvPMode {
        FreeForAll,
        NoPvP
    }

}
