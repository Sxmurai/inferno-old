package cope.inferno.asm.mixins.render;

import cope.inferno.impl.event.render.RenderModelEvent;
import cope.inferno.util.render.ColorUtil;
import cope.inferno.impl.features.module.modules.render.Chams;
import cope.inferno.impl.features.module.modules.render.PopChams;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// the opengl settings were taken from https://github.com/SkidFxcte/Cr33pyware/blob/main/src/main/java/dev/fxcte/creepyware/mixin/mixins/MixinRenderLivingBase.java
// when i learn opengl entirely, ill rewrite. @todo
@Mixin(RenderLivingBase.class)
public abstract class MixinRenderLivingBase<T extends EntityLivingBase> extends Render<T> {
    protected MixinRenderLivingBase(RenderManager renderManager) {
        super(renderManager);
    }

    @Redirect(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V"))
    public void renderModel(ModelBase modelBase, Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        boolean shouldCancel = false;
        if (PopChams.INSTANCE.isOn() && entity != null && PopChams.INSTANCE.pops.containsKey(entity.entityId)) {
            float alpha = PopChams.INSTANCE.pops.get(entity.entityId).getProgress();
            shouldCancel = true;

            ColorUtil.Color color = PopChams.INSTANCE.color.getValue();

            GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glDisable(2929);
            GL11.glEnable(10754);
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, alpha / 255.0f);

            modelBase.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

            GL11.glDisable(2929);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPopAttrib();
            GL11.glPopMatrix();

            PopChams.INSTANCE.pops.get(entity.entityId).update(true);
        } else {
            if (Chams.INSTANCE.isOn() && Chams.INSTANCE.mode.getValue() != Chams.Mode.Normal) {
                ColorUtil.Color color = Chams.INSTANCE.color.getValue();

                shouldCancel = true;

                GL11.glPushAttrib(1048575);
                GL11.glDisable(3008);
                GL11.glDisable(3553);
                GL11.glDisable(2896);
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glLineWidth(1.5f);
                GL11.glEnable(2960);

                GL11.glDisable(2929);
                GL11.glDepthMask(false);
                GL11.glEnable(10754);

                GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
                modelBase.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
                GL11.glEnable(2929);
                GL11.glDepthMask(true);

                if (Chams.INSTANCE.mode.getValue() == Chams.Mode.XQZ) {
                    ColorUtil.Color hidden = Chams.INSTANCE.hidden.getValue();
                    GL11.glColor4f(hidden.getRed() / 255.0f, hidden.getGreen() / 255.0f, hidden.getBlue() / 255.0f, hidden.getAlpha() / 255.0f);
                    modelBase.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
                }

                GL11.glEnable(3042);
                GL11.glEnable(2896);
                GL11.glEnable(3553);
                GL11.glEnable(3008);
                GL11.glPopAttrib();
            }
        }

        RenderModelEvent event = new RenderModelEvent(entity, modelBase, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        MinecraftForge.EVENT_BUS.post(event);

        if (!shouldCancel && !event.isCanceled()) {
            modelBase.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        }
    }

    @Inject(method = "doRender", at = @At("HEAD"))
    public void doRenderPre(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (Chams.INSTANCE.isOn() && Chams.INSTANCE.mode.getValue() == Chams.Mode.Normal) {
            GL11.glEnable(32823);
            GL11.glPolygonOffset(1.0f, -1000000.0f);
        }
    }

    @Inject(method = "doRender", at = @At("RETURN"))
    public void doRenderPost(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (Chams.INSTANCE.isOn() && Chams.INSTANCE.mode.getValue() == Chams.Mode.Normal) {
            GL11.glPolygonOffset(1.0f, 1000000.0f);
            GL11.glDisable(32823);
        }
    }
}
