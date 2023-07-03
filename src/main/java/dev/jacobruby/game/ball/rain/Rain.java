package dev.jacobruby.game.ball.rain;

import dev.jacobruby.game.ball.Ball;
import dev.jacobruby.game.ball.BallType;
import dev.jacobruby.player.DBPlayer;
import dev.jacobruby.util.TimedRunnable;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public class Rain extends TimedRunnable {
    private static double RANGE = 7.0;

    private DBPlayer summoner;
    private Location center;

    public Rain(DBPlayer summoner, Location center) {
        this.summoner = summoner;
        this.center = center;
    }

    @Override
    protected void run(int tick) {
        double angle = Math.random() * Math.PI * 2;

        Vector offset = new Vector(Math.cos(angle), 0, Math.sin(angle));
        offset.multiply(Math.random() * RANGE);
        offset.setY(8);

        Vector spawn = this.center.toVector().add(offset);

        Ball ball = new Ball(this.summoner, BallType.RAIN_DROP, spawn);

        this.center.getWorld().playSound(ball.getBukkitEntity().getLocation(), Sound.ENTITY_AXOLOTL_SPLASH, 3.0f, 1.3f);

        if (tick >= 50) {
            cancel();
        }
    }
}
