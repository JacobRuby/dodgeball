package dev.jacobruby;


import com.sk89q.worldedit.WorldEditException;
import dev.jacobruby.config.ConfigManager;
import dev.jacobruby.game.DodgeBallGame;
import dev.jacobruby.lobby.DodgeBallLobby;
import dev.jacobruby.placeholders.DodgeBallExpansion;
import dev.jacobruby.player.DBPlayer;
import dev.jacobruby.player.PlayerManager;
import dev.jacobruby.arena.ArenaManager;
import dev.jacobruby.scoreboard.BoardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class DodgeBallPlugin extends JavaPlugin implements Listener {

    private static DodgeBallPlugin INSTANCE;

    public static DodgeBallPlugin get() {
        return Objects.requireNonNull(INSTANCE, "The plugin has not been initialized yet!");
    }

    private ConfigManager configManager;
    private ArenaManager arenaManager;
    private BoardManager boardManager;
    private PlayerManager playerManager;
    private DodgeBallLobby lobby;

    @Nullable
    private DodgeBallGame game;

    public DodgeBallPlugin() {
        INSTANCE = this;

        this.configManager = new ConfigManager(this);
        this.arenaManager = new ArenaManager(this.configManager);
        this.boardManager = new BoardManager();
        this.playerManager = new PlayerManager();
        this.lobby = new DodgeBallLobby();
    }

    @Override
    public void onEnable() {
        this.arenaManager.init();
        this.boardManager.init();
        this.playerManager.init();
        this.lobby.init();
        this.lobby.setup();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            registerPlaceholders();
        }
    }

    private void registerPlaceholders() {
        new DodgeBallExpansion(this).register();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public BoardManager getBoardManager() {
        return boardManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public DodgeBallLobby getLobby() {
        return lobby;
    }

    public void startNewGame(List<DBPlayer> players) {
        if (this.game != null) {
            throw new IllegalStateException("A game is already running!");
        }

        if (players.size() == 0) return;

        try {
            this.arenaManager.pasteArenaSchematic();
        } catch (IOException | WorldEditException e) {
            Bukkit.broadcast(Component.text("Failed to load arena!").color(NamedTextColor.RED));
            throw new RuntimeException(e);
        }

        DodgeBallGame game = new DodgeBallGame();
        game.init(players);

        this.game = game;
    }

    public void resetToLobby() {
        if (this.game == null) {
            throw new IllegalStateException("There is no game running!");
        }

        this.game = null;
        this.lobby.setup();
    }

    public boolean isGameRunning() {
        return this.game != null;
    }

    public DodgeBallGame getGame() {
        return game;
    }

    public void registerListener(Listener... listeners) {
        PluginManager pluginManager = Bukkit.getPluginManager();

        for (Listener listener : listeners) {
            pluginManager.registerEvents(listener, this);
        }
    }
}