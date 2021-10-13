package me.sxmurai.inferno.client.features.modules.render;

import me.sxmurai.inferno.api.utils.EntityUtils;
import me.sxmurai.inferno.api.utils.RenderUtils;
import me.sxmurai.inferno.api.values.Value;
import me.sxmurai.inferno.client.Inferno;
import me.sxmurai.inferno.client.manager.managers.commands.text.ChatColor;
import me.sxmurai.inferno.client.manager.managers.friends.Friend;
import me.sxmurai.inferno.client.manager.managers.modules.Module;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

@Module.Define(name = "Nametags", description = "Renders custom nametags rather than the vanilla ones", category = Module.Category.RENDER)
public class Nametags extends Module {
    private static final Random RNG = new Random();
    public static Nametags INSTANCE;

    public final Value<Boolean> invisible = new Value<>("Invisible", true);
    public final Value<Boolean> rectangle = new Value<>("Rectangle", true);
    public final Value<Float> opacity = new Value<>("Opacity", 0.5f, 0.0f, 1.0f, (v) -> rectangle.getValue());
    public final Value<Float> scaling = new Value<>("Scaling", 0.3f, 0.1f, 3.0f);
    public final Value<Boolean> nameProtect = new Value<>("NameProtect", false);
    public final Value<Boolean> onlyFriends = new Value<>("OnlyFriends", false, (v) -> nameProtect.getValue());
    public final Value<Boolean> health = new Value<>("Health", true);
    public final Value<Boolean> healthColors = new Value<>("HealthColors", true, (v) -> health.getValue());
    public final Value<Boolean> ping = new Value<>("Ping", false);
    public final Value<Boolean> armor = new Value<>("Armor", true);
    public final Value<Boolean> enchants = new Value<>("Enchants", false, (v) -> armor.getValue());
    public final Value<Boolean> reversed = new Value<>("Reversed", false, (v) -> armor.getValue());
    public final Value<Boolean> mainHand = new Value<>("MainHand", true);
    public final Value<Boolean> offhand = new Value<>("Offhand", true);

    public Nametags() {
        INSTANCE = this;
    }

    public void renderNametags(EntityPlayer player, double x, double y, double z) {
        if (player == null || player == mc.player || (!this.invisible.getValue() && EntityUtils.isInvisible(player))) {
            return;
        }

        double yOffset = y + (player.isSneaking() ? 0.5 : 0.7);

        RenderManager manager = mc.renderManager;

        double dist = mc.renderViewEntity.getDistance(x + manager.viewerPosX, y + manager.viewerPosY, z + manager.viewerPosZ);
        double scale = (this.scaling.getValue() * dist) / 50.0;

        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        GlStateManager.disableLighting();
        GlStateManager.translate(x, yOffset + 1.4, z);
        GlStateManager.rotate(-manager.playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(manager.playerViewX, mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        String name = "";
        if (this.ping.getValue()) {
            name += (Inferno.serverManager.getLatency(player.entityUniqueID) + "ms ");
        }

        if (this.nameProtect.getValue()) {
            Friend friend = Inferno.friendManager.getFriend(player);
            if (this.onlyFriends.getValue()) {
                if (friend != null) {
                    name += friend.getAlias() == null ? "Friend" + friend.hashCode() : friend.getAlias();
                } else {
                    name += player.getName();
                }
            } else {
                name += "Player" + RNG.nextInt(1000);
            }
        } else {
            name += player.getName();
        }

        name += " ";
        if (this.health.getValue()) {
            float health = EntityUtils.getHealth(player);
            name += (this.healthColors.getValue() ? this.getHealthColor(health) : "") + health + ChatColor.Reset;
        }

        float width = Inferno.textManager.getWidth(name) / 2.0f;

        if (this.rectangle.getValue()) {
            RenderUtils.drawRect(-width - 2.0f, -(Inferno.textManager.getHeight() + 1.0f), (width * 2.0f) + 2.0f, Inferno.textManager.getHeight() + 2.0f, 0.0f, 0.0f, 0.0f, this.opacity.getValue());
        }

        Inferno.textManager.drawRegularString(name, -width, -(Inferno.textManager.getHeight() - 1.0f), -1);

        int xOffset = 0;
        xOffset = xOffset - 16 / 2 * player.inventory.armorInventory.size();
        xOffset = xOffset - 16 / 2;
        xOffset = xOffset - 16 / 2;

        if (this.mainHand.getValue()) {
            if (!player.getHeldItemMainhand().isEmpty) {
                this.renderItemStack(player.getHeldItemMainhand(), xOffset, -26);
            }
        }

        if (this.armor.getValue()) {
            ArrayList<ItemStack> armorPieces = new ArrayList<>(player.inventory.armorInventory);
            if (this.reversed.getValue()) {
                Collections.reverse(armorPieces);
            }

            xOffset += 16;
            for (ItemStack stack : armorPieces) {
                if (!stack.isEmpty()) {
                    this.renderItemStack(stack, xOffset, -26);
                }

                xOffset += 16;
            }
        }

        if (this.offhand.getValue()) {
            if (!this.armor.getValue()) {
                xOffset += 16;
            }

            if (!player.getHeldItemOffhand().isEmpty) {
                GlStateManager.pushMatrix();
                this.renderItemStack(player.getHeldItemOffhand(), xOffset, -26);
                GlStateManager.popMatrix();
            }
        }

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();
    }

    private void renderItemStack(ItemStack stack, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        // GlStateManager.clear(256);
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

    private String getHealthColor(float health) {
        if (health >= 20.0f) {
            return ChatColor.Green.toString();
        } else if (health < 16.0f) {
            return ChatColor.Yellow.toString();
        } else {
            return ChatColor.Red.toString();
        }
    }
}
