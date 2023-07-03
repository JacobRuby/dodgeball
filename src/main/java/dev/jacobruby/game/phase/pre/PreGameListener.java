package dev.jacobruby.game.phase.pre;

import dev.jacobruby.player.DBPlayer;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PreGameListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        DBPlayer player = DBPlayer.get(event.getPlayer());
        if (player.isSpectator()) return;

        Location to = event.getTo();
        Location from = event.getFrom();

        if (to.getX() != from.getX() || to.getZ() != from.getZ()) {
            event.setTo(event.getFrom());
        }
    }
}
