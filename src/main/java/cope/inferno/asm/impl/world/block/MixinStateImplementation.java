package cope.inferno.asm.impl.world.block;

import cope.inferno.core.events.AddBoxToListEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(BlockStateContainer.StateImplementation.class)
public class MixinStateImplementation {
    @Shadow @Final
    private Block block;

    @Redirect(method = "addCollisionBoxToList", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;addCollisionBoxToList(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V"))
    public void addCollisionBoxToList(Block b, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean p_185908_6_) {
        AddBoxToListEvent event = new AddBoxToListEvent(b, collidingBoxes, pos, entityIn, entityBox);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            block.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185908_6_);
        }
    }
}