package cope.inferno.core.features.module.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.inferno.asm.duck.IEntityPlayer;
import cope.inferno.core.Inferno;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.EntityUtil;
import cope.inferno.util.render.ColorUtil;
import cope.inferno.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class Nametags extends Module {
    public static Nametags INSTANCE;

    public Nametags() {
        super("Nametags", Category.RENDER, "Shows a descriptive nametag");
        INSTANCE = this;
    }

    // scaling
    public static final Setting<Double> scale = new Setting<>("Scale", 3.0, 1.0, 10.0);
    public static final Setting<Integer> dynamic = new Setting<>("Dynamic", 5, 1, 10);

    // background
    public static final Setting<Boolean> background = new Setting<>("Background", true);
    public static final Setting<Boolean> outline = new Setting<>(background, "Outline", true);
    public static final Setting<Float> outlineWidth = new Setting<>(outline, "OutlineWidth", 3.0f, 1.0f, 5.0f);
    public static final Setting<Color> outlineColor = new Setting<>(outline, "OutlineColor", new Color(0, 0, 0));
    public static final Setting<Float> opacity = new Setting<>(background, "Opacity", 0.6f, 0.1f, 1.0f);

    public static final Setting<Boolean> ping = new Setting<>("Ping", true);
    public static final Setting<Boolean> health = new Setting<>("Health", true);
    public static final Setting<Boolean> totems = new Setting<>("Totems", false);

    public static final Setting<Boolean> items = new Setting<>("Items", true);
    public static final Setting<Boolean> durability = new Setting<>(items, "Durability", true);
    public static final Setting<Boolean> mainhand = new Setting<>(items, "Mainhand", true);
    public static final Setting<Boolean> showName = new Setting<>(mainhand, "ShowName", false);
    public static final Setting<Boolean> offhand = new Setting<>(items, "Offhand", true);
    public static final Setting<Boolean> armor = new Setting<>(items, "Armor", true);
    public static final Setting<Boolean> reversed = new Setting<>(armor, "Reversed", true);
    public static final Setting<Boolean> enchantments = new Setting<>(armor, "Enchantments", true);
    public static final Setting<Boolean> simple = new Setting<>(enchantments, "Simple", false);
    public static final Setting<Integer> amount = new Setting<>(enchantments, "SimpleAmount", 4, 2, 5);

    @Override
    public void onRender3D() {
        if (mc.getRenderViewEntity() != null) {
            for (EntityPlayer player : mc.world.playerEntities) {
                if (player == null || player.equals(mc.player)) {
                    continue;
                }

                double x = RenderUtil.interpolate(player.posX, player.lastTickPosX) - mc.renderManager.renderPosX;
                double y = RenderUtil.interpolate(player.posY, player.lastTickPosY) - mc.renderManager.renderPosY;
                double z = RenderUtil.interpolate(player.posZ, player.lastTickPosZ) - mc.renderManager.renderPosZ;

                renderNametag(player, x, y, z);
            }
        }
    }

    private void renderNametag(EntityPlayer player, double x, double y, double z) {
        Vec3d camera = new Vec3d(mc.renderManager.renderPosX, mc.renderManager.renderPosY, mc.renderManager.renderPosZ);

        double distance = mc.getRenderViewEntity().getDistance(x + camera.x, y + camera.y, z + camera.z);
        double scaledScale = scale.getValue() / 10.0;
        double scaling = Math.max(scaledScale * distance, scaledScale * dynamic.getValue()) / 50.0;

        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        GlStateManager.disableLighting();
        GlStateManager.translate(x, y + (player.isSneaking() ? 0.5 : 0.7) + 1.4, z);
        GlStateManager.rotate(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(mc.renderManager.playerViewX, mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scaling, -scaling, scaling);
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        StringBuilder display = new StringBuilder()
                .append(((IEntityPlayer) player).getStatus().getColor())
                .append(player.getName())
                .append(ChatFormatting.RESET)
                .append(" ");

        if (ping.getValue()) {
            display
                    .append(mc.player.connection.getPlayerInfo(player.getUniqueID()).getResponseTime())
                    .append("ms")
                    .append(" ");
        }

        if (health.getValue()) {
            float health = EntityUtil.getHealth(mc.player);
            display
                    .append(getHealthColor(health))
                    .append(Math.round(health * 10.0f) / 10.0f)
                    .append(ChatFormatting.RESET)
                    .append(" ");
        }

        if (totems.getValue()) {
            int totems = getInferno().getTotemPopManager().getPops(player);
            if (totems > 0) {
                display.append("-").append(totems);
            }
        }

        renderBaseNametag(display.toString());

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.disablePolygonOffset();
        GlStateManager.popMatrix();
    }

    private static String getHealthColor(float health) {
        if (health >= 20.0f) {
            return ChatFormatting.GREEN.toString();
        } else if (health >= 17.0f) {
            return ChatFormatting.YELLOW.toString();
        } else if (health >= 10.0f) {
            return ChatFormatting.RED.toString();
        } else if (health <= 10.0f) {
            return ChatFormatting.DARK_RED.toString();
        }

        return ChatFormatting.WHITE.toString();
    }

    public static void renderBaseNametag(String text) {
        int width = mc.fontRenderer.getStringWidth(text) / 2;

        int color = ColorUtil.getColor(0, 0, 0, (int) (opacity.getValue() * 255.0f));

        double x = -width - 4.0;
        double y = -(mc.fontRenderer.FONT_HEIGHT + 1.0);
        double w = (width * 2.0) + 4.0;
        double h = mc.fontRenderer.FONT_HEIGHT + 2.0;

        if (background.getValue()) {
            RenderUtil.drawRectangle(x, y, w, h, color);
        }

        if (outline.getValue()) {
            RenderUtil.drawOutline(x, y, w, h, outlineWidth.getValue(), outlineColor.getValue().getRGB());
        }

        Inferno.INSTANCE.getFontManager().drawNormalizedString(text, -width, -(mc.fontRenderer.FONT_HEIGHT - 1), -1);
    }
}
