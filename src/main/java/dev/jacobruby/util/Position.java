package dev.jacobruby.util;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public record Position(double x, double y, double z, float yaw, float pitch) {

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public BlockVector3 toBlockVector3() {
        return BlockVector3.at(this.x, this.y, this.z);
    }

    public static Position from(Location location) {
        return new Position(location.x(), location.y(), location.z(), location.getYaw(), location.getPitch());
    }

    public static Position from(Vector vector) {
        return new Position(vector.getX(), vector.getY(), vector.getZ(), 0, 0);
    }

    public static Position deserialize(String string) {
        String[] args = string.split(" ");

        if (args.length < 3) {
            throw new ArrayIndexOutOfBoundsException("Position must contain at least 3 numbers separated by spaces");
        }

        double x = Double.parseDouble(args[0]), y = Double.parseDouble(args[1]), z = Double.parseDouble(args[2]);
        float yaw = 0, pitch = 0;

        if (args.length >= 4) {
            yaw = Float.parseFloat(args[3]);
        }

        if (args.length >= 5) {
            pitch = Float.parseFloat(args[4]);
        }

        return new Position(x, y, z, yaw, pitch);
    }

}
