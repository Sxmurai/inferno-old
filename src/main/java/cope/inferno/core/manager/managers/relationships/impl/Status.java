package cope.inferno.core.manager.managers.relationships.impl;

import com.mojang.realmsclient.gui.ChatFormatting;

public enum Status {
    NEUTRAL(ChatFormatting.RESET),
    FRIEND(ChatFormatting.AQUA),
    ENEMY(ChatFormatting.RED);

    private final String color;

    Status(ChatFormatting color) {
        this.color = color.toString();
    }

    public String getColor() {
        return color;
    }
}
