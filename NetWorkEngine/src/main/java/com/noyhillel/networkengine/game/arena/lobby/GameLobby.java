package com.noyhillel.networkengine.game.arena.lobby;

import com.noyhillel.networkengine.game.WorldStrapped;
import com.noyhillel.networkengine.game.arena.Point;
import com.noyhillel.networkengine.game.arena.PointIterator;
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
