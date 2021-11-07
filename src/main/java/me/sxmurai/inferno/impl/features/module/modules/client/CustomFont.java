package me.sxmurai.inferno.impl.features.module.modules.client;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.event.inferno.OptionChangeEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.option.Option;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

@Module.Define(name = "CustomFont", category = Module.Category.Client)
@Module.Info(description = "Manages the custom font for the client")
public class CustomFont extends Module {
    public static CustomFont INSTANCE;

    public static Option<String> font = new Option<>("Font", "Verdana");
    public static Option<Style> style = new Option<>("Style", Style.Plain);
    public static Option<Boolean> shadow = new Option<>("Shadow", true);
    public static Option<Integer> size = new Option<>("Size", 18, 6, 26);
    public static Option<Boolean> antiAlias = new Option<>("AntiAlias", true);
    public static Option<Boolean> fractionalMetrics = new Option<>("FractionalMetrics", true);
    public static Option<Boolean> override = new Option<>("Override", false);

    public CustomFont() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onOptionChange(OptionChangeEvent event) {
        if (this.getOptions().stream().anyMatch((o) -> event.getOption().equals(o))) {
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