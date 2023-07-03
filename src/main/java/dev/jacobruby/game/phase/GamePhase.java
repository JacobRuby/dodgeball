package dev.jacobruby.game.phase;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.game.DodgeBallGame;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GamePhase {
    protected final DodgeBallGame game;

    private boolean ended;

    private List<Listener> listeners = new ArrayList<>();

    protected GamePhase(DodgeBallGame game) {
        this.game = game;
    }

    protected void registerListener(Listener... listeners) {
        PluginManager pm = Bukkit.getPluginManager();

        for (Listener listener : listeners) {
            pm.registerEvents(listener, DodgeBallPlugin.get());
        }

        this.listeners.addAll(Arrays.asList(listeners));
    }

    public void end() {
        if (this.ended) throw new IllegalStateException("Already ended");
        this.ended = true;

        if (this.game.hasNextPhase()) {
            this.game.nextPhase();
        } else {
            this.game.finish();
        }
    }

    public abstract void init();

    public void cleanup() {
        for (Listener listener : this.listeners) {
            HandlerList.unregisterAll(listener);
        }
    }

    public DodgeBallGame getGame() {
        return game;
    }

    public boolean isEnded() {
        return ended;
    }
}
