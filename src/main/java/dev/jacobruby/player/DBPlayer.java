package dev.jacobruby.player;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.game.ball.Ball;
import dev.jacobruby.game.team.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class DBPlayer {
    private WeakReference<Player> player;
    private UUID uuid;
    private String name;

    @Nullable
    private Team team;
    private boolean alive;
    private boolean spectator;

    private int kills;

    private int lastShove;
    private Location lastSafeLocation;

    public DBPlayer(Player player) {
        this.setPlayer(player);
    }

    public void setPlayer(Player player) {
        this.player = new WeakReference<>(player);
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    @Nullable
    public Player getPlayer() {
        return this.player.get();
    }

    @Nullable
    public net.minecraft.world.entity.player.Player getHandle() {
        Player player = this.player.get();

        return player == null ? null : ((CraftPlayer) player).getHandle();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setTeam(@Nullable Team team) {
        this.team = team;
    }

    @Nullable
    public Team getTeam() {
        return team;
    }

    public int getKills() {
        return kills;
    }

    public TextColor getTeamColor() {
        return this.team.getTextColor();
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;

        Player player = this.player.get();
        if (spectator) {
            player.sendMessage(Component.text("You are now spectating.").color(NamedTextColor.GRAY));
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            player.sendMessage(Component.text("You are no longer spectating.").color(NamedTextColor.GRAY));
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void clearInventory() {
        Player player = this.player.get();
        Inventory inventory = ((CraftInventoryPlayer) player.getInventory()).getInventory();
        inventory.clearContent();

        ServerPlayer handle = ((CraftPlayer) player).getHandle();
        handle.inventoryMenu.clearCraftingContent();
        player.setItemOnCursor(null);
    }

    public void killedBy(DBPlayer killer) {
        if (!this.isAlive()) throw new IllegalStateException("Player is not alive");

        this.setAlive(false);
        this.setSpectator(true);
        this.clearInventory();

        if (killer == null) {
            // this is a painfully long line
            boolean voidDeath = this.getPlayer().getLocation().getY() < DodgeBallPlugin.get().getGame().getBoundingBox().getMinY();

            Component message = Component.empty()
                .append(Component.text("KILL! ").decorate(TextDecoration.BOLD).color(NamedTextColor.RED))
                .append(Component.text(this.getName()).color(this.getTeamColor()))
                .append(Component.text(voidDeath ? " got sent to the shadow realm!" : " disconnected!").color(NamedTextColor.GRAY));

            for (DBPlayer dbPlayer : DodgeBallPlugin.get().getPlayerManager().getPlayers()) {
                dbPlayer.getPlayer().sendMessage(message);
            }
        } else {
            killer.onKill(this);
        }

        Player player = this.player.get();
        World world = player.getWorld();
        world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 2.0f, 1.0f);

        Location pos = player.getLocation();
        Color color = this.getTeam().getDyeColor().getColor();
        world.spawnParticle(Particle.DUST_COLOR_TRANSITION, pos.x(), pos.y() + 1, pos.z(), 20, 0.3, 0.5, 0.3, new Particle.DustTransition(color, Color.BLACK.mixColors(color), 0.7f));
    }

    private void onKill(DBPlayer player) {
        this.kills++;
        player.getPlayer().sendMessage(Component.empty()
                .append(Component.text("DEATH! ").decorate(TextDecoration.BOLD).color(NamedTextColor.RED))
                .append(Component.text("You were killed by ").color(NamedTextColor.GRAY))
                .append(Component.text(this.getName()).color(this.getTeamColor()))
                .append(Component.text("!").color(NamedTextColor.GRAY)));

        this.player.get().sendMessage(Component.empty()
                .append(Component.text("KILL! ").decorate(TextDecoration.BOLD).color(NamedTextColor.RED))
                .append(Component.text("You killed ").color(NamedTextColor.GRAY))
                .append(Component.text(player.getName()).color(player.getTeamColor()))
                .append(Component.text("!").color(NamedTextColor.GRAY)));

        Component message = Component.empty()
            .append(Component.text("KILL! ").decorate(TextDecoration.BOLD).color(NamedTextColor.RED))
            .append(Component.text(player.getName()).color(player.getTeamColor()))
            .append(Component.text(" was killed by ").color(NamedTextColor.GRAY))
            .append(Component.text(this.getName()).color(this.getTeamColor()))
            .append(Component.text("!").color(NamedTextColor.GRAY));

        for (DBPlayer dbPlayer : DodgeBallPlugin.get().getPlayerManager().getPlayers()) {
            if (dbPlayer == this || dbPlayer == player) continue;
            dbPlayer.getPlayer().sendMessage(message);
        }

        DodgeBallPlugin.get().getGame().updateAliveLines();
    }

    public int getLastShove() {
        return lastShove;
    }

    public void setLastShove(int lastShove) {
        this.lastShove = lastShove;
    }

    public Location getLastSafeLocation() {
        return lastSafeLocation.clone();
    }

    public void setLastSafeLocation(Location lastSafeLocation) {
        this.lastSafeLocation = lastSafeLocation;
    }

    @Nullable
    public static DBPlayer get(net.minecraft.world.entity.Entity nms) {
        return get(nms.getBukkitEntity());
    }

    @Nullable
    public static DBPlayer get(Entity bukkit) {
        if (bukkit instanceof Player player) return get(player);
        return null;
    }

    @Nullable
    public static DBPlayer get(Player bukkit) {
        return DodgeBallPlugin.get().getPlayerManager().getPlayer(bukkit);
    }
}
