package me.sxmurai.inferno.impl.features.command.commands;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.command.Command;
import me.sxmurai.inferno.impl.features.module.modules.client.CustomFont;

import java.awt.*;
import java.util.List;

@Command.Define(triggers = {"font", "f", "customfont"}, description = "Manages the custom font")
public class Font extends Command {
    @Override
    public void execute(List<String> args) {
        if (args.isEmpty()) {
            Command.send("The current font is " + CustomFont.font.getValue() + ".");
            return;
        }

        switch (args.get(0).toLowerCase()) {
            case "size": {
                if (args.size() == 1) {
                    Command.send("Expected an integer.");
                    return;
                }

                int size = -1;
                try {
                    size = Integer.parseInt(args.get(1));
                } catch (NumberFormatException e) {
                    Command.send("Provided an invalid integer.");
                    return;
                }

                if (size == -1) {
                    Command.send("wtf");
                    return;
                }

                if (size < 6 || size > 26) {
                    Command.send("Must provide an integer between 6 and 26.");
                    return;
                }

                CustomFont.size.setValue(size);
                Command.send("Set the font size to " + size + ".");
                break;
            }

            case "reload": {
                Inferno.fontManager.resetCustomFont();
                Command.send("Reloaded custom font renderer.");
                break;
            }

            default: {
                for (java.awt.Font font : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
                    if (font.getName().toLowerCase().startsWith(String.join(" ", args).toLowerCase())) {
                        CustomFont.font.setValue(font.getName());
                        Command.send("Set new font to " + font.getName() + ".");
                        return;
                    }
                }

                Command.send("Expected 'size', 'reload', or a font name.");
                break;
            }
        }

        if (!CustomFont.INSTANCE.isOn()) {
            Inferno.fontManager.resetCustomFont();
        }
    }
}
