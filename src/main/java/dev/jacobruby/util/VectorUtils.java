package dev.jacobruby.util;

import org.bukkit.util.Vector;

public class VectorUtils {
    private VectorUtils() {
    }

    public static Vector deserialize(String string) {
        String[] args = string.split(",");

        if (args.length < 3) {
            throw new ArrayIndexOutOfBoundsException("Vector must contain at least 3 numbers separated by commas");
        }

        double x = Double.parseDouble(args[0]), y = Double.parseDouble(args[1]), z = Double.parseDouble(args[2]);
        return new Vector(x, y, z);
    }
}
