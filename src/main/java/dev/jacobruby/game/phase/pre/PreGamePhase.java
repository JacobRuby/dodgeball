package dev.jacobruby.game.phase.pre;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.game.DodgeBallGame;
import dev.jacobruby.game.phase.GamePhase;

public class PreGamePhase extends GamePhase {

    public PreGamePhase(DodgeBallGame game) {
        super(game);
    }

    @Override
    public void init() {
        registerListener(new PreGameListener());

        new SpawnFirstBallsTask(this.game).runTaskTimer(DodgeBallPlugin.get(), 40, 8);
        new CountdownTask(this).runTaskTimer(DodgeBallPlugin.get(), 20, 0);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}
