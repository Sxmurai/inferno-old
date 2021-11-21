package me.sxmurai.inferno.impl.features.module.modules.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.entity.EntityUtil;
import me.sxmurai.inferno.util.render.ColorUtil;
import me.sxmurai.inferno.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

@Module.Define(name = "Nametags", category = Module.Category.Render)
@Module.Info(description = "Shows custom nametags, overriding the vanilla ones")
public class Nametags extends Module {
    public static Nametags INSTANCE;

    public static final Setting<Boolean> self = new Setting<>("Self", false);
    public static final Setting<Shape> shape = new Setting<>("Shape", Shape.Rectangle);
    public static final Setting<Boolean> outline = new Setting<>("Outline", true);
    public static final Setting<Double> scaling = new Setting<>("Scaling", 0.3, 0.1, 3.9);
    public static final Setting<Double> opacity = new Setting<>("Opacity", 0.7, 0.0, 1.0);

    public static final Setting<Boolean> invisible = new Setting<>("Invisible", true);
    public static final Setting<Boolean> nameProtect = new Setting<>("NameProtect", false);

    public static final Setting<Boolean> mainhand = new Setting<>("Mainhand", true);
    public static final Setting<Boolean> offhand = new Setting<>("Offhand", true);
    public static final Setting<Boolean> armor = new Setting<>("Armor", true);
    public static final Setting<Boolean> reversed = new Setting<>("Reversed", true, armor::getValue);
    public static final Setting<Enchants> enchants = new Setting<>("Enchants", Enchants.All, () -> mainhand.getValue() || offhand.getValue() || armor.getValue());
    public static final Setting<Boolean> health = new Setting<>("Health", true);
    public static final Setting<Boolean> pops = new Setting<>("Pops", false);
    public static final Setting<Boolean> ping = new Setting<>("Ping", false);

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

        double dist = mc.renderViewEntity.getDistance(x + camera.x, y + camera.y, z + camera.z);
        double scale = (scaling.getValue() * dist) / 50.0;
        if (dist <= 3.0) {
            scale = (scaling.getValue() * 3.0) / 50.0;
        }

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

        String username = player.getName();
        if (nameProtect.getValue()) {
            if (Inferno.friendManager.isFriend(player.getUniqueID())) {
                username = Inferno.friendManager.getFriend(player.getUniqueID()).getAlias();
            } else {
                username = ("Player" + player.hashCode());
            }
        }

        name.append(player.isSneaking() ?
                ChatFormatting.YELLOW :
                Inferno.friendManager.isFriend(player.getUniqueID()) ?
                        ChatFormatting.BLUE :
                        ChatFormatting.WHITE
        ).append(username)
                .append(ChatFormatting.RESET)
                .append(" ");

        if (health.getValue()) {
            float health = EntityUtil.getHealth(player);

            name.append(getHealthColor(health))
                    .append(Math.round(health * 10.0) / 10.0)
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
                renderItemStack(player.getHeldItemMainhand(), xOffset, -26);

                if (enchants.getValue() != Enchants.None) {
                    renderEnchantments(player.getHeldItemMainhand(), xOffset, -26);
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

                renderItemStack(stack, xOffset, -26);

                if (enchants.getValue() != Enchants.None) {
                    renderEnchantments(stack, xOffset, -26);
                }

                xOffset += 16;
            }
        }

        if (offhand.getValue()) {
            if (!player.getHeldItemOffhand().isEmpty()) {
                renderItemStack(player.getHeldItemOffhand(), xOffset, -26);

                if (enchants.getValue() != Enchants.None) {
                    renderEnchantments(player.getHeldItemOffhand(), xOffset, -26);
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
        GlStateManager.depthMask(true);
        RenderHelper.enableStandardItemLighting();
        mc.renderItem.zLevel = -150.0f;
        GlStateManager.disableAlpha();
        GlStateManager.enableDepth();
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

    // this looks like shit, please rewrite @todo
    private static void renderEnchantments(ItemStack stack, double x, double y) {
        // god i hate myself
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);

        ArrayList<String> text = new ArrayList<>();
        if (stack.getItem() == Items.GOLDEN_APPLE && stack.hasEffect()) {
            text.add(ChatFormatting.RED + "god");
        }

        if (enchants.isEmpty() && text.isEmpty()) {
            return;
        }

        if (enchants.size() >= 4 && Nametags.enchants.getValue() == Enchants.Simplified) { // i could make a better way of detecting if its max armor, but meh
            text.add(ChatFormatting.RED + "max");
        } else {
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                text.add(shortenEnchantName(entry.getKey(), entry.getValue()));
            }
        }

        if (!text.isEmpty()) {
            double scaling = 0.5;
            if (y < text.size() * ((Inferno.fontManager.getHeight() + 1) * scaling)) {
                y -= (text.size() * ((Inferno.fontManager.getHeight() + 1) * scaling) + 16.0);
            }

            for (String str : text) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);
                GlStateManager.scale(scaling, scaling, scaling);

                Inferno.fontManager.drawCorrectString(str, x * 2.0, y, -1);
                y += (Inferno.fontManager.getHeight() + 1);

                GlStateManager.scale(2.0, 2.0, 0.0);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.popMatrix();
            }
        }
    }

    // taken from https://github.com/IUDevman/gamesense-client/blob/62061a43fea311f42c64ea2b1dbbb56599c32295/src/main/java/com/gamesense/client/module/modules/render/Nametags.java#L352
    private static String shortenEnchantName(Enchantment enchantment, int level) {
        ResourceLocation location = Enchantment.REGISTRY.getNameForObject(enchantment);
        String str = location == null ? enchantment.getName() : location.toString();

        int length = (level > 1) ? 12 : 15;
        if (str.length() > length) {
            str = str.substring(10, length);
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1) + (level > 1 ? level : "");
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

    public enum Enchants {
        None, All, Simplified
    }
}
