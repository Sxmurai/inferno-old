package me.sxmurai.inferno.impl.features.module.modules.visual;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.option.Option;
import me.sxmurai.inferno.util.entity.EntityUtil;
import me.sxmurai.inferno.util.render.ColorUtil;
import me.sxmurai.inferno.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;

@Module.Define(name = "Nametags", category = Module.Category.Visual)
@Module.Info(description = "Shows custom nametags, overriding the vanilla ones")
public class Nametags extends Module {
    public static Nametags INSTANCE;

    public static final Option<Boolean> self = new Option<>("Self", true);
    public static final Option<Shape> shape = new Option<>("Shape", Shape.Rectangle);
    public static final Option<Boolean> outline = new Option<>("Outline", true);
    public static final Option<Double> scaling = new Option<>("Scaling", 0.3, 0.1, 3.9);
    public static final Option<Double> opacity = new Option<>("Opacity", 0.7, 0.0, 1.0);

    public static final Option<Boolean> invisible = new Option<>("Invisible", true);
    public static final Option<Boolean> nameProtect = new Option<>("NameProtect", false);

    public static final Option<Boolean> mainhand = new Option<>("Mainhand", true);
    public static final Option<Boolean> offhand = new Option<>("Offhand", true);
    public static final Option<Boolean> armor = new Option<>("Armor", true);
    public static final Option<Boolean> reversed = new Option<>("Reversed", true, armor::getValue);
    public static final Option<Boolean> enchants = new Option<>("Enchants", true, () -> mainhand.getValue() || offhand.getValue() || armor.getValue());
    public static final Option<Boolean> health = new Option<>("Health", true);
    public static final Option<Boolean> pops = new Option<>("Pops", false);
    public static final Option<Boolean> ping = new Option<>("Ping", false);

    public Nametags() {
        INSTANCE = this;
    }

    @Override
    public void onRenderWorld() {
        for (EntityPlayer player : mc.world.playerEntities) {
            if (player == null || (player == mc.player && !self.getValue()) || (!invisible.getValue() && EntityUtil.isInvisible(player))) {
                continue;
            }

            Vec3d pos = new Vec3d(player.posX, player.posY, player.posZ).subtract(RenderUtil.renderPositions());
            Nametags.renderNametag(player, pos.x, pos.y, pos.z);
        }
    }

    public static void renderNametag(EntityPlayer player, double x, double y, double z) {
        if (player == null || (player == mc.player && !self.getValue()) || (!invisible.getValue() && EntityUtil.isInvisible(player))) {
            return;
        }

        double head = y + (player.isSneaking() ? 0.5 : 0.7);
        Vec3d camera = RenderUtil.renderPositions();

        double scale = (scaling.getValue() * mc.renderViewEntity.getDistance(x + camera.x, y + camera.y, z + camera.z)) / 50.0;

        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        GlStateManager.disableLighting();
        GlStateManager.translate(x, head + 1.4, z);
        GlStateManager.rotate(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(mc.renderManager.playerViewX, mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        StringBuilder name = new StringBuilder();

        if (ping.getValue()) {
            name.append(Inferno.serverManager.getLatency(player)).append("ms").append(" ");
        }

        name.append(player.isSneaking() ? ChatFormatting.YELLOW : ChatFormatting.WHITE)
                .append(nameProtect.getValue() ? ("Player" + player.hashCode()) : player.getName())
                .append(ChatFormatting.RESET)
                .append(" ");

        if (health.getValue()) {
            float health = EntityUtil.getHealth(player);

            name.append(getHealthColor(health))
                    .append(health)
                    .append(ChatFormatting.RESET)
                    .append(" ");
        }

        if (pops.getValue()) {
            int pops = Inferno.totemPopManager.getPops(player);
            name.append("[").append(pops).append("]");
        }

        double width = Inferno.fontManager.getWidth(name.toString()) / 2.0;
        if (shape.getValue() != Shape.None) {
            drawShape(width);
        }

        Inferno.fontManager.drawCorrectString(name.toString(), -width, -(Inferno.fontManager.getHeight() - 1.0), -1);

        int xOffset = -24 / 2 * player.inventory.armorInventory.size();

        if (mainhand.getValue()) {
            if (!player.getHeldItemMainhand().isEmpty()) {
                GlStateManager.pushMatrix();
                renderItemStack(player.getHeldItemMainhand(), xOffset, -26);
                GlStateManager.popMatrix();

                if (enchants.getValue()) {
                    renderEnchantments(player.getHeldItemMainhand());
                }
            }
        }

        xOffset += 16;

        if (armor.getValue()) {
            ArrayList<ItemStack> armor = new ArrayList<>(player.inventory.armorInventory);
            if (reversed.getValue()) {
                Collections.reverse(armor);
            }

            for (ItemStack stack : armor) {
                if (stack.isEmpty()) {
                    continue;
                }

                GlStateManager.pushMatrix();
                renderItemStack(stack, xOffset, -26);
                GlStateManager.popMatrix();

                if (enchants.getValue()) {
                    renderEnchantments(stack);
                }

                xOffset += 16;
            }
        }

        if (offhand.getValue()) {
            if (!player.getHeldItemOffhand().isEmpty()) {
                GlStateManager.pushMatrix();
                renderItemStack(player.getHeldItemOffhand(), xOffset, -26);
                GlStateManager.popMatrix();

                if (enchants.getValue()) {
                    renderEnchantments(player.getHeldItemOffhand());
                }
            }
        }

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();
    }

    private static void renderItemStack(ItemStack stack, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);
        RenderHelper.enableStandardItemLighting();
        mc.renderItem.zLevel = -150.0f;
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.disableCull();

        mc.renderItem.renderItemAndEffectIntoGUI(stack, x, y);
        mc.renderItem.renderItemOverlays(mc.fontRenderer, stack, x, y);

        mc.renderItem.zLevel = 0.0f;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private static void renderEnchantments(ItemStack stack) {
        // oh no cringe
    }

    private static void drawShape(double width) {
        int color = ColorUtil.getColor(0, 0, 0, (int) (opacity.getValue() * 255));

        double x = -width - 4.0;
        double y = -(Inferno.fontManager.getHeight() + 1.0);
        double w = (width * 2.0) + 4.0;
        double h = Inferno.fontManager.getHeight() + 2.0;

        if (shape.getValue() == Shape.Rectangle) {
            RenderUtil.drawRectangle(x, y, w, h, color);
        } else if (shape.getValue() == Shape.Rounded) {
            RenderUtil.drawRoundedRectangle(x, y, w, h, 5.0, color);
        }
    }

    private static String getHealthColor(float health) {
        if (health >= 20.0f) {
            return ChatFormatting.GREEN.toString();
        } else if (health <= 17.0f) {
            return ChatFormatting.YELLOW.toString();
        }

        return ChatFormatting.RED.toString();
    }

    public enum Shape {
        None, Rectangle, Rounded
    }
}
