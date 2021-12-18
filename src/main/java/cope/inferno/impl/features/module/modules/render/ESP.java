package cope.inferno.impl.features.module.modules.render;

import cope.inferno.Inferno;
import cope.inferno.impl.event.render.RenderModelEvent;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.render.ColorUtil;
import cope.inferno.util.render.RenderUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@Module.Define(name = "ESP", category = Module.Category.Render)
@Module.Info(description = "Shows where entities are")
public class ESP extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Wireframe);
    public final Setting<Float> width = new Setting<>("Width", 1.5f, 0.1f, 5.0f);
    public final Setting<Boolean> self = new Setting<>("Self", true);
    public final Setting<Boolean> walls = new Setting<>("Walls", false);

    @Override
    protected void onDeactivated() {
        if (fullNullCheck()) {
            for (Entity entity : mc.world.loadedEntityList) {
                if (entity.isGlowing()) {
                    entity.setGlowing(false);
                }
            }
        }
    }

    @Override
    public void onRenderWorld() {
        if (this.mode.getValue() == Mode.Filled || this.mode.getValue() == Mode.OutlinedBox || this.mode.getValue() == Mode.Glowing) {
            for (Entity entity : mc.world.loadedEntityList) {
                if (!(entity instanceof EntityLivingBase) || (!this.self.getValue() && entity == mc.player)) {
                    continue;
                }

                if (this.mode.getValue() == Mode.Glowing) {
                    entity.setGlowing(true);
                } else {
                    if (entity.isGlowing()) {
                        entity.setGlowing(false);
                    }

                    RenderUtil.drawEsp(entity.getEntityBoundingBox().offset(RenderUtil.getScreen()), this.mode.getValue() == Mode.Filled, this.mode.getValue() == Mode.OutlinedBox, this.width.getValue(), ColorUtil.getColor(255, 255, 255, 80));
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderModel(RenderModelEvent event) {
        if (!(event.getEntity() instanceof EntityLivingBase) || (!this.self.getValue() && event.getEntity() == mc.player)) {
            return;
        }

        if (this.mode.getValue() == Mode.Outline) {
            event.setCanceled(true);
            this.setupFBOs();

            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();

            GL11.glClearStencil(0);
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(this.width.getValue());

            GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xff);
            GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            event.getModelBase().render(event.getEntity(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());

            GL11.glStencilFunc(GL11.GL_NEVER, 0, 0xff);
            GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            event.getModelBase().render(event.getEntity(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());

            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xff);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

            Color color = new Color(255, 255, 255);
            if (event.getEntity() instanceof EntityPlayer && Inferno.friendManager.isFriend(event.getEntity().getUniqueID())) {
                color = new Color(0, 102, 255);
            }

            GL11.glColor3d(color.getRed(), color.getGreen(), color.getBlue());

            if (walls.getValue()) {
                GL11.glDepthMask(false);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }

            event.getModelBase().render(event.getEntity(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());

            if (walls.getValue()) {
                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }

            GL11.glLineWidth(1.0f);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glPopAttrib();
            GL11.glPopMatrix();

            // render model
            event.getModelBase().render(event.getEntity(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());
        } else if (this.mode.getValue() == Mode.Wireframe) {
            event.setCanceled(true);

            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();

            if (this.walls.getValue()) {
                GL11.glDepthMask(false);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);

            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glLineWidth(this.width.getValue());

            Color color = new Color(255, 255, 255);
            if (event.getEntity() instanceof EntityPlayer && Inferno.friendManager.isFriend(event.getEntity().getUniqueID())) {
                color = new Color(0, 102, 255);
            }

            GL11.glColor3d(color.getRed(), color.getGreen(), color.getBlue());

            event.getModelBase().render(event.getEntity(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());

            if (this.walls.getValue()) {
                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }

            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            event.getModelBase().render(event.getEntity(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());

            GL11.glDisable(GL11.GL_LINE_SMOOTH);

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            GL11.glPopAttrib();
            GL11.glPopMatrix();

            event.getModelBase().render(event.getEntity(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScaleFactor());
        }
    }

    private void setupFBOs() {
        Framebuffer framebuffer = mc.framebuffer;
        if (framebuffer != null && framebuffer.depthBuffer > -1) {
            EXTFramebufferObject.glDeleteRenderbuffersEXT(framebuffer.depthBuffer);
            int stencilId = EXTFramebufferObject.glGenRenderbuffersEXT();

            EXTFramebufferObject.glBindRenderbufferEXT(36161, stencilId);
            EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, mc.displayWidth, mc.displayHeight);
            EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencilId);
            EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencilId);

            framebuffer.depthBuffer = -1;
        }
    }

    public enum Mode {
        Outline, Wireframe, Glowing, Filled, OutlinedBox
    }
}
