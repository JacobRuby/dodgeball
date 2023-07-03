package dev.jacobruby.game.ball.speed;

import dev.jacobruby.game.ball.Ball;
import dev.jacobruby.player.DBPlayer;
import dev.jacobruby.util.TimedRunnable;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class EffectCloud extends TimedRunnable {

    private DBPlayer summoner;
    private Location location;
    private PotionEffect effect;
    private boolean friendly;

    public EffectCloud(DBPlayer summoner, Location location, PotionEffect effect, boolean friendly) {
        this.summoner = summoner;
        this.location = location;
        this.effect = effect;
        this.friendly = friendly;
    }

    @Override
    protected void run(int tick) {
        for (Player player : this.location.getNearbyPlayers(3)) {
            DBPlayer dbPlayer = DBPlayer.get(player);

            if (!dbPlayer.isAlive() || (dbPlayer.getTeam() == this.summoner.getTeam()) != this.friendly) continue;

            dbPlayer.getPlayer().addPotionEffect(this.effect);
        }

        Color color = this.effect.getType().getColor();
        World world = this.location.getWorld();
        world.spawnParticle(Particle.REDSTONE, this.location, 4, 1.5, 1.5, 1.5, new Particle.DustOptions(color, 2.0f));

        if (tick >= 20 * 10) {
            cancel();
        }
    }
}
