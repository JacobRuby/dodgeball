package dev.jacobruby.game.phase.pre;

import dev.jacobruby.game.DodgeBallGame;
import dev.jacobruby.player.DBPlayer;
import dev.jacobruby.util.TimedRunnable;
import dev.jacobruby.util.Title;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CountdownTask extends TimedRunnable {
    private PreGamePhase phase;
    private final DodgeBallGame game;

    private int secondsRemaining = 4;

    public CountdownTask(PreGamePhase phase) {
        this.phase = phase;
        this.game = phase.getGame();
    }

    @Override
    protected void run(int tick) {
        if (tick % 20 == 0) {
            this.secondsRemaining--;

            for (DBPlayer dbPlayer : game.getPlayers()) {
                Player player = dbPlayer.getPlayer();
                player.setLevel(this.secondsRemaining);
            }

            if (this.secondsRemaining <= 0) {
                for (DBPlayer dbPlayer : this.game.getPlayers()) {
                    Player player = dbPlayer.getPlayer();
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
                }

                Title.sendAll(Component.empty(),
                    Component.text("DODGE").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN), 3, 34, 3);
                cancel();
                this.phase.end();
                return;
            }

            if (this.secondsRemaining < 4) {
                for (DBPlayer dbPlayer : this.game.getPlayers()) {
                    Player player = dbPlayer.getPlayer();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                }

                Title.sendAll(Component.empty(),
                    Component.text(this.secondsRemaining).decorate(TextDecoration.BOLD).color(NamedTextColor.RED), 3, 14, 3);
            }
        }
    }
}
