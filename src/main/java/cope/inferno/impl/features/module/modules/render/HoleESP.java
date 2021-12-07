package cope.inferno.impl.features.module.modules.render;

import cope.inferno.Inferno;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.manager.HoleManager;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.render.ColorUtil;
import cope.inferno.util.render.RenderUtil;
import cope.inferno.util.timing.Timer;
import cope.inferno.util.world.BlockUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Module.Define(name = "HoleESP", category = Module.Category.Render)
@Module.Info(description = "Shows where holes are in the ground")
public class HoleESP extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.OutlinedBox);
    public final Setting<Double> height = new Setting<>("Height", 1.0, -3.0, 3.0);
    public final Setting<Float> width = new Setting<>("Width", 3.0f, 0.1f, 5.0f);

    public final Setting<ColorUtil.Color> safeHole = new Setting<>("SafeHole", new ColorUtil.Color(0.0f, 255.0f, 0.0f, 80.0f));
    public final Setting<ColorUtil.Color> unsafeHole = new Setting<>("UnsafeHole", new ColorUtil.Color(255.0f, 0.0f, 0.0f, 80.0f));

    public final Setting<Boolean> voidHoles = new Setting<>("VoidHoles", true);
    public final Setting<ColorUtil.Color> voidHole = new Setting<>("VoidHole", new ColorUtil.Color(227, 218, 39, 80), this.voidHoles::getValue);

    private List<BlockPos> voidHolePositions = new ArrayList<>();
    private final Timer timer = new Timer();

    @Override
    protected void onDeactivated() {
        this.voidHolePositions.clear();
    }

    @Override
    public void onRenderWorld() {
        boolean filled = this.mode.getValue() == Mode.Filled || this.mode.getValue() == Mode.OutlinedBox || this.mode.getValue() == Mode.Flat || this.mode.getValue() == Mode.FlatOutlinedBox;
        boolean outline = this.mode.getValue() == Mode.Outline || this.mode.getValue() == Mode.OutlinedBox || this.mode.getValue() == Mode.FlatOutline || this.mode.getValue() == Mode.FlatOutlinedBox;

        for (HoleManager.Hole hole : Inferno.holeManager.getHoles()) {
            ColorUtil.Color c = hole.getRating() == HoleManager.Rating.Safe ? this.safeHole.getValue() : this.unsafeHole.getValue();
            int color = ColorUtil.getColor((int) c.getRed(), (int) c.getGreen(), (int) c.getBlue(), (int) c.getAlpha());

            AxisAlignedBB box = this.offsetBoundingBox(new AxisAlignedBB(hole.getPos())).offset(RenderUtil.getScreen());
            RenderUtil.drawEsp(box, filled, outline, this.width.getValue(), color);
        }

        if (this.voidHoles.getValue() && !this.voidHolePositions.isEmpty()) {
            for (BlockPos hole : this.voidHolePositions) {
                ColorUtil.Color c = this.voidHole.getValue();
                int color = ColorUtil.getColor((int) c.getRed(), (int) c.getGreen(), (int) c.getBlue(), (int) c.getAlpha());

                AxisAlignedBB box = this.offsetBoundingBox(new AxisAlignedBB(hole)).offset(RenderUtil.getScreen());
                RenderUtil.drawEsp(box, filled, outline, this.width.getValue(), color);
            }
        }
    }

    @Override
    public void onUpdate() {
        if (this.voidHoles.getValue() && this.timer.passedS(1.5)) {
            this.timer.reset();
            this.voidHolePositions = BlockUtil.getSphere(mc.player.getPosition(), 8, 8, false, true, 0)
                    .stream().filter((pos) -> pos.getY() == 0 && mc.world.isAirBlock(pos))
                    .collect(Collectors.toList());
        }
    }

    private AxisAlignedBB offsetBoundingBox(AxisAlignedBB box) {
        if (this.mode.getValue() == Mode.Filled || this.mode.getValue() == Mode.Outline || this.mode.getValue() == Mode.OutlinedBox) {
            box = box.setMaxY(box.minY + this.height.getValue());
        } else {
            box = box.setMaxY(box.minY);
        }

        return box;
    }

    public enum Mode {
        Filled, Outline, OutlinedBox,
        Flat, FlatOutline, FlatOutlinedBox
    }
}
