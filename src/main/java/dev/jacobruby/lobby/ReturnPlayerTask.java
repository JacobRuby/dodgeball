package dev.jacobruby.lobby;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.util.BezierCurve;
import dev.jacobruby.util.TimedRunnable;
import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class ReturnPlayerTask extends TimedRunnable {
    private Player player;
    private Location to;

    private BezierCurve curve;
    private int totalTicks;

    private ArmorStand ride;

    public ReturnPlayerTask(Player player, Location to) {
        this.player = player;
        this.to = to;

        Location location = player.getLocation();
        Vector a = location.toVector();
        Vector d = to.toVector();
        Vector b = a.clone().add(d.clone().subtract(a).multiply(0.33)).add(new Vector(0, 4, 0));
        Vector c = a.clone().add(d.clone().subtract(a).multiply(0.66)).add(new Vector(0, 4, 0));

        this.curve = new BezierCurve(a, b, c, d);

        this.ride = player.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setMetadata("returnStand", new FixedMetadataValue(DodgeBallPlugin.get(), true));
        });

        this.ride.addPassenger(player);

        this.totalTicks = (int) (d.distance(a) * 5);
    }

    @Override
    protected void run(int tick) {
        double t = tick / (double) this.totalTicks;
        Vector point = this.curve.getPercent(t);
        World world = this.ride.getWorld();
        Location location = point.toLocation(world);

        this.ride.teleport(location, TeleportFlag.EntityState.RETAIN_PASSENGERS);

        world.spawnParticle(Particle.VILLAGER_HAPPY, point.getX(), point.getY(), point.getZ(), 3, 0.05, 0.05, 0.05);

        if (tick % 8 == 0) {
            world.playSound(location, Sound.BLOCK_LAVA_POP, 1.0f, 1f + (float) t);
        }

        if (tick >= this.totalTicks) {
            this.ride.eject();
            this.ride.remove();
            cancel();
        }
    }
}
