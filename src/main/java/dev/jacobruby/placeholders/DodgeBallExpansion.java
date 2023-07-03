package dev.jacobruby.placeholders;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.game.DodgeBallGame;
import dev.jacobruby.game.team.Team;
import dev.jacobruby.player.DBPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DodgeBallExpansion extends PlaceholderExpansion {
    private DodgeBallPlugin plugin;

    public DodgeBallExpansion(DodgeBallPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "dodgeball";
    }

    @Override
    public @NotNull String getAuthor() {
        return "JacobRuby";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.0.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        DBPlayer dbPlayer = null;

        if (player != null) dbPlayer = DBPlayer.get(player);

        if (params.equals("player-kills")) {
            return String.valueOf(dbPlayer.getKills());
        }

        if (params.equals("player-team")) {
            return dbPlayer.getTeam().getName();
        }

        if (params.equals("player-alive")) {
            return dbPlayer.isAlive() ? "Alive" : "Dead";
        }

        if (params.startsWith("team-alive-")) {
            String teamId = params.replaceFirst("team-alive-", "");

            DodgeBallGame game = this.plugin.getGame();
            if (game == null) {
                return "0";
            }

            Team team = game.getTeam(teamId);
            if (team == null) {
                return "0";
            }

            return String.valueOf(team.getAliveCount());
        }

        return null;
    }
}
