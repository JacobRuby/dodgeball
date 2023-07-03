package dev.jacobruby.util;

import org.bukkit.util.Vector;

public class BezierCurve {
    private final Vector a, b, c, d;

    public BezierCurve(Vector a, Vector b, Vector c) {
        this(a, b, c, null);
    }

    public BezierCurve(Vector a, Vector b, Vector c, Vector d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public Vector getPercent(double t) {
        double u = 1 - t;

        Vector v = new Vector();
        if (this.d == null) {
            // Quadratic
            v.add(this.a.clone().multiply(u * u));
            v.add(this.b.clone().multiply(u * t * 2));
            v.add(this.c.clone().multiply(t * t));
        } else {
            // Cubic
            v.add(this.a.clone().multiply(u * u * u));
            v.add(this.b.clone().multiply(u * u * t * 3));
            v.add(this.c.clone().multiply(u * t * t * 3));
            v.add(this.d.clone().multiply(t * t * t));
        }

        return v;
    }
}
