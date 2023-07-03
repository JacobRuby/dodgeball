package dev.jacobruby.player;

import dev.jacobruby.DodgeBallPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerManager implements Listener {

    private final Map<UUID, DBPlayer> players = new HashMap<>();
    private final Map<UUID, DBPlayer> loggedOutPlayers = new HashMap<>();

    public PlayerManager() {
    }

    public void init() {
        DodgeBallPlugin.get().registerListener(this);
    }

    private DBPlayer registerNewPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        DBPlayer dbPlayer = new DBPlayer(player);
        this.players.put(uuid, dbPlayer);
        return dbPlayer;
    }

    @Nullable
    public DBPlayer getPlayer(Player player) {
        return this.getPlayer(player.getUniqueId());
    }

    @Nullable
    public DBPlayer getPlayer(UUID uuid) {
        return this.players.get(uuid);
    }

    public List<DBPlayer> getPlayers() {
        return List.copyOf(players.values());
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (this.players.containsKey(uuid)) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("You are already logged in?"));
            return;
        }

        DBPlayer dbPlayer;
        if (this.loggedOutPlayers.containsKey(uuid)) {
            // TODO
            dbPlayer = null;
        } else {
            dbPlayer = registerNewPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        DBPlayer dbPlayer = DBPlayer.get(player);

        DodgeBallPlugin plugin = DodgeBallPlugin.get();

        player.setScoreboard(plugin.getBoardManager().getScoreboard());
        dbPlayer.clearInventory();

        if (plugin.isGameRunning()) {
            plugin.getGame().onPlayerJoin(dbPlayer);
        } else {
            plugin.getLobby().queuePlayer(dbPlayer);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!this.players.containsKey(uuid)) {
            // https://youtu.be/7J3QGgXbyvc
            return;
        }

        DBPlayer dbPlayer = this.players.remove(uuid);

        DodgeBallPlugin plugin = DodgeBallPlugin.get();
        if (plugin.isGameRunning()) {
            plugin.getGame().onPlayerQuit(dbPlayer);
        } else {
            plugin.getLobby().dequeuePlayer(dbPlayer);
        }
    }

    @EventHandler
    public void onPlayerInitialSpawn(PlayerSpawnLocationEvent event) {
        DodgeBallPlugin plugin = DodgeBallPlugin.get();
        if (plugin.isGameRunning()) {
            // TODO
            event.setSpawnLocation(plugin.getLobby().getSpawn());
        } else {
            event.setSpawnLocation(plugin.getLobby().getSpawn());
        }
    }
}
