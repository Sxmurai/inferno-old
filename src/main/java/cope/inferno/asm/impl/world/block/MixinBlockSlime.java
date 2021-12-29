package cope.inferno.asm.impl.world.block;

import cope.inferno.core.features.module.movement.NoSlow;
import cope.inferno.util.internal.Wrapper;
import net.minecraft.block.BlockSlime;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockSlime.class)
public class MixinBlockSlime {
    @Inject(method = "onEntityWalk", at = @At("HEAD"), cancellable = true)
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn, CallbackInfo info) {
        if (NoSlow.INSTANCE.isToggled() && NoSlow.slime.getValue() && entityIn.equals(Wrapper.mc.player)) {
            info.cancel();
        }
    }
}
