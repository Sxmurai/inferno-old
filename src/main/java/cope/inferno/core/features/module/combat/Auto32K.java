package cope.inferno.core.features.module.combat;

import cope.inferno.core.events.BlockPlaceEvent;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.inventory.InventoryUtil;
import cope.inferno.util.entity.inventory.Swap;
import cope.inferno.util.internal.ChatUtil;
import cope.inferno.util.internal.timing.Timer;
import cope.inferno.util.world.block.Place;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Auto32K extends Module {
    public Auto32K() {
        super("Auto32K", Category.COMBAT, "Made for ohare.cc, but it does what it says");
    }

    public static final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public static final Setting<Boolean> swing = new Setting<>("Swing", true);
    public static final Setting<Place> place = new Setting<>("Place", Place.VANILLA);

    public static final Setting<Boolean> autoOpen = new Setting<>("AutoOpen", true);
    public static final Setting<Boolean> autoDrop = new Setting<>("AutoDrop", true);

    public static final Setting<Integer> swordSlot = new Setting<>("Slot", 9, 1, 9);

    private BlockPos hopperPos = null;
    private final Timer timer = new Timer();

    private Stage stage = Stage.HOPPER;

    @Override
    protected void onDisable() {
        hopperPos = null;
        stage = Stage.HOPPER;
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiHopper && stage.equals(Stage.OPENING)) {
            stage = Stage.READY;
        } else if (event.getGui() == null && stage.equals(Stage.READY)) {
            hopperPos = null;
            stage = Stage.HOPPER;
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack stack = mc.player.getHeldItem(event.getHand());
        if (InventoryUtil.isBlockItemStack(stack)) {
            Block block = ((ItemBlock) stack.getItem()).getBlock();

            if (block.equals(Blocks.HOPPER) && stage.equals(Stage.HOPPER)) {
                hopperPos = event.getPos().offset(event.getFacing());
            }
        }
    }

    @Override
    public void onUpdate() {
        switch (stage) {
            case HOPPER: {
                if (hopperPos != null && mc.world.getBlockState(hopperPos).getBlock().equals(Blocks.HOPPER)) {
                    stage = Stage.SHULKER;

                    int slot = InventoryUtil.getHotbarItem(ItemShulkerBox.class, null, false);
                    if (slot == -1) {
                        ChatUtil.sendPrefixed("No shulker found! Please re-place the hopper with a shulker in your hotbar!");

                        hopperPos = null;
                        stage = Stage.HOPPER;
                        return;
                    }

                    getInferno().getInventoryManager().swap(slot, Swap.CLIENT);
                    mc.player.setActiveHand(EnumHand.MAIN_HAND);

                    getInferno().getInteractionManager().place(hopperPos.offset(EnumFacing.UP), place.getValue(), rotate.getValue(), swing.getValue());
                }
                break;
            }

            case SHULKER: {
                BlockPos shulkerPos = hopperPos.offset(EnumFacing.UP);
                if (mc.world.getBlockState(shulkerPos).getBlock() instanceof BlockShulkerBox) {
                    stage = Stage.OPENING;

                    getInferno().getInventoryManager().swap(swordSlot.getValue() - 1, Swap.CLIENT);

                    if (autoOpen.getValue()) {
                        getInferno().getInteractionManager().rightClick(hopperPos, rotate.getValue(), swing.getValue());
                    }
                }
                break;
            }

            case READY: {
                if (!(mc.currentScreen instanceof GuiHopper)) {
                    return;
                }

                if (!is32k(mc.player.inventory.getStackInSlot(swordSlot.getValue())) && autoDrop.getValue()) {
                    mc.playerController.windowClick(0, InventoryUtil.toClickableSlot(swordSlot.getValue()), 0, ClickType.THROW, mc.player);
                }

                break;
            }
        }
    }

    private boolean is32k(ItemStack stack) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, stack) == 32767;
    }

    public enum Stage {
        HOPPER, SHULKER, OPENING, READY
    }
}
