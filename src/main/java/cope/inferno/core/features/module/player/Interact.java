package cope.inferno.core.features.module.player;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.inventory.InventoryUtil;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.util.math.BlockPos;

public class Interact extends Module {
    public static Interact INSTANCE;

    public Interact() {
        super("Interact", Category.PLAYER, "Changes how you interact with blocks");
        INSTANCE = this;
    }

    public static final Setting<Boolean> noEntityTrace = new Setting<>("NoEntityTrace", true);
    public static final Setting<Boolean> smart = new Setting<>(noEntityTrace, "Smart", true);

    public static final Setting<Boolean> antiTile = new Setting<>("AntiTile", false);
    public static final Setting<Boolean> chests = new Setting<>(antiTile, "Chests", false);
    public static final Setting<Boolean> enderChests = new Setting<>(antiTile, "EnderChests", false);
    public static final Setting<Boolean> shulkers = new Setting<>(antiTile, "Shulkers", false);
    public static final Setting<Boolean> anvils = new Setting<>(antiTile, "Anvils", true);

    public static final Setting<Boolean> liquidPlace = new Setting<>("LiquidPlace", false);

    public static boolean shouldNoEntityTrace() {
        if (!noEntityTrace.getValue()) {
            return false;
        }

        return !smart.getValue() || InventoryUtil.isHolding(ItemPickaxe.class, false);
    }

    public static boolean shouldNoInteract(BlockPos pos) {
        if (!antiTile.getValue()) {
            return false;
        }

        Block block = mc.world.getBlockState(pos).getBlock();
        if (block == Blocks.AIR) {
            return false;
        }

        return chests.getValue() && block instanceof BlockChest ||
                enderChests.getValue() && block instanceof BlockEnderChest ||
                shulkers.getValue() && block instanceof BlockShulkerBox ||
                anvils.getValue() && block instanceof BlockAnvil;
    }
}