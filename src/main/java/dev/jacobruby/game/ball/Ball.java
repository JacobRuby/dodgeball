package dev.jacobruby.game.ball;

import com.mojang.math.Transformation;
import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.game.DodgeBallGame;
import dev.jacobruby.player.DBPlayer;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;

public class Ball extends Snowball {
    private final DBPlayer thrower;
    private final BallType type;

    private final ArmorStand ride;
    private final Display.ItemDisplay display;

    public Ball(DBPlayer thrower, BallType type) {
        this(thrower, type, null);
    }

    public Ball(DBPlayer thrower, BallType type, Vector location) {
        super(thrower.getHandle().level(), thrower.getHandle());
        this.thrower = thrower;
        this.type = type;

        if (location != null) {
            this.setPos(location.getX(), location.getY(), location.getZ());
        }

        this.ride = EntityType.ARMOR_STAND.create(this.level());
        this.updateRidePosition();
        this.ride.setInvisible(true);
        this.ride.setMarker(true);
        this.setInvulnerable(true);

        this.display = EntityType.ITEM_DISPLAY.create(this.level());
        this.display.setItemStack(CraftItemStack.asNMSCopy(new ItemStack(type.getMaterial())));

        Matrix4f matrix = new Matrix4f();
        matrix.scale(0.75f);
        this.display.setTransformation(new Transformation(matrix));

        this.display.setBillboardConstraints(Display.BillboardConstraints.CENTER);
        this.display.setPos(this.position());
        this.display.persist = false;

        this.level().addFreshEntity(this);
        this.level().addFreshEntity(this.ride);
        this.level().addFreshEntity(this.display);
        this.display.startRiding(this.ride);
    }

    public DBPlayer getThrower() {
        return thrower;
    }

    @Override
    public boolean broadcastToPlayer(ServerPlayer spectator) {
        return false;
    }

    @Override
    public void tick() {
        this.checkPosition();

        if (isRemoved()) return;

        this.updateRidePosition();
        super.tick();
    }

    private void checkPosition() {
        DodgeBallPlugin plugin = DodgeBallPlugin.get();
        BoundingBox arenaBoundingBox = plugin.getArenaManager().getArenaBoundingBox();
        DodgeBallGame game = plugin.getGame();

        if (game == null) {
            discard();
            return;
        }

        Vec3 pos = this.position();
        if (!arenaBoundingBox.contains(pos.x, pos.y, pos.z)) {
            discard();
        }
    }

    private void updateRidePosition() {
        this.ride.setPos(this.position().add(0, -.2, 0));
    }

    @Override
    public void remove(RemovalReason reason) {
        this.display.remove(reason);
        this.ride.remove(reason);
        super.remove(reason);
    }

    @Override
    public boolean canCollideWith(Entity other) {
        DBPlayer dbPlayer = DBPlayer.get(other);
        if (dbPlayer != null) {
            return dbPlayer.isAlive();
        }

        return super.canCollideWith(other);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);

        if (this.type.respawnsOnHit()) {
            this.type.dropItem(this.getBukkitEntity().getLocation());
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        this.type.onHitEntity(this, entityHitResult);
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        this.type.onHitBlock(this, blockHitResult);
    }
}
