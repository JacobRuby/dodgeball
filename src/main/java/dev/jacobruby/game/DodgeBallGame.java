package dev.jacobruby.game;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.game.ball.BallItem;
import dev.jacobruby.game.ball.BallType;
import dev.jacobruby.game.listener.GameListener;
import dev.jacobruby.game.phase.GamePhase;
import dev.jacobruby.game.phase.end.EndGamePhase;
import dev.jacobruby.game.phase.in.InGamePhase;
import dev.jacobruby.game.phase.pre.PreGamePhase;
import dev.jacobruby.game.team.Team;
import dev.jacobruby.lobby.LobbyNPC;
import dev.jacobruby.player.DBPlayer;
import dev.jacobruby.arena.ArenaManager;
import dev.jacobruby.scoreboard.BoardManager;
import dev.jacobruby.util.RingQueue;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.MinecraftServer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DodgeBallGame {

    private final DodgeBallPlugin plugin;
    private GameState state;

    private GameListener listener;

    private GamePhase currentPhase;
    private Queue<GamePhase> phases;

    private List<Location> ballSpawns;
    private IntObjectMap<BallItem> spawnedItems;

    private BoundingBox boundingBox;
    private World world;

    private List<DBPlayer> players;
    private List<DBPlayer> spectators;

    private List<Team> teams;

    private Map<Block, DBPlayer> fireBlocks;
    private int startTick;

    public DodgeBallGame() {
        this.plugin = DodgeBallPlugin.get();
        this.players = new ArrayList<>();
        this.fireBlocks = new HashMap<>();
    }

    public void init(List<DBPlayer> players) {
        this.setState(GameState.INITIALIZING);

        ArenaManager arenaManager = this.plugin.getArenaManager();
        this.ballSpawns = arenaManager.getBallSpawns();
        this.spawnedItems = new IntObjectHashMap<>();

        this.boundingBox = arenaManager.getArenaBoundingBox();
        this.world = arenaManager.getGameWorld();

        this.world.getEntities().forEach(entity -> {
            if (entity instanceof Player) return;
            if (((CraftEntity) entity).getHandle() instanceof LobbyNPC) return;
            entity.remove();
        });

        setupTeams();
        setupPlayers(players);
        setupScoreboard();
        setupPhases();

        this.listener = new GameListener(this);
        this.plugin.registerListener(this.listener);

        this.nextPhase();
        this.setState(GameState.ACTIVE);
        this.startTick = MinecraftServer.currentTick;
    }

    private void setupTeams() {
        this.teams = new ArrayList<>();

        // Kind of primitive, but it works
        Team red = new Team("red", "Red Team", NamedTextColor.RED, DyeColor.RED);
        Team blue = new Team("blue", "Blue Team", NamedTextColor.BLUE, DyeColor.BLUE);

        this.teams.add(red);
        this.teams.add(blue);

        ArenaManager arenaManager = this.plugin.getArenaManager();
        for (Team team : this.teams) {
            team.setSpawns(arenaManager.getSpawns(team));
            team.setBoundingBox(arenaManager.getBoundingBox(team));
            team.setItemRespawnLocation(arenaManager.getItemRespawnLocation(team));
        }
    }

    private void setupPlayers(List<DBPlayer> players) {
        this.players = List.copyOf(players);

        this.spectators = new ArrayList<>();
        for (DBPlayer player : this.plugin.getPlayerManager().getPlayers()) {
            if (players.contains(player)) continue;

            this.spectators.add(player);
        }

        RingQueue<Team> teamRingQueue = new RingQueue<>(this.teams);
        for (DBPlayer player : this.players) {
            Team team = teamRingQueue.pollFirst();
            team.join(player);

            player.setAlive(true);
        }

        for (Team team : this.teams) {
            List<DBPlayer> members = team.getMembers();
            int memberCount = members.size();
            List<Location> spawns = team.getSpawns();
            int spawnCount = spawns.size();

            // This is basically to spread them out over an "even" distance on the spawn platform.
            for (int i = 0; i < members.size(); i++) {
                DBPlayer member = members.get(i);

                double percent = i / (double) memberCount;

                int spawnLocationIndex = (int) (percent * (spawnCount - 1));
                Location spawn = spawns.get(spawnLocationIndex);
                member.getPlayer().teleport(spawn);
            }
        }
    }

    private void setupScoreboard() {
        BoardManager boardManager = this.plugin.getBoardManager();

        List<Component> lines = new ArrayList<>();
        lines.add(Component.text("        ").decorate(TextDecoration.STRIKETHROUGH));

        for (Team team : this.teams) {
            lines.add(Component.text(team.getName()).decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED).color(team.getTextColor()));
            lines.add(Component.text(""));
        }

        lines.add(Component.empty());
        lines.add(Component.text("play.example.net").decorate(TextDecoration.ITALIC).color(TextColor.color(0x2d8fcc)));

        boardManager.setLines(lines);

        this.updateAliveLines();
    }

    private void setupPhases() {
        this.phases = new ArrayDeque<>();

        this.phases.add(new PreGamePhase(this));
        this.phases.add(new InGamePhase(this));
        this.phases.add(new EndGamePhase(this));
    }

    public void updateAliveLines() {
        BoardManager boardManager = this.plugin.getBoardManager();

        int line = 2;
        for (Team team : this.teams) {
            long alive = team.getAliveCount();
            boardManager.setLine(line, Component.text("» " + alive + " Alive"));
            line += 2;
        }
    }

    public boolean hasOpenItemSlot() {
        return this.spawnedItems.size() < this.ballSpawns.size();
    }

    public BallItem getItemSpawnSlot(int slot) {
        return this.spawnedItems.get(slot);
    }

    public BallItem spawnItem(int slot, BallType type) {
        if (this.spawnedItems.containsKey(slot)) return null;

        Location location = this.ballSpawns.get(slot);
        BallItem item = type.dropItem(location);
        item.setSpawnedSlot(slot);

        World world = location.getWorld();
        world.playSound(location, Sound.ENTITY_ALLAY_ITEM_GIVEN, 10f, 0.5f);
        world.spawnParticle(Particle.DUST_COLOR_TRANSITION, location, 20, 0.5, 0.5, 0.5, new Particle.DustTransition(Color.WHITE, type.getColor(), 2.0f));

        this.spawnedItems.put(slot, item);
        return item;
    }

    public void removeItem(int slot) {
        this.spawnedItems.remove(slot);
    }

    public int getRandomAvailableItemSlot() {
        IntList list = new IntArrayList();

        for (int slot = 0; slot < this.ballSpawns.size(); slot++) {
            if (this.getItemSpawnSlot(slot) == null) {
                list.add(slot);
            }
        }

        if (list.isEmpty()) return -1;

        return list.getInt(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public World getWorld() {
        return world;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public boolean hasNextPhase() {
        return !this.phases.isEmpty();
    }

    // If I had more time I would definitely reorganize how the phases switch.
    public void nextPhase() {
        if (!hasNextPhase()) return;

        GamePhase oldPhase = this.currentPhase;
        this.currentPhase = this.phases.poll();
        this.currentPhase.init();

        if (oldPhase != null) {
            oldPhase.cleanup();
        }
    }

    public void finish() {
        this.setState(GameState.CLEANUP);
        if (this.currentPhase != null) {
            this.currentPhase.cleanup();
        }

        for (BallItem item : this.spawnedItems.values()) {
            if (item == null) continue;
            item.discard();
        }

        HandlerList.unregisterAll(this.listener);

        BoardManager boardManager = this.plugin.getBoardManager();
        this.teams.forEach(boardManager::removeTeam);

        this.setState(GameState.FINISHED);
        this.plugin.resetToLobby();
    }

    public void onPlayerJoin(DBPlayer player) {
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> player.setSpectator(true), 3);
        player.getPlayer().teleport(DodgeBallPlugin.get().getArenaManager().getSpectatorSpawn());
    }

    public void onPlayerQuit(DBPlayer player) {
        Team team = player.getTeam();
        if (team != null) {
            if (player.isAlive()) {
                player.killedBy(null);
            }
        }
    }

    public List<DBPlayer> getPlayers() {
        return players;
    }

    public List<Team> getTeams() {
        return teams;
    }

    @Nullable
    public Team getTeam(String id) {
        for (Team team : this.teams) {
            if (team.getId().equals(id)) {
                return team;
            }
        }

        return null;
    }

    public Map<Block, DBPlayer> getFireBlocks() {
        return fireBlocks;
    }

    public int getStartTick() {
        return startTick;
    }

    public DodgeBallPlugin getPlugin() {
        return plugin;
    }
}
