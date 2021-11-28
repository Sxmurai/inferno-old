package cope.inferno.asm.mixins.world.blocks;

import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.modules.movement.NoSlow;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockSlime.class)
public class MixinBlockSlime extends BlockBreakable {
    protected MixinBlockSlime(Material materialIn, boolean ignoreSimilarityIn, MapColor mapColorIn) {
        super(materialIn, ignoreSimilarityIn, mapColorIn);
    }

    @Redirect(method = "onEntityWalk", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    public boolean onEntityWalk(Entity entityIn) {
        return NoSlow.INSTANCE.isOn() && NoSlow.slime.getValue() && entityIn == Wrapper.mc.player || entityIn.isSneaking();
    }
}