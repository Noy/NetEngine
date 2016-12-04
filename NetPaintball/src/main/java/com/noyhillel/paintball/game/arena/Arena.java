package com.noyhillel.paintball.game.arena;

import com.noyhillel.networkengine.game.WorldStrapped;
import com.noyhillel.networkengine.game.arena.ArenaMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;

@EqualsAndHashCode(callSuper = false, of = {"meta"})
@Data
public final class Arena extends WorldStrapped {
    @NonNull private final Location redTeamSpawn;
    @NonNull private final Location blueTeamSpawn;
    @NonNull private final ArenaMeta meta;

    public Arena(Location redTeamSpawn, Location blueTeamSpawn, ArenaMeta arenaMeta, File file) {
        super(file);
        this.redTeamSpawn = new Location(this.getLoadedWorld(), redTeamSpawn.getX(), redTeamSpawn.getY(), redTeamSpawn.getZ());
        this.blueTeamSpawn = new Location(this.getLoadedWorld(), blueTeamSpawn.getX(), blueTeamSpawn.getY(), blueTeamSpawn.getZ());
        this.meta = arenaMeta;
    }

    public Arena(Location redTeamSpawn, Location blueTeamSpawn, ArenaMeta arenaMeta, World world) {
        super(world);
        this.redTeamSpawn = new Location(this.getLoadedWorld(), redTeamSpawn.getX(), redTeamSpawn.getY(), redTeamSpawn.getZ());
        this.blueTeamSpawn = new Location(this.getLoadedWorld(), blueTeamSpawn.getX(), blueTeamSpawn.getY(), blueTeamSpawn.getZ());
        this.meta = arenaMeta;
    }
}

