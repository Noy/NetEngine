package org.inscriptio.uhc.arena;

import com.noyhillel.networkengine.game.WorldStrapped;
import com.noyhillel.networkengine.game.arena.ArenaMeta;
import com.noyhillel.networkengine.game.arena.Point;
import com.noyhillel.networkengine.game.arena.PointIterator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bukkit.World;

import java.io.File;
import java.util.List;

@EqualsAndHashCode(callSuper = false, of = {"meta"})
@Data
public final class Arena extends WorldStrapped {
    @NonNull private final PointIterator randomPointSpawns;
    @NonNull private Point centerSpawn;
    @NonNull private final PointIterator tier1;
    @NonNull private final PointIterator tier2;
    @NonNull private final ArenaMeta meta;

    public Arena(List<Point> randomSpotSpawn, List<Point> tier1, List<Point> tier2, ArenaMeta arenaMeta, File file) {
        super(file);
        this.randomPointSpawns = new PointIterator(randomSpotSpawn);
        for (Point point : randomPointSpawns.getPoints()) {
            point = randomPointSpawns.getPoints().get((int) (Math.random() * randomSpotSpawn.size()));
            centerSpawn = point;
        }
        this.tier1 = new PointIterator(tier1);
        this.tier2 = new PointIterator(tier2);
        this.meta = arenaMeta;
    }

    public Arena(List<Point> cornicopiaSpawn, List<Point> tier1, List<Point> tier2, ArenaMeta arenaMeta, World world) {
        super(world);
        this.randomPointSpawns = new PointIterator(cornicopiaSpawn);
        this.tier1 = new PointIterator(tier1);
        this.tier2 = new PointIterator(tier2);
        this.meta = arenaMeta;
    }
}
