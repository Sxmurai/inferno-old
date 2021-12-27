package cope.inferno.core.manager.managers;

import cope.inferno.core.features.module.Module;
import cope.inferno.util.internal.Wrapper;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventManager implements Wrapper {
    public static EventManager INSTANCE;

    public EventManager() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (nullCheck() && event.getEntity().equals(mc.player)) {
            for (Module module : getInferno().getModuleManager().getModules()) {
                if (module.isToggled()) {
                    mc.profiler.startSection("update_" + module.getName());

                    module.onUpdate();

                    mc.profiler.endSection();
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (nullCheck()) {
            for (Module module : getInferno().getModuleManager().getModules()) {
                if (module.isToggled()) {
                    mc.profiler.startSection("clientTick_" + module.getName());

                    module.onTick();

                    mc.profiler.endSection();
                }
            }
        }
    }
}
