package dev.jacobruby.util;

import dev.jacobruby.DodgeBallPlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;

import java.util.function.Consumer;

public class BlockModFunction implements Consumer<Block> {
    private Material material;
    private BoundingBox boundingBox;

    public BlockModFunction(Material material) {
        this.material = material;
        this.boundingBox = DodgeBallPlugin.get().getArenaManager().getArenaBoundingBox();
    }

    @Override
    public void accept(Block block) {
        if (this.boundingBox.contains(block.getLocation().toVector())) {
            block.setType(this.material);
        }
    }
}
