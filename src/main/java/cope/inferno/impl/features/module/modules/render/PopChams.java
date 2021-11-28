package cope.inferno.impl.features.module.modules.render;

import cope.inferno.impl.event.entity.TotemPopEvent;
import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.impl.ui.Animation;
import cope.inferno.util.render.ColorUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Module.Define(name = "PopChams", category = Module.Category.Render)
@Module.Info(description = "https://www.youtube.com/watch?v=gjAnN4cdVNg")
public class PopChams extends Module {
    public static PopChams INSTANCE;

    public final Setting<Float> range = new Setting<>("Range", 50.0f, 1.0f, 100.0f);
    public final Setting<Float> speed = new Setting<>("Speed", 1.0f, 0.1f, 5.0f);
    public final Setting<Integer> delay = new Setting<>("Delay", 5, 1, 15);
    public final Setting<ColorUtil.Color> color = new Setting<>("Color", new ColorUtil.Color(138, 66, 245, 120.0f));

    public final Map<Integer, Animation> pops = new ConcurrentHashMap<>();

    public PopChams() {
        INSTANCE = this;
    }

    @Override
    protected void onDeactivated() {
        this.pops.clear();
    }

    @Override
    public void onUpdate() {
        if (!this.pops.isEmpty()) {
            this.pops.forEach((id, animation) -> {
                if (animation.getProgress() <= 0.0f) {
                    Entity entity = Wrapper.mc.world.getEntityByID(id);
                    Wrapper.mc.world.removeEntityFromWorld(id);
                    if (entity != null) {
                        Wrapper.mc.world.removeEntity(entity);
                        Wrapper.mc.world.removeEntityDangerously(entity);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {
        if (event.getPlayer() != Wrapper.mc.player) {
            EntityPlayer popper = event.getPlayer();
            if (Wrapper.mc.player.getDistance(popper) > this.range.getValue()) {
                return;
            }

            EntityOtherPlayerMP yes = new EntityOtherPlayerMP(Wrapper.mc.world, popper.getGameProfile());
            yes.copyLocationAndAnglesFrom(popper);
            yes.setEntityId(694201337 + this.pops.size());

            Wrapper.mc.world.spawnEntity(yes);

            Animation animation = new Animation(0.0f, this.speed.getValue(), this.delay.getValue().longValue() * 10L, true);
            animation.setProgress(this.color.getValue().getAlpha());
            this.pops.put(yes.entityId, animation);
        }
    }
}