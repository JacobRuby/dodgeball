package dev.jacobruby.game.phase.in;

import com.google.common.collect.Lists;
import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.game.DodgeBallGame;
import dev.jacobruby.game.ball.BallType;
import dev.jacobruby.game.phase.GamePhase;
import dev.jacobruby.game.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class InGamePhase extends GamePhase {

    private BukkitTask bukkitTask;

    private int nextBallSpawn;

    public InGamePhase(DodgeBallGame game) {
        super(game);
    }

    @Override
    public void init() {
        registerListener(new InGameListener());

        this.bukkitTask = Bukkit.getScheduler().runTaskTimer(DodgeBallPlugin.get(), this::tick, 0, 1);
        this.resetBallDelay();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.bukkitTask.cancel();
    }

    public void tick() {
        checkEnd();
        if (isEnded()) return;

        if (--this.nextBallSpawn <= 0) {
            dropRandomBall();
            resetBallDelay();
        }
    }

    private void resetBallDelay() {
        this.nextBallSpawn = DodgeBallPlugin.get().getArenaManager().getExtraBallSpawnDelay() + ThreadLocalRandom.current().nextInt(40);
    }

    private void dropRandomBall() {
        if (!this.game.hasOpenItemSlot()) return;

        BallType type;

        List<BallType> values = Arrays.stream(BallType.values()).filter(BallType::spawnsNaturally).toList();
        type = values.get(ThreadLocalRandom.current().nextInt(values.size()));

        this.game.spawnItem(this.game.getRandomAvailableItemSlot(), type);
    }

    private void checkEnd() {
        if (isEnded()) return;

        for (Team team : this.game.getTeams()) {
            if (team.getAliveCount() == 0) {
                this.end();
                break;
            }
        }
    }
}
