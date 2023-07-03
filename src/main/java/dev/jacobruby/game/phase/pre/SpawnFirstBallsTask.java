package dev.jacobruby.game.phase.pre;

import dev.jacobruby.game.DodgeBallGame;
import dev.jacobruby.game.ball.BallType;
import dev.jacobruby.util.TimedRunnable;

public class SpawnFirstBallsTask extends TimedRunnable {
    private DodgeBallGame game;
    private int amount;

    public SpawnFirstBallsTask(DodgeBallGame game) {
        this.game = game;
        this.amount = this.game.getPlayers().size() / 2;
    }

    @Override
    protected void run(int tick) {
        this.game.spawnItem(this.game.getRandomAvailableItemSlot(), BallType.SNOW_BALL);

        if (tick + 1 >= this.amount) {
            cancel();
        }
    }
}
