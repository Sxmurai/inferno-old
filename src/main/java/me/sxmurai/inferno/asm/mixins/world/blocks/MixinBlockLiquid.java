package me.sxmurai.inferno.asm.mixins.world.blocks;

import me.sxmurai.inferno.impl.features.module.modules.player.Interact;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLiquid.class)
public class MixinBlockLiquid {
    @Inject(method = "canCollideCheck", at = @At("HEAD"), cancellable = true)
    public void canCollideCheck(IBlockState state, boolean hitIfLiquid, CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(hitIfLiquid && state.getValue(BlockLiquid.LEVEL) == 0 || (Interact.INSTANCE.isOn() && Interact.liquidPlace.getValue()));
    }
}
