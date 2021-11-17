package me.sxmurai.inferno.impl.features.command;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.Wrapper;
import net.minecraft.util.text.TextComponentString;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

public abstract class Command implements Wrapper {
    private final List<String> triggers;
    private final String description;

    public Command() {
        Define define = this.getClass().getDeclaredAnnotation(Define.class);

        this.triggers = Arrays.asList(define.triggers());
        this.description = define.description();
    }

    public abstract void execute(List<String> args);

    public List<String> getTriggers() {
        return triggers;
    }

    public String getDescription() {
        return description;
    }

    public static void send(String text) {
        mc.player.sendMessage(new TextComponentString(Command.getPrefix() + text));
    }

    public static String getPrefix() {
        return ChatFormatting.DARK_GRAY + "[" + ChatFormatting.RED + Inferno.NAME + ChatFormatting.DARK_GRAY + "]" + ChatFormatting.RESET + " ";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Define {
        String[] triggers() default {};
        String description() default "No description provided";
    }
}
