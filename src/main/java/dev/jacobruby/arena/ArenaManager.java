package dev.jacobruby.arena;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.config.ConfigManager;
import dev.jacobruby.game.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ArenaManager {
    private final ConfigManager configManager;

    private File arenaSchematicFile;
    private World gameWorld;
    private BlockVector3 pasteLocation;
    private Location spectatorSpawn;
    private BoundingBox arenaBoundingBox;

    private List<Location> ballSpawns;
    private int extraBallSpawnDelay;

    public ArenaManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void init() {
        DodgeBallPlugin plugin = DodgeBallPlugin.get();
        // Put the default schematic in the data folder.
        plugin.saveResource("schematics/Dodgeball_Lobby.schem", false);

        this.arenaSchematicFile = configManager.getDataFile("arena.schematic");
        if (!this.arenaSchematicFile.exists()) {
            throw new IllegalArgumentException("arena.schematic file does not exist! %s".formatted(arenaSchematicFile.getAbsolutePath()));
        }

        this.gameWorld = configManager.getWorld("arena.world");
        this.gameWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        this.gameWorld.setGameRule(GameRule.DO_FIRE_TICK, false);
        this.gameWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        this.gameWorld.setGameRule(GameRule.MOB_GRIEFING, false);
        this.gameWorld.setGameRule(GameRule.DO_FIRE_TICK, false);
        this.gameWorld.setGameRule(GameRule.DO_TILE_DROPS, false);
        this.gameWorld.setGameRule(GameRule.DO_ENTITY_DROPS, false);

        Bukkit.getScheduler().runTaskLater(plugin, () -> this.gameWorld.getEntities().forEach(entity -> {
            if (entity instanceof Player) return;
            entity.remove();
        }), 1);

        this.pasteLocation = configManager.getPosition("arena.location").toBlockVector3();
        this.spectatorSpawn = configManager.getPosition("arena.spectatorSpawn").toLocation(this.gameWorld);
        this.arenaBoundingBox = configManager.getBoundingBox("arena.boundingBox");

        this.ballSpawns = configManager.getPositionListAsLocations("arena.ballSpawns", this.gameWorld);
        this.extraBallSpawnDelay = configManager.getConfig().getInt("arena.extraBallSpawnDelay");
    }

    // Found this on spigot
    public void pasteArenaSchematic() throws IOException, WorldEditException {
        ClipboardFormat format = ClipboardFormats.findByFile(this.arenaSchematicFile);

        ClipboardReader reader = format.getReader(new FileInputStream(this.arenaSchematicFile));
        Clipboard clipboard = reader.read();

        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(this.gameWorld);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(adaptedWorld)) {
            Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                .to(this.pasteLocation).build();

            Operations.complete(operation);
        }
    }

    public World getGameWorld() {
        return gameWorld;
    }

    public Location getSpectatorSpawn() {
        return spectatorSpawn;
    }

    public BoundingBox getArenaBoundingBox() {
        return arenaBoundingBox.clone();
    }

    public List<Location> getSpawns(Team team) {
        return this.configManager.getPositionListAsLocations("arena." + team.getId() + ".spawns", this.gameWorld);
    }

    public BoundingBox getBoundingBox(Team team) {
        return this.configManager.getBoundingBox("arena." + team.getId() + ".boundingBox");
    }

    public Location getItemRespawnLocation(Team team) {
        return this.configManager.getPosition("arena." + team.getId() + ".itemRespawn").toLocation(this.gameWorld);
    }

    public List<Location> getBallSpawns() {
        return List.copyOf(ballSpawns);
    }

    public int getExtraBallSpawnDelay() {
        return extraBallSpawnDelay;
    }
}
