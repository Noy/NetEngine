package com.noyhillel.survivalgames.game.loots;

import com.noyhillel.survivalgames.arena.Point;
import com.noyhillel.survivalgames.game.GameException;
import com.noyhillel.survivalgames.game.impl.SGGame;
import org.bukkit.World;

import java.util.List;

public class SGTierUtil {
    public static void setupPoints(SGGame game, List<Point> points, String filename) throws GameException {
        Tier tier = new Tier(filename);
        tier.load();
        World loadedWorld = game.getArena().getLoadedWorld();
        for (Point point : points) {
            Loot l = new Loot(tier, point.toLocation(loadedWorld));
            l.fillChest();
        }
    }
}
