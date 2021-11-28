package cope.inferno.impl.features.module.modules.render;

import com.google.common.collect.Lists;
import cope.inferno.impl.event.inferno.OptionChangeEvent;
import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

@Module.Define(name = "Wallhack", category = Module.Category.Render)
@Module.Info(description = "Allows you to see blocks through walls")
public class Wallhack extends Module {
    public static Wallhack INSTANCE;
    public static ArrayList<Block> blocks = new ArrayList<>();

    public static final Setting<Integer> opacity = new Setting<>("Opacity", 120, 0, 255);
    public static final Setting<Integer> light = new Setting<>("Light", 100, 0, 100);
    public final Setting<Reload> reload = new Setting<>("Reload", Reload.Soft);

    private boolean needsReload = false;

    public Wallhack() {
        INSTANCE = this;

        // @todo, this will be loaded dynamically using configs. for now this will do
        blocks = Lists.newArrayList(
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
    }

    @Override
    protected void onActivated() {
        ForgeModContainer.forgeLightPipelineEnabled = false;

        if (fullNullCheck()) {
            this.reload();
        } else {
            this.needsReload = true;
        }
    }

    @Override
    protected void onDeactivated() {
        this.needsReload = false;
        this.reload();
        Wrapper.mc.renderChunksMany = false;
        ForgeModContainer.forgeLightPipelineEnabled = true;
    }

    @Override
    public void onUpdate() {
        if (this.needsReload) {
            this.needsReload = false;
            this.reload();
        }
    }

    @SubscribeEvent
    public void onOptionChange(OptionChangeEvent event) {
        if (this.getSettings().stream().anyMatch((e) -> event.getOption().equals(e))) {
            this.reload();
        }
    }

    private void reload() {
        Wrapper.mc.renderChunksMany = true;

        if (this.reload.getValue() == Reload.All) {
            Wrapper.mc.renderGlobal.loadRenderers();
        } else if (this.reload.getValue() == Reload.Soft) {
            Vec3d pos = Wrapper.mc.player.getPositionVector();
            int dist = Wrapper.mc.gameSettings.renderDistanceChunks * 16;
            Wrapper.mc.renderGlobal.markBlockRangeForRenderUpdate((int) (pos.x) - dist, (int) (pos.y) - dist, (int) (pos.z) - dist, (int) (pos.x) + dist, (int) (pos.y) + dist, (int) (pos.z) + dist);
        }
    }

    public enum Reload {
        Soft, All
    }
}
