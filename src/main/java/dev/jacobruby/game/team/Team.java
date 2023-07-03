package dev.jacobruby.game.team;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.player.DBPlayer;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private final List<DBPlayer> members = new ArrayList<>();

    private String id;
    private String name;
    private TextColor textColor;
    private DyeColor dyeColor;

    private org.bukkit.scoreboard.Team scoreboardTeam;

    private List<Location> spawns;
    private BoundingBox boundingBox;
    private Location itemRespawnLocation;

    public Team(String id, String name, TextColor textColor, DyeColor dyeColor) {
        this.id = id;
        this.name = name;
        this.textColor = textColor;
        this.dyeColor = dyeColor;

        this.scoreboardTeam = DodgeBallPlugin.get().getBoardManager().getNewTeam(this);
    }

    public void join(DBPlayer player) {
        if (!this.members.add(player)) return;
        player.setTeam(this);
        this.scoreboardTeam.addPlayer(player.getPlayer());
    }

    public void setSpawns(List<Location> spawns) {
        this.spawns = spawns;
    }

    public List<Location> getSpawns() {
        return List.copyOf(spawns);
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setItemRespawnLocation(Location itemRespawnLocation) {
        this.itemRespawnLocation = itemRespawnLocation;
    }

    public Location getItemRespawnLocation() {
        return itemRespawnLocation.clone();
    }

    public int getMemberCount() {
        return this.members.size();
    }

    public List<DBPlayer> getMembers() {
        return List.copyOf(members);
    }

    public List<DBPlayer> getAliveMembers() {
        return this.members.stream().filter(DBPlayer::isAlive).toList();
    }

    public long getAliveCount() {
        return this.members.stream().filter(DBPlayer::isAlive).count();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TextColor getTextColor() {
        return textColor;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }
}
