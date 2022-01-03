package cope.inferno.core.features.module.render;

import cope.inferno.asm.duck.IEntityPlayer;
import cope.inferno.core.events.LivingBaseModelRenderEvent;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.manager.managers.relationships.impl.Status;
import cope.inferno.core.setting.Setting;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.EXTFramebufferObject;

import static org.lwjgl.opengl.GL11.*;

public class ESP extends Module {
    public ESP() {
        super("ESP", Category.RENDER, "Renders an ESP around entities");
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.OUTLINE);

    public static final Setting<Boolean> walls = new Setting<>("Walls", true);

    public static final Setting<Boolean> smooth = new Setting<>("Smooth", true);
    public static final Setting<Float> width = new Setting<>("Width", 1.0f, 0.1f, 5.0f);

    public static final Setting<Boolean> players = new Setting<>("Players", true);
    public static final Setting<Boolean> self = new Setting<>(players, "Self", true);
    public static final Setting<Boolean> tile = new Setting<>("Tile", true);
    public static final Setting<Boolean> other = new Setting<>("Other", true);

    @Override
    protected void onDisable() {
        for (Entity entity : mc.world.loadedEntityList) {
            entity.setGlowing(false);
        }
    }

    @Override
    public void onRender3D() {
        for (Entity entity : mc.world.loadedEntityList) {
            entity.setGlowing(mode.getValue().equals(Mode.GLOW));
        }

        if (mode.getValue().equals(Mode.GLOW)) {
            mc.renderGlobal.entityOutlineShader.listShaders.forEach((shader) -> {
                ShaderUniform uniform = shader.getShaderManager().getShaderUniform("Radius");
                if (uniform != null) {
                    uniform.set(width.getValue());
                }
            });
        }
    }

    @SubscribeEvent
    public void onLivingBaseModelRender(LivingBaseModelRenderEvent event) {
        if (event.getEntity() == mc.player && !self.getValue()) {
            return;
        }

        if (mode.getValue().equals(Mode.WIREFRAME) || mode.getValue().equals(Mode.OUTLINE)) {
            event.setCanceled(true);

            float r = 1.0f;
            float g = 1.0f;
            float b = 1.0f;

            if (event.getEntity() instanceof EntityPlayer) {
                Status status = ((IEntityPlayer) event.getEntity()).getStatus();
                if (status.equals(Status.FRIEND)) {
                    r = 0.0f;
                    g = 0.4f;
                    b = 1.0f;
                } else if (status.equals(Status.ENEMY)) {
                    r = 1.0f;
                    g = 0.0f;
                    b = 0.0f;
                }
            }

            Runnable runnable = () ->
                    event.getModelBase().render(
                            event.getEntity(),
                            event.getLimbSwing(),
                            event.getLimbSwingAmount(),
                            event.getAgeInTicks(),
                            event.getNetHeadYaw(),
                            event.getHeadPitch(),
                            event.getScaleFactor());

            if (mode.getValue().equals(Mode.OUTLINE)) {
                renderOutline(runnable, r, g, b);
            } else if (mode.getValue().equals(Mode.WIREFRAME)) {
                renderWireframe(runnable, r, g, b);
            }
        }
    }

    private void renderOutline(Runnable runnable, float r, float g, float b) {
        setupFBOs();

        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glPushMatrix();

        glClearStencil(0);
        glClear(GL_STENCIL_BUFFER_BIT);

        glEnable(GL_STENCIL_TEST);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);

        if (smooth.getValue()) {
            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        }

        glLineWidth(width.getValue());

        glStencilFunc(GL_NEVER, 1, 0xff);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        runnable.run();

        glStencilFunc(GL_NEVER, 0, 0xff);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        runnable.run();

        glStencilFunc(GL_EQUAL, 1, 0xff);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        glColor3f(r, g, b);

        if (walls.getValue()) {
            glDepthMask(false);
            glDisable(GL_DEPTH_TEST);
        }

        runnable.run();

        if (walls.getValue()) {
            glEnable(GL_DEPTH_TEST);
            glDepthMask(true);
        }

        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_LIGHTING);

        glPopAttrib();
        glPopMatrix();

        runnable.run();
    }

    private void renderWireframe(Runnable runnable, float r, float g, float b) {
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glPushMatrix();

        if (walls.getValue()) {
            glDepthMask(false);
            glDisable(GL_DEPTH_TEST);
        }

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);

        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        if (smooth.getValue()) {
            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        }

        glLineWidth(width.getValue());
        glColor3f(r, g, b);

        runnable.run();

        if (walls.getValue()) {
            glEnable(GL_DEPTH_TEST);
            glDepthMask(true);
        }

        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        runnable.run();

        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_LIGHTING);

        glPopAttrib();
        glPopMatrix();

        runnable.run();
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
        /**
         * Minecrafts default glow shader with the radius removed
         */
        GLOW,

        /**
         * A simple outline using gl stencil
         */
        OUTLINE,

        /**
         * Draws an outline of every model part
         */
        WIREFRAME,

        /**
         * A simple filled box
         */
        BOXED,

        /**
         * A simple filled outlined box
         */
        OUTLINED_BOX
    }
}
