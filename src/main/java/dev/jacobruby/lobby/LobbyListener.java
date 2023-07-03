package dev.jacobruby.lobby;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.game.team.Team;
import dev.jacobruby.player.DBPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.MinecraftServer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

public class LobbyListener implements Listener {
    private DodgeBallLobby lobby;

    public LobbyListener(DodgeBallLobby lobby) {
        this.lobby = lobby;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteractNpc(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getRightClicked() != this.lobby.getNpc().getBukkitEntity()) return;

        Player player = event.getPlayer();
        if (DodgeBallPlugin.get().isGameRunning()) {
            player.sendMessage(Component.text("The game is already running!").color(NamedTextColor.RED));
        }

        if (this.lobby.canStart()) {
            this.lobby.startGame();
        } else {
            player.sendMessage(Component.text("We need " + this.lobby.getRemainingPlayersNeeded() + " more players to start!").color(NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent event) {
        if (DodgeBallPlugin.get().isGameRunning()) return;

        if (event instanceof EntityDamageByEntityEvent event2) {
            if (event2.getDamager() instanceof Player player) {
                if (player.getGameMode() == GameMode.CREATIVE) return;
            }
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        DodgeBallPlugin plugin = DodgeBallPlugin.get();
        if (plugin.isGameRunning()) return;

        Player player = event.getPlayer();
        DBPlayer dbPlayer = DBPlayer.get(player);
        BoundingBox boundingBox = this.lobby.getBoundingBox();

        Location location = player.getLocation();
        if (boundingBox.contains(location.toVector()) && location.getBlock().getRelative(BlockFace.DOWN).isSolid()) {
            dbPlayer.setLastSafeLocation(location.getBlock().getLocation().add(0.5, 0, 0.5));
        }

        if (dbPlayer.getLastShove() + 10 > MinecraftServer.currentTick) {
            return;
        }

        Vector loc = location.toVector();

        if (!boundingBox.contains(loc)) {
            Location to = dbPlayer.getLastSafeLocation();

            if (to == null) {
                to = boundingBox.getCenter().toLocation(player.getWorld());
            }

            new ReturnPlayerTask(player, to).runTaskTimer(plugin, 1, 1);
        }
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (event.getDismounted().hasMetadata("returnStand")) event.setCancelled(true);
    }
}
