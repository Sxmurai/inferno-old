package cope.inferno.impl.features.module.modules.combat;

import cope.inferno.impl.features.command.Command;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.entity.EntityUtil;
import cope.inferno.util.entity.InventoryUtil;
import net.minecraft.init.Items;
import net.minecraft.util.text.TextComponentString;

@Module.Define(name = "AutoLog", category = Module.Category.Combat)
@Module.Info(description = "Automatically logs out upon an action")
public class AutoLog extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Disconnect);
    public final Setting<Boolean> disable = new Setting<>("Disable", true);
    public final Setting<Boolean> noTotems = new Setting<>("NoTotems", true);
    public final Setting<Integer> totems = new Setting<>("Totems", 2, 0, 32, this.noTotems::getValue);
    public final Setting<Float> health = new Setting<>("Health", 10.0f, 1.0f, 20.0f);

    @Override
    public void onUpdate() {
        if (this.noTotems.getValue() && InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING) < this.totems.getValue()) {
            this.log("Less than " + this.totems.getValue() + " totems!");
            return;
        }

        if (EntityUtil.getHealth(mc.player) <= this.health.getValue()) {
            this.log("Less than " + this.health.getValue() + " health!");
        }
    }

    private void log(String reason) {
        if (this.mode.getValue() == Mode.Shutdown) {
            mc.shutdown();
            return;
        }

        mc.player.connection.onDisconnect(new TextComponentString(Command.getPrefix() + reason));

        if (this.disable.getValue()) {
            this.toggle();
        }
    }

    public enum Mode {
        Disconnect, Shutdown
    }
}
