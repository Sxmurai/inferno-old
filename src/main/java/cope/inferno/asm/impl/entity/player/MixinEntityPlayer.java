package cope.inferno.asm.impl.entity.player;

import cope.inferno.asm.duck.IEntityPlayer;
import cope.inferno.core.Inferno;
import cope.inferno.core.events.EntityVelocityEvent;
import cope.inferno.core.manager.managers.relationships.impl.Relationship;
import cope.inferno.core.manager.managers.relationships.impl.Status;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements IEntityPlayer {
    public MixinEntityPlayer(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "isPushedByWater", at = @At("HEAD"), cancellable = true)
    public void hookIsPushedByWater(CallbackInfoReturnable<Boolean> info) {
        EntityVelocityEvent event = new EntityVelocityEvent(EntityVelocityEvent.Material.LIQUID);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.setReturnValue(false);
        }
    }


    @Override
    public Relationship getRelationship() {
        return Inferno.INSTANCE.getRelationshipManager().getRelationship(getUniqueID());
    }

    @Override
    public Status getStatus() {
        Relationship relation = getRelationship();
        return relation == null ? Status.NEUTRAL : relation.getStatus();
    }

    @Override
    public void setStatus(Status status) {
        Relationship relation = getRelationship();
        if (relation != null) {
            relation.setStatus(status);
        } else {
            Inferno.INSTANCE.getRelationshipManager().add(getUniqueID(), status);
        }
    }

    @Override
    public boolean isRelationship(Status status) {
        return getRelationship() != null;
    }
}
