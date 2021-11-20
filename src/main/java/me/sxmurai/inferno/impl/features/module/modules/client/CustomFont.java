package me.sxmurai.inferno.impl.features.module.modules.client;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.event.inferno.OptionChangeEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

@Module.Define(name = "CustomFont", category = Module.Category.Client)
@Module.Info(description = "Manages the custom font for the client")
public class CustomFont extends Module {
    public static CustomFont INSTANCE;

    public static Setting<String> font = new Setting<>("Font", "Verdana");
    public static Setting<Style> style = new Setting<>("Style", Style.Plain);
    public static Setting<Boolean> shadow = new Setting<>("Shadow", true);
    public static Setting<Integer> size = new Setting<>("Size", 18, 6, 26);
    public static Setting<Boolean> antiAlias = new Setting<>("AntiAlias", true);
    public static Setting<Boolean> fractionalMetrics = new Setting<>("FractionalMetrics", true);
    public static Setting<Boolean> override = new Setting<>("Override", false);

    public CustomFont() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onOptionChange(OptionChangeEvent event) {
        if (this.getSettings().stream().anyMatch((o) -> event.getOption().equals(o))) {
            Inferno.fontManager.resetCustomFont();
        }
    }

    public enum Style {
        Plain(Font.PLAIN),
        Bold(Font.BOLD),
        Italic(Font.ITALIC),
        BoldedItalic(Font.BOLD + Font.ITALIC);

        private final int style;
        Style(int style) {
            this.style = style;
        }

        public int getStyle() {
            return style;
        }
    }
}