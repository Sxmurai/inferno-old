package cope.inferno.asm.mixins.world.blocks;

import cope.inferno.impl.features.module.modules.movement.NoSlow;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockSoulSand.class)
public class MixinBlockSoulSand extends Block {
    public MixinBlockSoulSand(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn, CallbackInfo info) {
        if (NoSlow.INSTANCE.isOn() && NoSlow.soulSand.getValue()) {
            info.cancel();
        }
    }
}