package dev.jacobruby.util;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.util.Vector;

public class Hologram extends ArmorStand {
    private Vector offset = new Vector();
    private Entity entity;

    public Hologram(Entity entity, Component text) {
        super(EntityType.ARMOR_STAND, entity.level());
        this.entity = entity;

        this.setMarker(true);
        this.setInvulnerable(true);
        this.setInvisible(true);
        this.setNoGravity(true);

        this.setCustomName(PaperAdventure.asVanilla(text));
        this.setCustomNameVisible(true);
        this.level().addFreshEntity(this);
    }

    public Hologram offset(Vector offset) {
        this.offset = offset.clone();
        return this;
    }

    @Override
    public void tick() {
        if (this.entity.isRemoved()) {
            discard();
        }

        Vec3 pos = this.entity.position();
        this.setPos(pos.x + this.offset.getX(), pos.y + this.offset.getY(), pos.z + this.offset.getZ());

        super.tick();
    }
}
