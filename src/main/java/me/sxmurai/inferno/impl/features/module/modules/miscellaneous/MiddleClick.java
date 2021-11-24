package me.sxmurai.inferno.impl.features.module.modules.miscellaneous;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.command.Command;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.manager.InventoryManager;
import me.sxmurai.inferno.impl.manager.friend.Friend;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

@Module.Define(name = "MiddleClick")
@Module.Info(description = "Does things upon a middle click")
public class MiddleClick extends Module {
    public final Setting<Pearl> pearl = new Setting<>("Pearl", Pearl.None);
    public final Setting<Boolean> friend = new Setting<>("Friend", true);

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (Mouse.getEventButtonState() && Mouse.isButtonDown(2) && mc.objectMouseOver != null) {
            RayTraceResult result = mc.objectMouseOver;
            if (result.typeOfHit == RayTraceResult.Type.MISS) {
                if (this.pearl.getValue() == Pearl.None) {
                    return;
                }

                int slot = InventoryUtil.getHotbarItemSlot(Items.ENDER_PEARL, true);
                if (slot == -1) {
                    return;
                }

                int oldSlot = mc.player.inventory.currentItem;
                if (slot != 45) {
                    Inferno.inventoryManager.swap(slot, this.pearl.getValue().swap);
                }

                mc.playerController.processRightClick(mc.player, mc.world, slot == 45 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);

                if (slot != 45) {
                    Inferno.inventoryManager.swap(oldSlot, this.pearl.getValue().swap);
                }
            } else if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
                if (!friend.getValue()) {
                    return;
                }

                Entity entity = result.entityHit;
                if (!(entity instanceof EntityPlayer) || entity == mc.player) {
                    return;
                }

                EntityPlayer player = (EntityPlayer) entity;
                if (Inferno.friendManager.isFriend(player.getUniqueID())) {
                    Friend friend = Inferno.friendManager.getFriend(player.getUniqueID());
                    if (friend == null) { // wtf
                        return;
                    }

                    Inferno.friendManager.remove(friend);
                    Command.send("Unfriended " + player.getName() + "!");
                } else {
                    Inferno.friendManager.add(new Friend(player.getUniqueID(), player.getName()));
                    Command.send("Friended " + player.getName() + "!");
                }
            }
        }
    }

    public enum Pearl {
        None(null),
        Legit(InventoryManager.Swap.Legit),
        Silent(InventoryManager.Swap.Silent);

        private final InventoryManager.Swap swap;
        Pearl(InventoryManager.Swap swap) {
            this.swap = swap;
        }
    }
}
