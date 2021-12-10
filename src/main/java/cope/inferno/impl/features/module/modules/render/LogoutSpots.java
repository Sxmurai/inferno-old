package cope.inferno.impl.features.module.modules.render;

import cope.inferno.impl.event.network.ConnectionEvent;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.render.ColorUtil;
import cope.inferno.util.render.RenderUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

@Module.Define(name = "LogoutSpots", category = Module.Category.Render)
@Module.Info(description = "Shows where people logged out")
public class LogoutSpots extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Outline);
    public final Setting<ColorUtil.Color> color = new Setting<>("Color", new ColorUtil.Color(255, 0, 0, 255));
    public final Setting<Boolean> coordinates = new Setting<>("Coordinates", true);

    private final ArrayList<LogoutSpot> logoutSpots = new ArrayList<>();

    @Override
    public void onRenderWorld() {
        if (!this.logoutSpots.isEmpty()) {
            for (LogoutSpot spot : this.logoutSpots) {
                boolean outline = this.mode.getValue() == Mode.Outline || this.mode.getValue() == Mode.Both;
                boolean filled = this.mode.getValue() == Mode.Filled || this.mode.getValue() == Mode.Both;

                ColorUtil.Color c = this.color.getValue();
                RenderUtil.drawEsp(spot.box.offset(RenderUtil.getScreen()), filled, outline, 1.5f, ColorUtil.getColor((int) c.getRed(), (int) c.getGreen(), (int) c.getBlue(), (int) c.getAlpha()));
            }
        }
    }

    @SubscribeEvent
    public void onConnection(ConnectionEvent event) {
        EntityPlayer player = event.getPlayer();

        if (player == mc.player) {
            this.logoutSpots.clear();
        } else {
            if (event.getType() == ConnectionEvent.Type.LEAVE) {
                this.logoutSpots.add(new LogoutSpot(player.getRenderBoundingBox(), event.getName(), player.getPosition()));
            } else if (event.getType() == ConnectionEvent.Type.JOIN) {
                for (LogoutSpot spot : this.logoutSpots) {
                    if (spot.username.equalsIgnoreCase(event.getName())) {
                        this.logoutSpots.remove(spot);
                        break;
                    }
                }
            }
        }
    }

    private static class LogoutSpot {
        private final AxisAlignedBB box;
        private final String username;
        private final BlockPos pos;

        public LogoutSpot(AxisAlignedBB box, String username, BlockPos pos) {
            this.box = box;
            this.username = username;
            this.pos = pos;
        }
    }

    public enum Mode {
        Outline, Filled, Both
    }
}
