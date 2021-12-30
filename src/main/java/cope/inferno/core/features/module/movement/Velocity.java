package cope.inferno.core.features.module.movement;

import cope.inferno.core.events.EntityVelocityEvent;
import cope.inferno.core.events.PacketEvent;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Velocity extends Module {
    public Velocity() {
        super("Velocity", Category.MOVEMENT, "Prevents vanilla velocity modifications");
    }

    // modifications
    public static final Setting<Float> horizontal = new Setting<>("Horizontal", 0.0f, 0.0f, 100.0f);
    public static final Setting<Float> vertical = new Setting<>("Vertical", 0.0f, 0.0f, 100.0f);

    // packet modification related items
    public static final Setting<Boolean> knockback = new Setting<>("Knockback", true);
    public static final Setting<Boolean> explosions = new Setting<>("Explosions", true);

    // other shit
    public static final Setting<Boolean> ice = new Setting<>("Ice", false);
    public static final Setting<Boolean> liquids = new Setting<>("Liquids", false);
    public static final Setting<Boolean> blocks = new Setting<>("Blocks", true);
    public static final Setting<Boolean> push = new Setting<>("Push", true);
    public static final Setting<Boolean> bobbers = new Setting<>("Bobbers", true);

    @Override
    protected void onDisable() {
        if (ice.getValue()) {
            Blocks.ICE.slipperiness = 0.98f;
            Blocks.PACKED_ICE.slipperiness = 0.98f;
            Blocks.FROSTED_ICE.slipperiness = 0.98f;
        }
    }

    @Override
    public void onUpdate() {
        if (ice.getValue()) {
            Blocks.ICE.slipperiness = 0.0f;
            Blocks.PACKED_ICE.slipperiness = 0.0f;
            Blocks.FROSTED_ICE.slipperiness = 0.0f;
        } else {
            Blocks.ICE.slipperiness = 0.98f;
            Blocks.PACKED_ICE.slipperiness = 0.98f;
            Blocks.FROSTED_ICE.slipperiness = 0.98f;
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketEntityVelocity && knockback.getValue()) {
            SPacketEntityVelocity packet = event.getPacket();

            if (horizontal.getValue() == 0.0f && vertical.getValue() == 0.0f) {
                event.setCanceled(true);
                return;
            }

            packet.motionX *= horizontal.getValue().intValue();
            packet.motionY *= vertical.getValue().intValue();
            packet.motionZ *= horizontal.getValue().intValue();
        }

        if (event.getPacket() instanceof SPacketExplosion && explosions.getValue()) {
            SPacketExplosion packet = event.getPacket();

            if (horizontal.getValue() == 0.0f && vertical.getValue() == 0.0f) {
                event.setCanceled(true);
                return;
            }

            packet.motionX *= horizontal.getValue();
            packet.motionY *= vertical.getValue();
            packet.motionZ *= horizontal.getValue();
        }

        if (event.getPacket() instanceof SPacketEntityStatus && bobbers.getValue()) {
            SPacketEntityStatus packet = event.getPacket();

            if (packet.getEntity(mc.world) instanceof EntityFishHook && packet.getOpCode() == 31) {
                EntityFishHook entity = (EntityFishHook) packet.getEntity(mc.world);
                if (entity.caughtEntity == mc.player) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityVelocity(EntityVelocityEvent event) {
        switch (event.getMaterial()) {
            case BLOCK: event.setCanceled(blocks.getValue()); break;
            case ENTITY: event.setCanceled(push.getValue()); break;
            case LIQUID: event.setCanceled(liquids.getValue()); break;
        }
    }
}
