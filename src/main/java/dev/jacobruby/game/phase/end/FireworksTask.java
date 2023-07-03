package dev.jacobruby.game.phase.end;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.game.DodgeBallGame;
import dev.jacobruby.game.team.Team;
import dev.jacobruby.util.TimedRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class FireworksTask extends TimedRunnable {
    private EndGamePhase phase;
    private Team winner;
    private FireworkEffect fireworkEffect;

    private BoundingBox boundingBox;

    public FireworksTask(EndGamePhase phase, Team winner) {
        this.phase = phase;
        this.winner = winner;

        if (this.winner != null) {
            Color teamColor = this.winner.getDyeColor().getColor();
            this.fireworkEffect = FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(
                Color.WHITE.mixColors(teamColor),
                teamColor,
                Color.BLACK.mixColors(teamColor)
            ).withFade(
                teamColor,
                Color.BLACK.mixColors(teamColor),
                Color.WHITE.mixColors(teamColor)
            ).build();

            this.boundingBox = phase.getGame().getBoundingBox();
        }
    }

    @Override
    protected void run(int tick) {
        if (tick >= 200) {
            cancel();
            this.phase.end();
            return;
        }

        if (this.fireworkEffect != null && tick % 15 == 0) {
            spawnFirework();
        }
    }

    private void spawnFirework() {
        DodgeBallGame game = this.phase.getGame();
        Location location = this.boundingBox.getMin().add(new Vector(
            this.boundingBox.getWidthX() * Math.random(),
            this.boundingBox.getHeight() * Math.random(),
            this.boundingBox.getWidthZ() * Math.random()
        )).toLocation(game.getWorld());

        Firework fw = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(this.fireworkEffect);
        fw.setFireworkMeta(meta);

        Bukkit.getScheduler().runTask(DodgeBallPlugin.get(), fw::detonate);
    }
}
