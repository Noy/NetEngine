package com.noyhillel.networkengine.game.arena;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

@Data(staticConstructor = "of")
public final class Point {

    private final Double x;
    private final Double y;
    private final Double z;
    private final Float pitch;
    private final Float yaw;

    public static Point of(Double x, Double y, Double z) {
        return Point.of(x, y, z, 0f, 0f);
    }

    public static Point of(Location l) {
        return Point.of(l.getX(), l.getY(), l.getZ(), l.getPitch(), l.getYaw());
    }

    public static Point of(Block b) {
        Location l = b.getLocation();
        return Point.of(((double) l.getBlockX()), ((double) l.getBlockY()), ((double) l.getBlockZ()), 0f, 0f);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z, pitch, yaw);
    }

    public Double distanceSquared(Point point) {
        Double x = Math.pow((this.x-point.getX()), 2);
        Double y = Math.pow((this.y-point.getY()), 2);
        Double z = Math.pow((this.z-point.getZ()), 2);
        return x + y + z;
    }
}
