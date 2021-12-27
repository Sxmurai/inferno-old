package cope.inferno.util.internal;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;

public class ChatUtil implements Wrapper {
    public static final String PREFIX = ChatFormatting.RED + "[Inferno]" + ChatFormatting.RESET + " ";

    /**
     * Sends a non-prefixed message
     * @param message The message to send
     */
    public static void send(String message) {
        mc.player.sendMessage(new TextComponentString(message));
    }

    /**
     * Sendes a prefixed message
     * @param message The message to send
     */
    public static void sendPrefixed(String message) {
        mc.player.sendMessage(new TextComponentString(PREFIX + message));
    }

    /**
     * Sends a persistent message which can be edited or delete via the id
     * @param id The id
     * @param message The message
     */
    public static void sendPersistent(int id, String message) {
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(PREFIX + message), id);
    }
}
