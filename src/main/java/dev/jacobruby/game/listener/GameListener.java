package dev.jacobruby.game.listener;

import dev.jacobruby.game.DodgeBallGame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class GameListener implements Listener {
    private final DodgeBallGame game;

    public GameListener(DodgeBallGame game) {
        this.game = game;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }
}
