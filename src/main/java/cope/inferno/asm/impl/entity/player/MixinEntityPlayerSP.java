package cope.inferno.asm.impl.entity.player;

import com.mojang.authlib.GameProfile;
import cope.inferno.asm.duck.IEntityPlayerSP;
import cope.inferno.core.Inferno;
import cope.inferno.core.manager.managers.relationships.impl.Relationship;
import cope.inferno.core.manager.managers.relationships.impl.Status;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP extends AbstractClientPlayer implements IEntityPlayerSP {
    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }



    // methods inherited from IEntityPlayerSP interface below

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
        }
    }

    @Override
    public boolean isRelationship(Status status) {
        return getRelationship() != null;
    }
}
