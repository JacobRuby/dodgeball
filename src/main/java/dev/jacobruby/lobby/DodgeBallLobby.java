package dev.jacobruby.lobby;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.config.ConfigManager;
import dev.jacobruby.player.DBPlayer;
import dev.jacobruby.scoreboard.BoardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DodgeBallLobby {
    private World world;
    private Location spawn;
    private BoundingBox boundingBox;
    private int minimumPlayers;

    private List<DBPlayer> queuedPlayers = new ArrayList<>();
    private LobbyNPC npc;

    private boolean starting;
    private int countdown;

    public DodgeBallLobby() {
    }

    public void init() {
        DodgeBallPlugin plugin = DodgeBallPlugin.get();
        ConfigManager configManager = plugin.getConfigManager();

        this.world = configManager.getWorld("lobby.world");
        this.spawn = configManager.getPosition("lobby.spawn").toLocation(this.world);
        this.boundingBox = configManager.getBoundingBox("lobby.boundingBox");
        String npcName = configManager.getConfig().getString("lobby.npc.name");
        Location npcLocation = configManager.getPosition("lobby.npc.location").toLocation(this.world);
        EntityType<?> npcEntityType = configManager.getEntityType("lobby.npc.entityType");
        ConfigurationSection npcSkin = configManager.getConfig().getConfigurationSection("lobby.npc.skin");

        this.npc = new LobbyNPC(npcLocation, npcName);
        this.npc.setFakeType(npcEntityType);
        if (npcEntityType == EntityType.PLAYER) {
            this.npc.setSkin(npcSkin.getString("signature"), npcSkin.getString("value"));
        }

        this.minimumPlayers = configManager.getConfig().getInt("minimumPlayers");

        plugin.registerListener(
                new LobbyListener(this)
        );

        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0, 1);
    }

    private void tick() {


    }

    public void setup() {
        BoardManager board = DodgeBallPlugin.get().getBoardManager();
        board.setLines(List.of(
            Component.text("        ").decorate(TextDecoration.STRIKETHROUGH),
            Component.text("Lobby").decorate(TextDecoration.BOLD).color(NamedTextColor.BLUE),
            Component.empty(),
            Component.text(""),
            Component.text(""),
            Component.empty(),
            Component.text("play.example.net").decorate(TextDecoration.ITALIC).color(TextColor.color(0x2d8fcc))
        ));

        for (DBPlayer dbPlayer : DodgeBallPlugin.get().getPlayerManager().getPlayers()) {
            this.queuePlayer(dbPlayer);
        }

        Collections.shuffle(this.queuedPlayers);

        updatePlayerLine();
        updateStartingLine();
    }

    public void updatePlayerLine() {
        BoardManager board = DodgeBallPlugin.get().getBoardManager();
        board.setLine(4, Component.text("» " + this.queuedPlayers.size() + "/20").color(NamedTextColor.BLUE));
    }

    public void updateStartingLine() {
        BoardManager board = DodgeBallPlugin.get().getBoardManager();
        board.setLine(3,
            this.starting ? Component.text("Starting in " + this.countdown).color(NamedTextColor.GREEN) :
                Component.text("Waiting for players").color(NamedTextColor.WHITE)
            );
    }

    public void setStarting(boolean starting) {
        if (this.starting == starting) return;
        this.starting = starting;

        if (starting) {
            this.countdown = 10;

        }
    }

    public void startGame() {
        List<DBPlayer> players = List.copyOf(this.queuedPlayers);
        this.queuedPlayers.clear();
        DodgeBallPlugin.get().startNewGame(players);
    }

    public boolean canStart() {
        return this.getQueueCount() >= this.getMinimumPlayers();
    }

    public World getWorld() {
        return world;
    }

    public Location getSpawn() {
        return spawn.clone();
    }

    public BoundingBox getBoundingBox() {
        return boundingBox.clone();
    }

    public void queuePlayer(DBPlayer dbPlayer) {
        Player player = dbPlayer.getPlayer();
        player.teleport(this.spawn);
        dbPlayer.setSpectator(false);
        dbPlayer.clearInventory();
        this.queuedPlayers.add(dbPlayer);
        this.updatePlayerLine();
    }

    public void dequeuePlayer(DBPlayer player) {
        this.queuedPlayers.remove(player);
        this.updatePlayerLine();
    }

    public int getMinimumPlayers() {
        return minimumPlayers;
    }

    public List<DBPlayer> getQueuedPlayers() {
        return queuedPlayers;
    }

    public int getQueueCount() {
        return this.queuedPlayers.size();
    }

    public int getRemainingPlayersNeeded() {
        return this.minimumPlayers - this.getQueueCount();
    }

    public LobbyNPC getNpc() {
        return npc;
    }
}
