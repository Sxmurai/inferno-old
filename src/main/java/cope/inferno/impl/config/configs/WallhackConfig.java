package cope.inferno.impl.config.configs;

import com.google.common.collect.Lists;
import cope.inferno.impl.config.Config;
import cope.inferno.impl.features.module.modules.render.Wallhack;
import cope.inferno.impl.manager.FileManager;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.json.JSONArray;

import java.util.ArrayList;

public class WallhackConfig extends Config {
    private final ArrayList<Block> defaultBlocks = Lists.newArrayList(
            Blocks.COAL_ORE,
            Blocks.IRON_ORE,
            Blocks.GOLD_ORE,
            Blocks.LAPIS_ORE,
            Blocks.REDSTONE_ORE,
            Blocks.DIAMOND_ORE,

            Blocks.COAL_BLOCK,
            Blocks.IRON_BLOCK,
            Blocks.GOLD_BLOCK,
            Blocks.LAPIS_BLOCK,
            Blocks.REDSTONE_BLOCK,
            Blocks.DIAMOND_BLOCK,

            Blocks.IRON_BARS,
            Blocks.REDSTONE_LAMP,
            Blocks.LIT_REDSTONE_LAMP,
            Blocks.FURNACE,
            Blocks.LIT_FURNACE,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST
    );

    public WallhackConfig() {
        super("wallhack_blocks", ".json");
    }

    @Override
    public void load() {
        String content = this.parse();
        if (content == null || content.isEmpty()) {
            this.save();
            return;
        }

        JSONArray array = new JSONArray(content);
        for (Object element : array) {
            if (!(element instanceof String)) {
                continue;
            }

            Block block = Block.getBlockFromName((String) element);
            if (block == null || block == Blocks.AIR) {
                continue;
            }

            Wallhack.blocks.add(block);
        }
    }

    @Override
    public void save() {
        JSONArray array = new JSONArray();
        this.defaultBlocks.forEach((block) -> array.put(block.getRegistryName()));
        FileManager.getInstance().write(this.getPath(), array.toString(4));
    }

    @Override
    public void reset() {
        Wallhack.blocks.clear();
        Wallhack.blocks.addAll(this.defaultBlocks);
    }
}
