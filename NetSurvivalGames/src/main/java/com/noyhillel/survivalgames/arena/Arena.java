package com.noyhillel.survivalgames.arena;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bukkit.World;

import java.io.File;
import java.util.List;

@EqualsAndHashCode(callSuper = false, of = {"meta"})
@Data
public final class Arena extends WorldStrapped {
    @NonNull private final PointIterator cornicopiaSpawns;
    @NonNull private final PointIterator tier1;
    @NonNull private final PointIterator tier2;
    @NonNull private final ArenaMeta meta;

    public Arena(List<Point> cornicopiaSpawn, List<Point> tier1, List<Point> tier2, ArenaMeta arenaMeta, File file) {
        super(file);
        this.cornicopiaSpawns = new PointIterator(cornicopiaSpawn);
        this.tier1 = new PointIterator(tier1);
        this.tier2 = new PointIterator(tier2);
        this.meta = arenaMeta;
    }

    public Arena(List<Point> cornicopiaSpawn, List<Point> tier1, List<Point> tier2, ArenaMeta arenaMeta, World world) {
        super(world);
        this.cornicopiaSpawns = new PointIterator(cornicopiaSpawn);
        this.tier1 = new PointIterator(tier1);
        this.tier2 = new PointIterator(tier2);
        this.meta = arenaMeta;
    }
}
