package cope.inferno.impl.features.module.modules.combat;

import cope.inferno.Inferno;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;

@Module.Define(name = "Quiver", category = Module.Category.Combat)
@Module.Info(description = "Shoots positive arrow effects at you")
public class Quiver extends Module {
    public final Setting<Effect> primary = new Setting<>("Primary", Effect.Speed);
    public final Setting<Effect> secondary = new Setting<>("Secondary", Effect.Strength);
    public final Setting<Integer> amount = new Setting<>("Amount", 4, 0, 20);

    @Override
    public void onTick() {
        if (mc.player.isHandActive() && mc.player.getActiveItemStack().getItem() == Items.BOW && mc.player.getItemInUseMaxCount() >= this.amount.getValue()) {
            if (!mc.player.isPotionActive(this.primary.getValue().effect)) {
                Inferno.rotationManager.setRotations(Inferno.rotationManager.getYaw(), -90.0f);
                this.doShit(this.primary.getValue().effect);
                return;
            }

            if (!mc.player.isPotionActive(this.secondary.getValue().effect)) {
                Inferno.rotationManager.setRotations(Inferno.rotationManager.getYaw(), -90.0f);
                this.doShit(this.secondary.getValue().effect);
            }
        }
    }

    private void doShit(Potion effect) {
        int arrowSlot = -1;
        for (int i = 9; i < 36; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == Items.TIPPED_ARROW) {
                if (PotionUtils.getEffectsFromStack(stack).stream().anyMatch((e) -> e.getPotion().equals(effect))) {
                    arrowSlot = i;
                    break;
                }
            }
        }

        if (arrowSlot == -1) {
            return;
        }

        if (arrowSlot != 9) {
            boolean containedItem = mc.player.inventoryContainer.getSlot(36).getHasStack();

            this.click(arrowSlot);
            this.click(9);
            if (containedItem) {
                this.click(arrowSlot);
            }
        }

        if (mc.player.isHandActive()) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, mc.player.getPosition(), EnumFacing.UP));
            mc.player.stopActiveHand();
        }

        // put back
        if (arrowSlot != 9) {
            boolean containedItem = mc.player.inventoryContainer.getSlot(arrowSlot).getHasStack();

            this.click(9);
            this.click(arrowSlot);
            if (containedItem) {
                this.click(9);
            }
        }
    }

    private void click(int id) {
        Inferno.inventoryManager.click(id, ClickType.PICKUP);
    }

    public enum Effect {
        Strength(MobEffects.STRENGTH),
        JumpBoost(MobEffects.JUMP_BOOST),
        Speed(MobEffects.SPEED);

        private final Potion effect;
        Effect(Potion effect) {
            this.effect = effect;
        }
    }
}
