package dev.jacobruby.game.ball;

import com.mojang.math.Transformation;
import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.game.DodgeBallGame;
import dev.jacobruby.game.team.Team;
import dev.jacobruby.util.Hologram;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;

public class BallItem extends ItemEntity {
    private final BallType type;
    private final Display.ItemDisplay display;
    private final Hologram hologram;

    private int spawnedSlot = -1;

    private Team lastLocationTeam;

    public BallItem(BallType type, Location location) {
        super(((CraftWorld) location.getWorld()).getHandle(), location.x(), location.y(), location.z(), CraftItemStack.asNMSCopy(type.getItemStack()), 0, 0.5f, 0);
        this.type = type;

        this.display = EntityType.ITEM_DISPLAY.create(this.level());
        this.display.setItemStack(this.getItem());
        this.display.persist = false;
        this.display.setBillboardConstraints(Display.BillboardConstraints.VERTICAL);

        this.updateDisplay();

        this.hologram = new Hologram(this, Component.text(type.getName()).color(TextColor.color(type.getColor().asRGB()))).offset(new Vector(0, 2.3, 0));

        this.level().addFreshEntity(this);
        this.level().addFreshEntity(this.display);
    }

    public void setSpawnedSlot(int spawnedSlot) {
        this.spawnedSlot = spawnedSlot;
    }

    public int getSpawnedSlot() {
        return spawnedSlot;
    }

    @Override
    public boolean broadcastToPlayer(ServerPlayer spectator) {
        return false;
    }

    @Override
    public void tick() {
        this.checkPosition();

        if (isRemoved()) return;

        super.tick();
        this.updateDisplay();
        this.doParticles();
    }

    private void updateDisplay() {
        Matrix4f matrix = new Matrix4f();
        matrix.scale(1.5f);

        double y = Math.sin(Math.PI * 2 * tickCount / 40.0) * 0.2;
        matrix.translate(0, (float) y + 1.0f, 0);

        this.display.setInterpolationDelay(0);
        this.display.setInterpolationDuration(1);
        this.display.setTransformation(new Transformation(matrix));

        this.display.setPos(this.position());
    }

    private void doParticles() {
        World world = this.level().getWorld();
        Vec3 pos = this.position();
        Color color = this.type.getColor();
        world.spawnParticle(Particle.DUST_COLOR_TRANSITION, pos.x, pos.y, pos.z, 2, 0.05, 0.05, 0.05, new Particle.DustTransition(Color.WHITE.mixColors(color), color, 2.0f));
    }

    private void checkPosition() {
        Vec3 pos = this.position();
        Vector vector = new Vector(pos.x, pos.y, pos.z);

        DodgeBallPlugin plugin = DodgeBallPlugin.get();
        DodgeBallGame game = plugin.getGame();

        if (game == null) {
            discard();
            return;
        }

        for (Team team : game.getTeams()) {
            if (team.getBoundingBox().contains(vector)) {
                this.lastLocationTeam = team;
                System.out.println("Found team: " + (team == null ? "null" : team.getId()));
                break;
            }
        }

        BoundingBox arenaBoundingBox = game.getBoundingBox();

        if (!arenaBoundingBox.contains(vector)) {
            System.out.println("team: " + this.lastLocationTeam);
            if (this.lastLocationTeam != null) {
                Location loc = this.lastLocationTeam.getItemRespawnLocation();
                System.out.println("teamLoc: " + loc);
                this.setPos(loc.x(), loc.y(), loc.z());
            } else {
                discard();
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        this.display.remove(reason);
        this.hologram.remove(reason);
        super.remove(reason);

        if (this.spawnedSlot != -1) {
            DodgeBallPlugin.get().getGame().removeItem(this.spawnedSlot);
        }
    }
}
