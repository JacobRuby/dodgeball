package dev.jacobruby.game.ball.monster;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.game.ball.Ball;
import dev.jacobruby.player.DBPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MonsterEntity extends Zombie {
    private static List<EntityType<?>> TYPES = List.of(
        EntityType.ZOMBIE,
        EntityType.SKELETON,
        EntityType.ZOMBIFIED_PIGLIN,
        EntityType.STRAY,
        EntityType.HUSK,
        EntityType.BEE,
        EntityType.CAMEL,
        EntityType.WARDEN
    );
    private DBPlayer summoner;

    private EntityType<?> fakeType;

    public MonsterEntity(Ball spawner) {
        super(spawner.level());
        this.summoner = spawner.getThrower();

        this.fakeType = TYPES.get(ThreadLocalRandom.current().nextInt(TYPES.size()));

        Location location = spawner.getBukkitEntity().getLocation();
        setPos(location.x(), location.y() + 1, location.z());

        this.setInvulnerable(true);

        this.level().addFreshEntity(this);

        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
        meta.setColor(spawner.getThrower().getTeam().getDyeColor().getColor());
        helmet.setItemMeta(meta);
        this.setItemSlot(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(helmet));
    }

    @Override
    public EntityType<?> getType() {
        return this.fakeType;
    }

    @Override
    public double getMeleeAttackRangeSqr(LivingEntity target) {
        return super.getMeleeAttackRangeSqr(target);
    }

    @Override
    public void tick() {
        if (!(this.getTarget() instanceof Player nms)) {
            setTarget(null);
        } else {
            DBPlayer dbPlayer = DBPlayer.get(nms);

            if (!dbPlayer.isAlive() || dbPlayer.getTeam() == this.summoner.getTeam()) {
                setTarget(null);
            }
        }

        if (DodgeBallPlugin.get().getGame() == null) {
            discard();
            return;
        }

        super.tick();
    }

    public DBPlayer getSummoner() {
        return summoner;
    }
}
