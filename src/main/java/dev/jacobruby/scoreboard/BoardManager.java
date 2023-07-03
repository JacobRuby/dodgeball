package dev.jacobruby.scoreboard;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;

public class BoardManager {

    private Scoreboard scoreboard;
    private Int2ObjectMap<String> actors;
    private Int2ObjectMap<Team> teams;

    private Objective objective;

    // I have no idea what I'm doing here
    public BoardManager() {

    }

    public void init() {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.actors = new Int2ObjectArrayMap<>();
        this.teams = new Int2ObjectArrayMap<>();

        ChatColor[] values = ChatColor.values();
        for (int i = 0; i < values.length; i++) {
            Team team = this.scoreboard.registerNewTeam("line" + i);
            String actor = String.valueOf(values[i]);
            team.addEntry(actor);

            this.actors.put(i, actor);
            this.teams.put(i, team);
        }

        this.objective = this.scoreboard.registerNewObjective("display", Criteria.DUMMY,
            MiniMessage.miniMessage().deserialize("<gradient:#3443eb:#2d8fcc>Dodge Ball"));
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void setLineCount(int lines) {
        for (int i = 1; i <= 15; i++) {
            String actor = this.actors.get(i - 1);
            Score score = this.objective.getScore(actor);

            if (i <= lines) {
                score.setScore(16 - i);
            } else {
                score.resetScore();
            }
        }
    }

    public void setLine(int line, Component text) {
        Team team = this.teams.get(line);
        team.prefix(text);
    }

    public void setLines(List<Component> lines) {
        clear();

        for (int i = 0; i < lines.size(); i++) {
            setLine(i, lines.get(i));
        }

        setLineCount(lines.size());
    }

    public void clear() {
        setLineCount(0);
        for (Team value : this.teams.values()) {
            value.prefix(Component.empty());
        }
    }

    public Team getNewTeam(dev.jacobruby.game.team.Team dodgeballTeam) {
        Team team = this.scoreboard.registerNewTeam(dodgeballTeam.getId());
        team.color(NamedTextColor.nearestTo(dodgeballTeam.getTextColor()));
        return team;
    }

    public void removeTeam(dev.jacobruby.game.team.Team dodgeballTeam) {
        Team team = this.scoreboard.getTeam(dodgeballTeam.getId());

        if (team == null) return;
        team.unregister();
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }
}
