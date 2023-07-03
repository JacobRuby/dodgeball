package dev.jacobruby.game.ball.vacuum;

import dev.jacobruby.game.ball.monster.MonsterEntity;
import dev.jacobruby.player.DBPlayer;
import dev.jacobruby.util.TimedRunnable;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Vacuum extends TimedRunnable {
    private static double RANGE = 5.0;
    private DBPlayer summoner;
    private Location location;
    private Display.ItemDisplay display;

    public Vacuum(DBPlayer summoner, Location location) {
        this.summoner = summoner;
        this.location = location;

        this.display = EntityType.ITEM_DISPLAY.create(((CraftWorld) location.getWorld()).getHandle());
        this.display.setItemStack(CraftItemStack.asNMSCopy(new ItemStack(Material.HOPPER)));
        this.display.setPos(location.x(), location.y() + 0.5, location.z());
        this.display.persist = false;
        this.display.setBillboardConstraints(Display.BillboardConstraints.VERTICAL);
        this.display.level().addFreshEntity(this.display);
    }

    @Override
    protected void run(int tick) {
        World world = this.location.getWorld();
        world.playSound(this.location, Sound.ENTITY_ENDERMAN_AMBIENT, 3.0f, 0.6f);
        world.spawnParticle(Particle.REDSTONE, this.location, 4, 1.5, 1.5, 1.5, new Particle.DustOptions(Color.BLACK, 2.0f));

        for (Entity entity : location.getNearbyEntities(RANGE, RANGE, RANGE)) {
            net.minecraft.world.entity.Entity handle = ((CraftEntity) entity).getHandle();
            if (handle == this.display) continue;

            DBPlayer dbPlayer = null;
            if (entity instanceof Player player) {
                dbPlayer = DBPlayer.get(player);
                if (!dbPlayer.isAlive() || dbPlayer.getTeam() == this.summoner.getTeam()) continue;
            }

            Location entityLoc = entity.getLocation();
            double distance = entityLoc.distance(this.location); // Need linear scaling so use sqrt

            if (distance < 0.3) {
                if (handle instanceof MonsterEntity monster) {
                    monster.discard();
                }

                if (dbPlayer != null) {
                    dbPlayer.killedBy(this.summoner);
                }
                continue;
            }

            double strength = RANGE - distance;
            Vector difference = this.location.clone().subtract(entityLoc.toVector()).toVector();

            world.spawnParticle(Particle.SMOKE_NORMAL, entityLoc, 0, difference.getX(), difference.getY(), difference.getZ());

            if (strength <= 0.0) continue;

            Vector pull = difference.clone().normalize().multiply(strength * 0.15);
            entity.setVelocity(pull);
        }

        if (tick >= 50) {
            cancel();
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        this.display.discard();
        super.cancel();
    }
}
