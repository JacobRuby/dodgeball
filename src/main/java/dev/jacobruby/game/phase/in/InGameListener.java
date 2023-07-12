package dev.jacobruby.game.phase.in;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.game.DodgeBallGame;
import dev.jacobruby.game.ball.BallType;
import dev.jacobruby.game.ball.monster.MonsterEntity;
import dev.jacobruby.game.team.Team;
import dev.jacobruby.player.DBPlayer;
import dev.jacobruby.util.BlockModFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InGameListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        DBPlayer dbPlayer = DBPlayer.get(player);

        if (dbPlayer.isSpectator() || dbPlayer.getLastShove() + 10 > MinecraftServer.currentTick) {
            return;
        }

        Team team = dbPlayer.getTeam();

        if (team == null) return;
        BoundingBox boundingBox = team.getBoundingBox();
        Vector loc = player.getLocation().toVector();

        if (!boundingBox.contains(loc)) {

            if (loc.getY() < boundingBox.getMinY()) {
                dbPlayer.killedBy(null);
            } else {
                Vector velocity = boundingBox.getCenter().subtract(loc).normalize();
                velocity.setY(0.5);

                player.setVelocity(velocity);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 2.0f, 0.6f);

                dbPlayer.setLastShove(MinecraftServer.currentTick);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        event.setCancelled(true);
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        DBPlayer dbPlayer = DBPlayer.get(player);
        if (dbPlayer == null || !dbPlayer.isAlive()) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        if (item.getType() == Material.SNOW_BLOCK) {
            player.getInventory().setItemInMainHand(null);
            player.sendMessage(Component.text("Congratulations, you discovered the super easter egg and WIN THE GAME!").color(NamedTextColor.AQUA));
            Bukkit.getScheduler().runTaskLater(DodgeBallPlugin.get(), () -> {
                player.sendMessage(Component.text("Just kidding, but have a hut!").color(NamedTextColor.RED));

                spawnHut(player.getLocation().getBlock());
            }, 20 * 3);
        }

        BallType type = BallType.fromMaterial(item.getType());
        if (type == null) return;

        DodgeBallGame game = DodgeBallPlugin.get().getGame();
        int difference = game.getStartTick() + 10 * 20 - MinecraftServer.currentTick;
        if (difference > 0) {
            int seconds = difference / 20 + 1;
            player.sendMessage(Component.text("Wait " + seconds + " seconds to throw dodge balls!"));
            return;
        }

        type.launchProjectile(dbPlayer);
        player.getInventory().setItemInMainHand(null);
    }

    private void spawnHut(Block block) {
        List<Block> blocks = new ArrayList<>();
        for (int x = -2; x < 3; x++) {
            for (int y = -1; y < 4; y++) {
                for (int z = -2; z < 3; z++) {
                    blocks.add(block.getRelative(x, y, z));
                }
            }
        }

        for (int x = -1; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = -1; z < 2; z++) {
                    blocks.remove(block.getRelative(x, y, z));
                }
            }
        }

        blocks.remove(block.getRelative(-2, 0, 0));
        blocks.remove(block.getRelative(-2, 1, 0));

        blocks.forEach(new BlockModFunction(Material.SNOW_BLOCK));
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        DBPlayer dbPlayer = DBPlayer.get(event.getEntity());

        if (dbPlayer == null || !dbPlayer.isAlive()) return;

        if (((CraftEntity) event.getDamager()).getHandle() instanceof MonsterEntity monster) {
            DBPlayer summoner = monster.getSummoner();
            if (summoner.getTeam() == dbPlayer.getTeam()) return;

            dbPlayer.killedBy(summoner);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FIRE) return;

        DBPlayer dbPlayer = DBPlayer.get(event.getEntity());
        if (dbPlayer == null || !dbPlayer.isAlive()) return;

        DodgeBallGame game = DodgeBallPlugin.get().getGame();
        if (game == null) return;

        DBPlayer summoner = null;
        Location location = dbPlayer.getPlayer().getLocation();
        Map<Block, DBPlayer> fireBlocks = game.getFireBlocks();
        for (Block block : fireBlocks.keySet()) {
            if (block.getLocation().add(0.5, 0.5, 0.5).distanceSquared(location) <= 4) {
                summoner = fireBlocks.get(block);
                break;
            }
        }

        if (summoner == null || dbPlayer.getTeam() == summoner.getTeam()) {
            return;
        }

        event.setCancelled(false);
        event.setDamage(0);
        dbPlayer.killedBy(summoner);
    }
}
