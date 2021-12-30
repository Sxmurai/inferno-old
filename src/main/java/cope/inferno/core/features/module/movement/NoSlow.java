package cope.inferno.core.features.module.movement;

import cope.inferno.core.events.IsKeyPressedEvent;
import cope.inferno.core.events.PacketEvent;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.network.NetworkUtil;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class NoSlow extends Module {
    public static NoSlow INSTANCE;

    private final KeyBinding[] MOVEMENT_BINDS = new KeyBinding[] {
        mc.gameSettings.keyBindForward,
        mc.gameSettings.keyBindBack,
        mc.gameSettings.keyBindRight,
        mc.gameSettings.keyBindLeft,
        mc.gameSettings.keyBindJump,
        mc.gameSettings.keyBindSneak
    };

    public NoSlow() {
        super("NoSlow", Category.MOVEMENT, "Stops you from being slowed down by items");
        INSTANCE = this;
    }

    public static final Setting<Bypass> bypass = new Setting<>("Bypass", Bypass.NCP);
    public static final Setting<Boolean> guiMove = new Setting<>("GuiMove", true);

    public static final Setting<Boolean> items = new Setting<>("Items", true);
    public static final Setting<Float> multiplier = new Setting<>(items, "Multiplier", 5.0f, 0.0f, 5.0f);
    public static final Setting<Boolean> food = new Setting<>(items, "Foods", true);
    public static final Setting<Boolean> bows = new Setting<>(items, "Bows", true);
    public static final Setting<Boolean> potions = new Setting<>(items, "Potions", true);

    public static final Setting<Boolean> soulsand = new Setting<>("Soulsand", false);
    public static final Setting<Boolean> slime = new Setting<>("Slime", false);

    private boolean sneakState = false;

    @Override
    protected void onDisable() {
        if (sneakState) {
            sneakState = false;
            NetworkUtil.send(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }

    @Override
    public void onUpdate() {
        if (!canNoSlow() && sneakState) {
            sneakState = false;
            NetworkUtil.send(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }

        if (guiMove.getValue() && mc.currentScreen != null) {
            mc.currentScreen.allowUserInput = true;

            for (KeyBinding keyBinding : MOVEMENT_BINDS) {
                KeyBinding.setKeyBindState(keyBinding.getKeyCode(), Keyboard.isKeyDown(keyBinding.getKeyCode()));
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                mc.player.rotationPitch -= 3.5f;
            } else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                mc.player.rotationPitch += 3.5f;
            } else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                mc.player.rotationYaw += 3.5f;
            } else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                mc.player.rotationYaw -= 3.5f;
            }

            // clamp pitch from -90 and 90, if you go past either of these it'll flag NCP
            mc.player.rotationPitch = MathHelper.clamp(mc.player.rotationPitch, -90.0f, 90.0f);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && bypass.getValue().equals(Bypass.NCP)) {
            NetworkUtil.send(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, mc.player.getPosition(), EnumFacing.DOWN));
        }

        if (event.getPacket() instanceof CPacketClickWindow && bypass.getValue().equals(Bypass.STRICT)) {
            NetworkUtil.send(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
        }
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event) {
        if (canNoSlow() && event.getEntity() != null && event.getEntity().equals(mc.player)) {
            event.getMovementInput().moveForward *= multiplier.getValue();
            event.getMovementInput().moveStrafe *= multiplier.getValue();
        }
    }

    @SubscribeEvent
    public void onEntityItemUse(LivingEntityUseItemEvent event) {
        if (canNoSlow() && event.getEntity() != null && event.getEntity().equals(mc.player)) {
            if (bypass.getValue().equals(Bypass.SNEAK) && !sneakState) {
                sneakState = true;
                NetworkUtil.send(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }

            if (bypass.getValue().equals(Bypass.STRICT)) {
                NetworkUtil.send(new CPacketHeldItemChange(mc.player.inventory.currentItem));
            }
        }
    }

    @SubscribeEvent
    public void onIsKeyPressed(IsKeyPressedEvent event) {
        if (guiMove.getValue() && mc.currentScreen != null) {
            event.setCanceled(true);
        }
    }

    private boolean canNoSlow() {
        if (items.getValue() && mc.player.isHandActive()) {
            Item item = mc.player.getActiveItemStack().getItem();
            if (!food.getValue() && (item instanceof ItemFood || item instanceof ItemBucketMilk)) {
                return false;
            }

            if (!bows.getValue() && item instanceof ItemBow) {
                return false;
            }

            if (!potions.getValue() && item instanceof ItemPotion) {
                return false;
            }
        }

        return mc.player.isHandActive();
    }

    public enum Bypass {
        /**
         * No bypass, just normal no slow
         */
        NONE,

        /**
         * Bypasses shitty anticheats/old NCP
         */
        NCP,

        /**
         * Bypasses on NCP-Updated by spoofing a sneak state
         * Only works while strafing.
         */
        SNEAK,

        /**
         * Full bypass for NCP-Updated
         * Sends a CPacketHeldItemChange packet constantly. Yep, that's it.
         * Also sends a STOP_SPRINTING packet for when a CPacketWindowClick packet is sent for the inv move bypass
         */
        STRICT
    }
}
