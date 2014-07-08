package com.noyhillel.battledome.arena;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.World;

@EqualsAndHashCode(of = {"x", "y", "z", "yaw", "pitch"}, doNotUseGetters = false)
@ToString(of = {"x", "y", "z", "yaw", "pitch"})
public final class Point {
    @Getter private Double x;
    @Getter private Double y;
    @Getter private Double z;
    @Getter private Float yaw;
    @Getter private Float pitch;

    public Point(Double x, Double y, Double z) {
        this(x,y,z,0,0);
    }
    public Point(Double x, Double y, Double z, float pitch, float yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public Point(Integer x, Integer y, Integer z, Integer pitch, Integer yaw) {
        this((double)x,(double)y,(double)z,(float)pitch,(float)yaw);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z, pitch, yaw);
    }
}
