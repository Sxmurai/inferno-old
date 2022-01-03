package cope.inferno.core.features.module.other;

import cope.inferno.asm.duck.IEntityPlayer;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.manager.managers.relationships.impl.Status;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.inventory.InventoryUtil;
import cope.inferno.util.entity.inventory.Swap;
import cope.inferno.util.internal.ChatUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

public class MiddleClick extends Module {
    public MiddleClick() {
        super("MiddleClick", Category.OTHER, "Does things upon a middle click");
    }

    public static final Setting<Boolean> friend = new Setting<>("Friend", true);
    public static final Setting<Boolean> unfriend = new Setting<>(friend, "Unfriend", true);

    public static final Setting<Boolean> pearl = new Setting<>("Pearl", true);

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (Mouse.isButtonDown(2) && Mouse.getEventButtonState()) {
            RayTraceResult rayTraceResult = mc.objectMouseOver;

            if (rayTraceResult.typeOfHit.equals(RayTraceResult.Type.MISS) && pearl.getValue()) {
                int slot = InventoryUtil.getHotbarItem(Items.ENDER_PEARL, null, true);
                if (slot == -1) {
                    return;
                }

                int oldSlot = mc.player.inventory.currentItem;
                getInferno().getInventoryManager().swap(slot, Swap.CLIENT);

                mc.playerController.processRightClick(mc.player, mc.world, slot == 45 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);

                getInferno().getInventoryManager().swap(oldSlot, Swap.CLIENT);
            }

            if (rayTraceResult.typeOfHit.equals(RayTraceResult.Type.ENTITY) &&
                    rayTraceResult.entityHit instanceof EntityPlayer &&
                    friend.getValue()) {

                EntityPlayer player = (EntityPlayer) rayTraceResult.entityHit;

                Status status = ((IEntityPlayer) player).getStatus();
                if (!unfriend.getValue() && status.equals(Status.FRIEND)) {
                    return;
                }

                Status newStatus = status.equals(Status.FRIEND) ? Status.NEUTRAL : Status.FRIEND;
                ((IEntityPlayer) player).setStatus(newStatus);

                ChatUtil.sendPrefixed(
                        player.getName() +
                                " has been " +
                                (newStatus.equals(Status.FRIEND) ?
                                        "added to " :
                                        "removed from ") +
                                "your friends list.");
            }
        }
    }
}
