package com.noyhillel.survivalgames.game.lobby;

import com.noyhillel.survivalgames.arena.Point;
import com.noyhillel.survivalgames.arena.PointIterator;
import com.noyhillel.survivalgames.arena.WorldStrapped;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.World;

import java.io.File;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
public final class GameLobby extends WorldStrapped {
    private final PointIterator spawnPoints;

    public GameLobby(List<Point> points, File zipFile) {
        super(zipFile);
        this.spawnPoints = new PointIterator(points);
    }

    public GameLobby(List<Point> lobbySpawns, World world) {
        super(world);
        this.spawnPoints = new PointIterator(lobbySpawns);
    }
}
