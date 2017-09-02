package org.inscriptio.uhc.game.loots;

import com.noyhillel.networkengine.game.arena.Point;
import org.bukkit.World;
import org.inscriptio.uhc.game.GameException;
import org.inscriptio.uhc.game.impl.UHCGame;

import java.util.List;

public class SGTierUtil {

    public static void setupPoints(UHCGame game, List<Point> points, String filename) throws GameException {
        Tier tier = new Tier(filename);
        tier.load();
        World loadedWorld = game.getArena().getLoadedWorld();
        for (Point point : points) {
            Loot l = new Loot(tier, point.toLocation(loadedWorld));
            l.fillChest();
        }
    }
}