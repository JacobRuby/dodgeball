package dev.jacobruby.game.ball;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.game.DodgeBallGame;
import dev.jacobruby.game.ball.fire.FireSpreadTask;
import dev.jacobruby.game.ball.monster.MonsterEntity;
import dev.jacobruby.game.ball.rain.Rain;
import dev.jacobruby.game.ball.speed.EffectCloud;
import dev.jacobruby.game.ball.vacuum.Vacuum;
import dev.jacobruby.game.team.Team;
import dev.jacobruby.player.DBPlayer;
import dev.jacobruby.util.BlockModFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public enum BallType {
    /*

    If I had more time, this would be the first thing to be restructured XD.
    I started with a simple enum and it kept growing. I'd definitely switch to a registry type deal so I could
    actually use some polymorphism here. A lot of the balls just run a task at the location of the ball, that
    could be abstracted.

     */
    SNOW_BALL("Snowball", Material.SNOWBALL, Color.WHITE),
    FIRE_BALL("Fireball", Material.FIRE_CHARGE, Color.ORANGE) {
        @Override
        public void onHitBlock(Ball ball, BlockHitResult hitResult) {
            DodgeBallGame game = DodgeBallPlugin.get().getGame();

            if (game == null) return;

            BlockPos relative = hitResult.getBlockPos().relative(hitResult.getDirection());
            Block block = ball.getBukkitEntity().getWorld().getBlockAt(relative.getX(), relative.getY(), relative.getZ());

            new FireSpreadTask(ball.getThrower(), block).runTaskTimer(DodgeBallPlugin.get(), 10, 10);
        }
    },
    MONSTER_BALL("Monster Ball", Material.ZOMBIE_SPAWN_EGG, Color.GREEN) {
        @Override
        public void onHitBlock(Ball ball, BlockHitResult hitResult) {
            summon(ball);
        }

        @Override
        public void onHitEntity(Ball ball, EntityHitResult hitResult) {
            super.onHitEntity(ball, hitResult);
            summon(ball);
        }

        private void summon(Ball ball) {
            new MonsterEntity(ball);
            Entity entity = ball.getBukkitEntity();
            World world = entity.getWorld();
            world.playSound(entity.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 3.0f, 1.7f);
            world.spawnParticle(Particle.DUST_COLOR_TRANSITION, entity.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, new Particle.DustTransition(Color.BLACK, color, 2.0f));
        }
    },
    VOID_BALL("Void Ball", Material.ENDER_PEARL, Color.PURPLE) {
        @Override
        public void onHitBlock(Ball ball, BlockHitResult hitResult) {
            BlockPos pos = hitResult.getBlockPos();
            Block block = ball.getBukkitEntity().getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());

            List<Block> blocks = new ArrayList<>();
            for (int x = -2; x < 3; x++) {
                for (int y = -2; y < 3; y++) {
                    for (int z = -2; z < 3; z++) {
                        blocks.add(block.getRelative(x, y, z));
                    }
                }
            }

            blocks.forEach(new BlockModFunction(Material.AIR));

            World world = block.getWorld();
            world.playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 1.7f);
            world.spawnParticle(Particle.DUST_COLOR_TRANSITION, block.getLocation().add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5, new Particle.DustTransition(color, Color.BLACK, 2.0f));
        }
    },
    WALL_BALL("Wall Ball", Material.BRICKS, Color.RED.mixColors(Color.ORANGE)) {
        @Override
        public void onHitBlock(Ball ball, BlockHitResult hitResult) {
            DodgeBallGame game = DodgeBallPlugin.get().getGame();

            if (game == null) return;

            BlockPos relative = hitResult.getBlockPos().relative(hitResult.getDirection());
            Block block = ball.getBukkitEntity().getWorld().getBlockAt(relative.getX(), relative.getY(), relative.getZ());

            summonWall(block);
        }

        @Override
        public void onHitEntity(Ball ball, EntityHitResult hitResult) {
            super.onHitEntity(ball, hitResult);

            DodgeBallGame game = DodgeBallPlugin.get().getGame();

            if (game == null) return;

            BlockPos relative = hitResult.getEntity().blockPosition();
            Block block = ball.getBukkitEntity().getWorld().getBlockAt(relative.getX(), relative.getY(), relative.getZ());

            for (int i = 0; i < 10; i++) {
                Block down = block.getRelative(BlockFace.DOWN);
                if (down.getType().isSolid()) break;

                block = down;
            }

            summonWall(block);
        }

        private void summonWall(Block block) {
            List<Block> blocks = new ArrayList<>();
            for (int x = -1; x < 2; x++) {
                for (int y = 0; y < 3; y++) {
                    blocks.add(block.getRelative(x, y, 0)); // This is obviously hard coded for this arena.
                }
            }

            blocks.forEach(new BlockModFunction(Material.BRICKS));

            World world = block.getWorld();
            Location location = block.getLocation().add(0.5, 1.5, 0.5);
            world.playSound(location, Sound.BLOCK_ANVIL_PLACE, 3.0f, 1.7f);
            world.spawnParticle(Particle.DUST_COLOR_TRANSITION, location, 60, 1.5, 0.5, 0.5, new Particle.DustTransition(Color.BLACK, color, 3.0f));
        }
    },
    FAST_BALL("Fast Ball", Material.AMETHYST_SHARD, Color.FUCHSIA, 1.8f, 0.1f),
    EXPLOSIVE_BALL("Explosive Ball", Material.TNT, Color.GRAY, 0.8f, 6.0f) {
        @Override
        public void onHitEntity(Ball ball, EntityHitResult hitResult) {
            super.onHitEntity(ball, hitResult);
            explode(ball.getThrower(), ball.getBukkitEntity().getLocation());
        }

        @Override
        public void onHitBlock(Ball ball, BlockHitResult hitResult) {
            explode(ball.getThrower(), ball.getBukkitEntity().getLocation());
        }

        private void explode(DBPlayer thrower, Location location) {
            for (int i = 0; i < 6; i++) {
                double angle = (i / (double) 6) * Math.PI * 2;

                Vector spawn = location.toVector();
                Ball ball = new Ball(thrower, EXPLOSIVE_SUB, spawn);

                Vector direction = new Vector(Math.cos(angle), Math.random(), Math.sin(angle));
                ball.shoot(direction.getX(), direction.getY(), direction.getZ(), this.speed, this.divergence);
            }

            location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 4.0f, 1.2f);
        }
    },
    EXPLOSIVE_SUB("Explosive Shard", Material.GUNPOWDER, Color.GRAY) {
        @Override
        public boolean spawnsNaturally() {
            return false;
        }
    },
    SOUL_BALL("Soul Ball", Material.SOUL_SAND, Color.fromRGB(0x964B00)) {
        @Override
        public void onHitBlock(Ball ball, BlockHitResult hitResult) {
            DodgeBallGame game = DodgeBallPlugin.get().getGame();

            if (game == null) return;

            BlockPos relative = hitResult.getBlockPos();
            Block block = ball.getBukkitEntity().getWorld().getBlockAt(relative.getX(), relative.getY(), relative.getZ());

            summonSoul(block);
        }

        @Override
        public void onHitEntity(Ball ball, EntityHitResult hitResult) {
            super.onHitEntity(ball, hitResult);

            DodgeBallGame game = DodgeBallPlugin.get().getGame();

            if (game == null) return;

            BlockPos relative = hitResult.getEntity().blockPosition();
            Block block = ball.getBukkitEntity().getWorld().getBlockAt(relative.getX(), relative.getY(), relative.getZ());

            for (int i = 0; i < 10; i++) {
                block = block.getRelative(BlockFace.DOWN);
                if (block.getType().isSolid()) break;
            }

            summonSoul(block);
        }

        private void summonSoul(Block block) {
            Location center = block.getLocation().add(0.5, 0.5, 0.5);
            List<Block> blocks = new ArrayList<>();
            for (int x = -3; x < 4; x++) {
                for (int z = -3; z < 4; z++) {
                    Block relative = block.getRelative(x, 0, z);
                    if (relative.getLocation().add(0.5, 0.5, 0.5).distanceSquared(center) > 9) continue;

                    blocks.add(relative);
                }
            }

            blocks.forEach(new BlockModFunction(Material.SOUL_SAND));

            World world = block.getWorld();
            world.playSound(center, Sound.BLOCK_SAND_BREAK, 3.0f, 1.7f);
            world.spawnParticle(Particle.DUST_COLOR_TRANSITION, center, 20, 2.5, 0.7, 2.5, new Particle.DustTransition(Color.BLACK, color, 2.0f));
        }
    },
    VACUUM_BALL("Vacuum Ball", Material.HOPPER, Color.GRAY.mixColors(Color.AQUA)) {
        @Override
        public void onHitEntity(Ball ball, EntityHitResult hitResult) {
            super.onHitEntity(ball, hitResult);
            summonVacuum(ball);
        }

        @Override
        public void onHitBlock(Ball ball, BlockHitResult hitResult) {
            summonVacuum(ball);
        }

        private void summonVacuum(Ball ball) {
            new Vacuum(ball.getThrower(), ball.getBukkitEntity().getLocation()).runTaskTimer(DodgeBallPlugin.get(), 0, 4);
        }
    },
    RAIN_BALL("Rain Ball", Material.WATER_BUCKET, Color.BLUE.mixColors(Color.AQUA)) {
        @Override
        public void onHitEntity(Ball ball, EntityHitResult hitResult) {
            super.onHitEntity(ball, hitResult);
            summonRain(ball);
        }

        @Override
        public void onHitBlock(Ball ball, BlockHitResult hitResult) {
            summonRain(ball);
        }

        private void summonRain(Ball ball) {
            new Rain(ball.getThrower(), ball.getBukkitEntity().getLocation()).runTaskTimer(DodgeBallPlugin.get(), 0, 4);
        }
    },
    RAIN_DROP("Rain Drop", Material.HEART_OF_THE_SEA, Color.BLUE.mixColors(Color.AQUA)) {
        @Override
        public boolean spawnsNaturally() {
            return false;
        }

        @Override
        public void onHitBlock(Ball ball, BlockHitResult hitResult) {
            DodgeBallGame game = DodgeBallPlugin.get().getGame();
            if (game == null) return;

            BlockPos relative = hitResult.getBlockPos().relative(hitResult.getDirection());
            Block block = ball.getBukkitEntity().getWorld().getBlockAt(relative.getX(), relative.getY(), relative.getZ());

            if (game.getFireBlocks().remove(block) != null) {
                block.setType(Material.AIR);
            }
        }
    },
    TRIPLE_SHOT("Triple Shot", Material.QUARTZ, Color.WHITE, 1.0f, 16.0f) {
        @Override
        public Ball launchProjectile(DBPlayer dbPlayer) {
            super.launchProjectile(dbPlayer);
            super.launchProjectile(dbPlayer);
            return super.launchProjectile(dbPlayer);
        }
    },
    SPEED_BALL("Speed Buff Ball", Material.SUGAR, PotionEffectType.SPEED.getColor(), 0.5f, 3.0f) {
        @Override
        public void onHitEntity(Ball ball, EntityHitResult hitResult) {
            super.onHitEntity(ball, hitResult);
            summonEffect(ball);
        }

        @Override
        public void onHitBlock(Ball ball, BlockHitResult hitResult) {
            summonEffect(ball);
        }

        private void summonEffect(Ball ball) {
            new EffectCloud(ball.getThrower(), ball.getBukkitEntity().getLocation(), new PotionEffect(PotionEffectType.SPEED, 20 * 5, 1), true)
                .runTaskTimer(DodgeBallPlugin.get(), 1, 1);
        }
    },
    SLOW_BALL("Slow Debuff Ball", Material.ANVIL, PotionEffectType.SLOW.getColor(), 0.5f, 3.0f) {
        @Override
        public void onHitEntity(Ball ball, EntityHitResult hitResult) {
            super.onHitEntity(ball, hitResult);
            summonEffect(ball);
        }

        @Override
        public void onHitBlock(Ball ball, BlockHitResult hitResult) {
            summonEffect(ball);
        }

        private void summonEffect(Ball ball) {
            new EffectCloud(ball.getThrower(), ball.getBukkitEntity().getLocation(), new PotionEffect(PotionEffectType.SLOW, 20 * 5, 1), false)
                .runTaskTimer(DodgeBallPlugin.get(), 1, 1);
        }
    }
    ;

    protected final String name;
    protected final Material material;
    protected final Color color;
    protected final float speed;
    protected final float divergence;

    BallType(String name, Material material, Color color) {
        this(name, material, color, 1.0f, 2.0f);
    }

    BallType(String name, Material material, Color color, float speed, float divergence) {
        this.name = name;
        this.material = material;
        this.color = color;
        this.speed = speed;
        this.divergence = divergence;
    }

    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }

    public Color getColor() {
        return color;
    }

    public boolean respawnsOnHit() {
        return this == SNOW_BALL;
    }

    public boolean spawnsNaturally() {
        return true;
    }

    public ItemStack getItemStack() {
        ItemStack itemStack = new ItemStack(this.material);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text(this.name).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(TextColor.color(this.color.asRGB())));
        itemMeta.addItemFlags(ItemFlag.values());
        itemStack.setItemMeta(itemMeta);

        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(itemStack);
        nms.getOrCreateTag().putUUID("uuid", UUID.randomUUID()); // Prevent stacking

        return CraftItemStack.asBukkitCopy(nms);
    }

    public BallItem dropItem(Location location) {
        BallItem item = new BallItem(this, location);
        return item;
    }

    public Ball launchProjectile(DBPlayer dbPlayer) {
        Ball ball = new Ball(dbPlayer, this);

        Player player = dbPlayer.getPlayer();
        Vector spawn = player.getEyeLocation().toVector();
        ball.setPos(spawn.getX(), spawn.getY(), spawn.getZ());

        Vector direction = player.getLocation().getDirection();
        ball.shoot(direction.getX(), direction.getY(), direction.getZ(), this.speed, this.divergence);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 2.0f, 1.5f);
        return ball;
    }

    public void onHitEntity(Ball ball, EntityHitResult hitResult) {
        Entity entity = hitResult.getEntity().getBukkitEntity();
        DBPlayer thrower = ball.getThrower();
        Team throwerTeam = thrower.getTeam();

        if (entity instanceof Player player) {
            DBPlayer dbPlayer = DBPlayer.get(player);

            if (dbPlayer.isSpectator()) {
                return;
            }

            Team otherTeam = dbPlayer.getTeam();

            if (throwerTeam == otherTeam) {
                return;
            }

            dbPlayer.killedBy(thrower);
        } else if (((CraftEntity) entity).getHandle() instanceof MonsterEntity monster) {
            Player player = thrower.getPlayer();
            if (monster.getSummoner().getTeam() == throwerTeam) {
                player.sendMessage(Component.text("This monster is on your team!"));
            } else {
                player.sendMessage(Component.text("Dodge balls can't hurt this monster! (But he's afraid of vacuums...)"));
            }
        }
    }

    public void onHitBlock(Ball ball, BlockHitResult hitResult) {
    }

    public static BallType fromMaterial(Material material) {
        for (BallType value : values()) {
            if (value.material == material) return value;
        }

        return null;
    }
}
