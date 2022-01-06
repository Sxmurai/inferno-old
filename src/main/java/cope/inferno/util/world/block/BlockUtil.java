package cope.inferno.util.world.block;

import com.google.common.collect.Lists;
import cope.inferno.util.internal.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BlockUtil implements Wrapper {
    /**
     * Represents blocks we need to send a sneak packet with to place a block on
     */
    public static final List<Block> SNEAK_BLOCKS = Lists.newArrayList(
            // normal tile entities
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.ANVIL,
            Blocks.ENDER_CHEST,
            Blocks.FURNACE,
            Blocks.LIT_FURNACE,
            Blocks.BREWING_STAND,
            Blocks.DISPENSER,
            Blocks.DROPPER,
            Blocks.BED,
            Blocks.BEACON,
            Blocks.CRAFTING_TABLE,
            Blocks.HOPPER,

            // redstone shit
            Blocks.LEVER,
            Blocks.STONE_BUTTON,
            Blocks.WOODEN_BUTTON,
            Blocks.POWERED_REPEATER,
            Blocks.UNPOWERED_REPEATER,
            Blocks.POWERED_COMPARATOR,
            Blocks.UNPOWERED_COMPARATOR,

            // doors
            Blocks.BREWING_STAND,
            Blocks.ACACIA_DOOR,
            Blocks.DARK_OAK_DOOR,
            Blocks.BIRCH_DOOR,
            Blocks.JUNGLE_DOOR,
            Blocks.OAK_DOOR,
            Blocks.SPRUCE_DOOR,

            // command blocks
            Blocks.COMMAND_BLOCK,
            Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK,

            // all shulker types
            Blocks.BLACK_SHULKER_BOX,
            Blocks.WHITE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.SILVER_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX
    );

    /**
     * An arraylist of all the non-explodable blocks
     */
    public static final ArrayList<Block> EXPLOSION_RESISTANT_BLOCKS = Lists.newArrayList(
            Blocks.OBSIDIAN,
            Blocks.BEDROCK,
            Blocks.COMMAND_BLOCK,
            Blocks.BARRIER,
            Blocks.ENCHANTING_TABLE,
            Blocks.END_PORTAL_FRAME,
            Blocks.BEACON,
            Blocks.ANVIL
    );

    /**
     * Gets the block from a block position
     * @param pos The position
     * @return The block object of what it is
     */
    public static Block getBlockFromPos(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    /**
     * Checks to see if you are needed to sneak to place a block there
     * @param pos The position to check
     * @return true if we need to sneak, false if not
     */
    public static boolean needsToSneak(BlockPos pos) {
        return SNEAK_BLOCKS.contains(getBlockFromPos(pos));
    }

    /**
     * Checks if we can replace this position with a new block here
     * @param pos The position to check
     * @return true if we can place, false if we cannot
     */
    public static boolean isReplaceable(BlockPos pos) {
        return mc.world.getBlockState(pos).getMaterial().isReplaceable();
    }

    /**
     * Checks if there's any entities at this bounding box
     * @param pos The position to check
     * @return true if there is an entity within the bb, or false if there is not
     */
    public static boolean intersects(BlockPos pos) {
        return intersects(new AxisAlignedBB(pos));
    }

    /**
     * Checks if there's any entities at this bounding box
     * @param box The collision box to check
     * @return true if there is an entity within the bb, or false if there is not
     */
    public static boolean intersects(AxisAlignedBB box) {
        return !mc.world.getEntitiesWithinAABB(Entity.class, box, (e) -> e != null && !e.isDead).isEmpty();
    }

    /**
     * Checks if we can click this block
     * @param pos The block's position
     * @return true if its clickable, false if it is not
     */
    public static boolean isClickable(BlockPos pos) {
        return isReplaceable(pos) && !intersects(pos);
    }

    /**
     * Gets the best facing value of a block for placements
     * @param origin The BlockPos to check
     * @return the EnumFacing enum value, or null if none were found
     */
    public static EnumFacing getFacing(BlockPos origin) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = origin.offset(facing);
            if (!isReplaceable(neighbor) && !intersects(neighbor)) {
                return facing;
            }
        }

        return null;
    }

    /**
     * Gets a sphere around a block position
     * @param start The start position
     * @param radius The radius of the sphere
     * @return All blocks surrounding that position in a sphere
     */
    public static ArrayList<BlockPos> getSphere(BlockPos start, int radius) {
        ArrayList<BlockPos> blocks = new ArrayList<>();

        float cx = start.getX(),
                cy = start.getY(),
                cz = start.getZ(),
                x = cx - radius;

        while (x <= cx + radius) {
            float z = cz - radius;

            while (z <= cz + radius) {
                float y = cy - radius;
                while (y < (cy + radius)) {
                    double dist = Math.pow(cx - x, 2) + Math.pow(cy - y, 2) + Math.pow(cz - z, 2);

                    if (dist < Math.pow(radius, 2.0f)) {
                        blocks.add(new BlockPos(x, y, z));
                    }

                    ++y;
                }

                ++z;
            }

            ++x;
        }

        return blocks;
    }

    /**
     * Checks if the headpsace above a position is available
     * @param pos The position to start at
     * @param blocks The amount of blocks to check
     * @return true if headspace is open, false if not
     */
    public static boolean isHeadspaceOpen(BlockPos pos, int blocks) {
        BlockPos current = pos;
        for (int i = 0; i < blocks; ++i) {
            current = current.add(0.0, 1.0, 0.0);
            if (!mc.world.isAirBlock(current)) {
                return false;
            }
        }

        return true;
    }
}
