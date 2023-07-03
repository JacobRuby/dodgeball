package dev.jacobruby.lobby;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import com.google.common.collect.Lists;
import dev.jacobruby.util.ProtocolUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class LobbyNPC extends PathfinderMob {
    private PacketContainer addPlayerPacket, removePlayerPacket, nameEntityPacket, metadataPacket, destroyPacket;
    private String skinSignature, skinValue;
    private String name;

    private EntityType<?> fakeType;

    protected LobbyNPC(Location location, String name) {
        super(EntityType.ZOMBIE, ((CraftWorld) location.getWorld()).getHandle());
        this.name = name;
        this.setCustomName(Component.literal(name));

        this.setPos(location.x(), location.y(), location.z());
        this.setRot(location.getYaw(), location.getPitch());

        this.initializePlayerPackets();

        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SNOWBALL));

        this.level().addFreshEntity(this);

        Vec3 position = this.position();
        ArmorStand hologram = new ArmorStand(this.level(), position.x, position.y + this.getBbHeight() + 0.25, position.z);
        hologram.setInvisible(true);
        hologram.setMarker(true);
        hologram.setInvulnerable(true);
        hologram.setCustomName(Component.literal("Click to start!").withStyle(ChatFormatting.GREEN));

        this.level().addFreshEntity(hologram);

        this.setPersistenceRequired();
    }

    public void setFakeType(EntityType<?> fakeType) {
        this.fakeType = fakeType;

        if (fakeType.equals(EntityType.PLAYER)) {
            initializePlayerPackets();
        }
    }

    @Override
    public void checkDespawn() {
        super.checkDespawn();
    }

    @Override
    public EntityType<?> getType() {
        return this.fakeType;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 16.0F, 1.0f));
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }

    @Override
    public void move(MoverType movementType, Vec3 movement) {
    }

    @Override
    public void tick() {
        super.tick();
        this.setYRot(this.yHeadRot);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        if (this.fakeType == EntityType.PLAYER) {
            updateNamedEntityPacket();

            CraftPlayer bukkit = player.getBukkitEntity();

            ProtocolUtil.sendPacket(bukkit, this.addPlayerPacket, this.nameEntityPacket, this.metadataPacket);

//            Bukkit.getScheduler().runTaskLaterAsynchronously(DodgeBallPlugin.get(), () -> {
//                ProtocolUtil.sendPacket(bukkit, this.removePlayerPacket);
//            }, 2);
        }

        super.startSeenByPlayer(player);
    }

    public void setSkin(String signature, String value) {
        if (this.fakeType != EntityType.PLAYER) {
            throw new IllegalStateException("LobbyNPC must be set to player to use skin");
        }

        this.skinSignature = signature;
        this.skinValue = value;
        this.updatePlayerInfoPackets();
    }

    // A lot of this junk is yoinked
    private void initializePlayerPackets() {
        this.updatePlayerInfoPackets();
        this.updateNamedEntityPacket();

        final WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        // Hide internal name
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), false);
        // Enable skin layers
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(17, WrappedDataWatcher.Registry.get(Byte.class)), (byte)127);

        this.metadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        this.metadataPacket.getIntegers().write(0, this.getId());

        List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
        for (WrappedWatchableObject object : dataWatcher.getWatchableObjects()) {
            wrappedDataValueList.add(new WrappedDataValue(object.getIndex(), object.getWatcherObject().getSerializer(), object.getRawValue()));
        }

        this.metadataPacket.getDataValueCollectionModifier().write(0, wrappedDataValueList);

        this.destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        this.destroyPacket.getIntLists().write(0, List.of(this.getId()));
    }

    private void updateNamedEntityPacket() {
        final byte yaw = (byte)((int)(this.getYRot() * 256.0F / 360.0F));
        final byte pitch = (byte)((int)(this.getXRot() * 256.0F / 360.0F));

        // Spawn the named entity
        this.nameEntityPacket = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        this.nameEntityPacket.getIntegers().write(0, this.getId());
        this.nameEntityPacket.getUUIDs().write(0, this.uuid);
        Vec3 pos = position();
        this.nameEntityPacket.getDoubles().write(0, pos.x);
        this.nameEntityPacket.getDoubles().write(1, pos.y);
        this.nameEntityPacket.getDoubles().write(2, pos.z);
        this.nameEntityPacket.getBytes().write(0, yaw);
        this.nameEntityPacket.getBytes().write(1, pitch);
    }

    private void updatePlayerInfoPackets() {
        this.addPlayerPacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        this.addPlayerPacket.getPlayerInfoActions().write(0, EnumSet.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER));
        List<PlayerInfoData> infoDataList = new ArrayList<>();

        WrappedGameProfile profile = new WrappedGameProfile(this.uuid, this.name);
        if (this.skinSignature != null && this.skinValue != null) {
            WrappedSignedProperty property = new WrappedSignedProperty("textures", this.skinValue, this.skinSignature);
            profile.getProperties().put("textures", property);
        }

        WrappedChatComponent displayName = WrappedChatComponent.fromText(this.name);
        infoDataList.add(new PlayerInfoData(profile, 0, EnumWrappers.NativeGameMode.SURVIVAL, displayName));
        this.addPlayerPacket.getPlayerInfoDataLists().write(1, infoDataList);
    }
}
