package cope.inferno.impl.event.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class RenderModelEvent extends Event {
    private final Entity entity;
    private final ModelBase modelBase;
    private final float limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor;

    public RenderModelEvent(Entity entity, ModelBase modelBase, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        this.entity = entity;
        this.modelBase = modelBase;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
        this.scaleFactor = scaleFactor;
    }

    public Entity getEntity() {
        return entity;
    }

    public ModelBase getModelBase() {
        return modelBase;
    }

    public float getLimbSwing() {
        return limbSwing;
    }

    public float getLimbSwingAmount() {
        return limbSwingAmount;
    }

    public float getAgeInTicks() {
        return ageInTicks;
    }

    public float getNetHeadYaw() {
        return netHeadYaw;
    }

    public float getHeadPitch() {
        return headPitch;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }
}
