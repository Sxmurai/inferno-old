package me.sxmurai.inferno.impl.features.module.modules.movement;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.event.entity.MoveEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.manager.HoleManager;
import me.sxmurai.inferno.impl.settings.Setting;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "Anchor", category = Module.Category.Movement)
@Module.Info(description = "Suspends movement over a hole")
public class Anchor extends Module {
    public final Setting<Double> tolerance = new Setting<>("Tolerance", 0.7, 0.1, 0.9);
    public final Setting<Integer> height = new Setting<>("Height", 5, 1, 10);
    public final Setting<Float> pitch = new Setting<>("Pitch", 45.0f, -90.0f, 90.0f);

    @SubscribeEvent(priority = EventPriority.HIGHEST) // this is so we override things like strafe.
    public void onMove(MoveEvent event) {
        if (Inferno.holeManager.isInHole() || mc.player.posY <= 0.0) {
            return;
        }

        double xOffset = Math.abs(mc.player.posX - Math.floor(mc.player.posX));
        double zOffset = Math.abs(mc.player.posZ - Math.floor(mc.player.posZ));

        double max = this.tolerance.getValue();
        double min = 1.0 - max;

        if (xOffset > max || xOffset < min || zOffset > max || zOffset < min) {
            return;
        }

        BlockPos location = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ).down();
        for (int i = 0; i < this.height.getValue(); ++i) {
            if (!mc.world.isAirBlock(location = location.down())) {
                for (HoleManager.Hole hole : Inferno.holeManager.getHoles()) {
                    if (hole.getPos().equals(location.up())) {
                        event.setX(0.0);
                        event.setZ(0.0);
                        event.setCanceled(true, true);

                        Inferno.rotationManager.setRotations(Inferno.rotationManager.getYaw(true), this.pitch.getValue());
                    }
                }
            }
        }
    }
}
