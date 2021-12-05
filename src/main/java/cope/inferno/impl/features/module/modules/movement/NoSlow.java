package cope.inferno.impl.features.module.modules.movement;

import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.EnumConverter;
import cope.inferno.impl.settings.Setting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

// new bypass made by FencingF, thought it was funny and big meme
@Module.Define(name = "NoSlow", category = Module.Category.Movement)
@Module.Info(description = "Stops the slowdown effect")
public class NoSlow extends Module {
    public static NoSlow INSTANCE;
    private static final KeyBinding[] MOVEMENT_KEYS = new KeyBinding[] {
            Wrapper.mc.gameSettings.keyBindForward,
            Wrapper.mc.gameSettings.keyBindBack,
            Wrapper.mc.gameSettings.keyBindRight,
            Wrapper.mc.gameSettings.keyBindLeft,
            Wrapper.mc.gameSettings.keyBindSneak,
            Wrapper.mc.gameSettings.keyBindJump,
            Wrapper.mc.gameSettings.keyBindSprint
    };

    public final Setting<Bypass> bypass = new Setting<>("Bypass", Bypass.NCP);
    public final Setting<Boolean> items = new Setting<>("Items", true);
    public final Setting<Boolean> guiMove = new Setting<>("GuiMove", true);
    public final Setting<Boolean> moveBypass = new Setting<>("MoveBypass", true);
    public static final Setting<Boolean> soulSand = new Setting<>("Soulsand", false);
    public static final Setting<Boolean> slime = new Setting<>("Slime", false);

    private boolean sneaking = false;

    public NoSlow() {
        INSTANCE = this;
    }

    @Override
    public String getDisplayInfo() {
        return EnumConverter.getActualName(this.bypass.getValue());
    }

    @Override
    protected void onDeactivated() {
        if (fullNullCheck() && this.sneaking) {
            Wrapper.mc.player.connection.sendPacket(new CPacketEntityAction(Wrapper.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }

        this.sneaking = false;
    }

    @Override
    public void onUpdate() {
        if (this.guiMove.getValue() && Wrapper.mc.currentScreen != null && !(Wrapper.mc.currentScreen instanceof GuiChat)) {
            for (KeyBinding binding : NoSlow.MOVEMENT_KEYS) {
                KeyBinding.setKeyBindState(binding.getKeyCode(), Keyboard.isKeyDown(binding.getKeyCode()));
            }
        }

        if (this.items.getValue() && !Wrapper.mc.player.isHandActive() && this.bypass.getValue() == Bypass.Sneak && this.sneaking) {
            this.sneaking = false;
            Wrapper.mc.player.connection.sendPacket(new CPacketEntityAction(Wrapper.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }

        if (this.bypass.getValue() == Bypass.New) {
            Wrapper.mc.player.connection.sendPacket(new CPacketHeldItemChange(Wrapper.mc.player.inventory.currentItem));
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            if (this.bypass.getValue() == Bypass.NCP && this.shouldDoItemNoSlow()) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, mc.player.getPosition(), EnumFacing.DOWN));
            }
        } else if (event.getPacket() instanceof CPacketClickWindow) {
            if (this.moveBypass.getValue()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
        }
    }

    @SubscribeEvent
    public void onItemUse(LivingEntityUseItemEvent event) {
        if (this.shouldDoItemNoSlow() && this.bypass.getValue() == Bypass.Sneak && !this.sneaking) {
            this.sneaking = true;
            Wrapper.mc.player.connection.sendPacket(new CPacketEntityAction(Wrapper.mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event) {
        if (this.shouldDoItemNoSlow()) {
            event.getMovementInput().moveForward *= 5.0f;
            event.getMovementInput().moveStrafe *= 5.0f;
        }
    }

    private boolean shouldDoItemNoSlow() {
        return this.items.getValue() && Wrapper.mc.player.isHandActive() && !Wrapper.mc.player.isRiding();
    }

    public enum Bypass {
        NCP, Sneak, New
    }
}
