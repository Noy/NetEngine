package com.noyhillel.battledome.game;


import com.noyhillel.battledome.exceptions.BattledomeException;
import com.noyhillel.battledome.arena.Point;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Arrays;

/**
 * Created by Noy on 6/23/2014.
 */
public final class BGameManager {

    @Getter private static BGameManager bGameManager = null;
    private final World world;
    @Getter private BGame game = null;

    public BGameManager(World world) {
        this.world = world;
        BGameManager.bGameManager = this;
    }

    public void start(Location location) throws BattledomeException {
        if (game == null) throw new  BattledomeException("Cannot start game!");
        game = new BGame(Arrays.asList(Bukkit.getOnlinePlayers()), pointFromLocation(location), world);
        game.startGame();
    }

    public void startGame(Location startLocation) throws BattledomeException {
        if (game != null) throw new BattledomeException("Could not start a game, a game is already running!");
        game = new BGame(Arrays.asList(Bukkit.getOnlinePlayers()), pointFromLocation(startLocation), world);
        game.startGame();
    }

    private static Point pointFromLocation(Location l) {
        return new Point(l.getX(), l.getY(), l.getZ(), l.getPitch(), l.getYaw());
    }
}
