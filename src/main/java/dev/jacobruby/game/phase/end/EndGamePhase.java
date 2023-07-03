package dev.jacobruby.game.phase.end;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.config.ConfigManager;
import dev.jacobruby.game.DodgeBallGame;
import dev.jacobruby.game.phase.GamePhase;
import dev.jacobruby.game.team.Team;
import dev.jacobruby.player.DBPlayer;
import dev.jacobruby.player.PlayerManager;
import dev.jacobruby.scoreboard.BoardManager;
import dev.jacobruby.util.Title;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

public class EndGamePhase extends GamePhase {
    private Team winner;

    public EndGamePhase(DodgeBallGame game) {
        super(game);
    }

    @Override
    public void init() {
        DodgeBallPlugin plugin = DodgeBallPlugin.get();

        registerListener(new EndGameListener());

        for (Team team : this.game.getTeams()) {
            if (team.getAliveCount() > 0) {
                this.winner = team;
                break;
            }
        }

        PlayerManager playerManager = plugin.getPlayerManager();
        ConfigManager configManager = plugin.getConfigManager();
        if (this.winner != null) {
            for (DBPlayer player : playerManager.getPlayers()) {
                if (player.getTeam() == null) continue;

                if (player.getTeam() == this.winner) {
                    configManager.dispatchCommands("commands.victory", player.getPlayer());
                } else {
                    configManager.dispatchCommands("commands.lost", player.getPlayer());
                }
            }
        }

        BoardManager boardManager = plugin.getBoardManager();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text("        ").decorate(TextDecoration.STRIKETHROUGH));

        lines.add(Component.empty());

        lines.add(Component.text("GAME OVER").decorate(TextDecoration.BOLD).color(NamedTextColor.RED));
        if (this.winner == null) {
            lines.add(Component.text("Draw"));
        } else {
            lines.add(Component.text(this.winner.getName()).color(this.winner.getTextColor()));
            lines.add(Component.text("is victorious!"));
        }

        lines.add(Component.empty());
        lines.add(Component.text("play.example.net").decorate(TextDecoration.ITALIC).color(TextColor.color(0x2d8fcc)));

        boardManager.setLines(lines);

        if (this.winner == null) {
            Title.sendAll(
                Component.text("DRAW").decorate(TextDecoration.BOLD).color(NamedTextColor.RED),
                Component.text("We have to ask ourselves, \"how did we get here?\"").decorate(TextDecoration.ITALIC).color(NamedTextColor.GRAY),
                10, 80, 10);
        } else {
            Component subtitle = Component.text(this.winner.getName()).color(this.winner.getTextColor()).append(Component.text(" is victorious").color(NamedTextColor.GRAY));

            for (DBPlayer player : playerManager.getPlayers()) {
                Team team = player.getTeam();

                if (team == null) {
                    Title.send(player.getPlayer(),
                        Component.text("GAME OVER").decorate(TextDecoration.BOLD).color(NamedTextColor.RED),
                        subtitle,
                        10, 80, 10);
                } else if (team == this.winner) {
                    Title.send(player.getPlayer(),
                        Component.text("VICTORY").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN),
                        subtitle,
                        10, 80, 10);
                } else {
                    Title.send(player.getPlayer(),
                        Component.text("DEFEAT").decorate(TextDecoration.BOLD).color(NamedTextColor.RED),
                        subtitle,
                        10, 80, 10);
                }
            }
        }

        new FireworksTask(this, this.winner).runTaskTimer(plugin, 0, 1);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}
