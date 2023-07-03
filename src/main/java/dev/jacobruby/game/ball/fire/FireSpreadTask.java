package dev.jacobruby.game.ball.fire;

import dev.jacobruby.DodgeBallPlugin;
import dev.jacobruby.game.DodgeBallGame;
import dev.jacobruby.game.ball.BallType;
import dev.jacobruby.player.DBPlayer;
import dev.jacobruby.util.BlockModFunction;
import dev.jacobruby.util.TimedRunnable;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class FireSpreadTask extends TimedRunnable {
    private DBPlayer thrower;
    private Block center;

    private List<Block> blocks;
    private List<Block> seeds;

    private BlockModFunction fireFunction = new BlockModFunction(Material.FIRE);

    public FireSpreadTask(DBPlayer thrower, Block center) {
        this.thrower = thrower;
        this.center = center;
        this.blocks = new ArrayList<>();
        this.seeds = new ArrayList<>();
        spread(this.center);
        this.seeds.add(center);
    }

    @Override
    protected void run(int tick) {
        DodgeBallGame game = DodgeBallPlugin.get().getGame();
        if (game == null || this.seeds.isEmpty()) {
            cancel();
            return;
        }

        List<Block> newSeeds = new ArrayList<>();

        for (Block block : this.seeds) {
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    if (Math.random() > 0.2) continue;

                    Block relative = block.getRelative(x, 0, z);
                    if (this.blocks.contains(relative)) continue;

                    if (!relative.getType().isAir() || relative.getRelative(BlockFace.DOWN).getType().isAir()) continue;

                    newSeeds.add(relative);
                    spread(relative);
                }
            }
        }

        this.seeds.clear();
        this.seeds.addAll(newSeeds);

        if (tick >= 7) {
            cancel();
        }
    }

    private void spread(Block block) {
        DodgeBallGame game = DodgeBallPlugin.get().getGame();

        this.fireFunction.accept(block);
        this.blocks.add(block);

        game.getFireBlocks().put(block, this.thrower);
        World world = block.getWorld();
        world.playSound(block.getLocation(), Sound.ITEM_FIRECHARGE_USE, 3.0f, 0.7f);
        world.spawnParticle(Particle.REDSTONE, block.getLocation().add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5, new Particle.DustOptions(BallType.FIRE_BALL.getColor(), 2.0f));
    }
}
