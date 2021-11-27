package me.sxmurai.inferno.impl.features.module.modules.movement;

import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.EnumConverter;
import me.sxmurai.inferno.impl.settings.Setting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
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
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindSneak,
            mc.gameSettings.keyBindJump,
            mc.gameSettings.keyBindSprint
    };

    public final Setting<Bypass> bypass = new Setting<>("Bypass", Bypass.NCP);
    public final Setting<Boolean> items = new Setting<>("Items", true);
    public final Setting<Boolean> guiMove = new Setting<>("GuiMove", true);
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
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }

        this.sneaking = false;
    }

    @Override
    public void onUpdate() {
        if (this.guiMove.getValue() && mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) {
            for (KeyBinding binding : NoSlow.MOVEMENT_KEYS) {
                KeyBinding.setKeyBindState(binding.getKeyCode(), Keyboard.isKeyDown(binding.getKeyCode()));
            }
        }

        if (this.items.getValue() && !mc.player.isHandActive() && this.bypass.getValue() == Bypass.Sneak && this.sneaking) {
            this.sneaking = false;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }

        if (this.bypass.getValue() == Bypass.New) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (fullNullCheck() && event.getPacket() instanceof CPacketPlayer && this.bypass.getValue() == Bypass.NCP && this.shouldDoItemNoSlow()) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, mc.player.getPosition(), EnumFacing.DOWN));
        }
    }

    @SubscribeEvent
    public void onItemUse(LivingEntityUseItemEvent event) {
        if (this.shouldDoItemNoSlow() && this.bypass.getValue() == Bypass.Sneak && !this.sneaking) {
            this.sneaking = true;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
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
        return this.items.getValue() && mc.player.isHandActive() && !mc.player.isRiding();
    }

    public enum Bypass {
        NCP, Sneak, New
    }
}
