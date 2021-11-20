package me.sxmurai.inferno.impl.features.module.modules.miscellaneous;

import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "Suffix")
@Module.Info(description = "Adds a nice little suffix at the end of your message")
public class Suffix extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Inferno);
    public final Setting<String> custom = new Setting<>("Custom", "FuckYou.com", () -> this.mode.getValue() == Mode.Custom);
    public final Setting<Separator> separator = new Setting<>("Separator", Separator.Line);
    public final Setting<Boolean> unicode = new Setting<>("Unicode", true);

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketChatMessage) {
            CPacketChatMessage packet = (CPacketChatMessage) event.getPacket();
            if (packet.message.startsWith("/") || packet.message.startsWith(",") || packet.message.startsWith("#")) { // @todo the , is the clients prefix
                return;
            }

            String suffix = this.mode.getValue() == Mode.Custom ?
                    this.toSuffix(this.custom.getValue().toLowerCase()) :
                    this.toSuffix(this.mode.getValue().suffix);

            String separator = this.unicode.getValue() ? this.separator.getValue().unicode : this.separator.getValue().ascii;

            packet.message += (" " + separator + " " + suffix);
        }
    }

    // https://github.com/ChompChompDead/Teddyware-Client/blob/master/src/main/java/com/chompchompdead/teddyware/client/module/client/ChatSuffix.java#L28
    // im not gonna sit here like a jackass and type all of these no thanks
    private String toSuffix(String suffix) {
        if (!this.unicode.getValue()) {
            return suffix;
        }

        return suffix.toLowerCase()
                .replace("a", "\u1d00")
                .replace("b", "\u0299")
                .replace("c", "\u1d04")
                .replace("d", "\u1d05")
                .replace("e", "\u1d07")
                .replace("f", "\ua730")
                .replace("g", "\u0262")
                .replace("h", "\u029c")
                .replace("i", "\u026a")
                .replace("j", "\u1d0a")
                .replace("k", "\u1d0b")
                .replace("l", "\u029f")
                .replace("m", "\u1d0d")
                .replace("n", "\u0274")
                .replace("o", "\u1d0f")
                .replace("p", "\u1d18")
                .replace("q", "\u01eb")
                .replace("r", "\u0280")
                .replace("s", "\ua731")
                .replace("t", "\u1d1b")
                .replace("u", "\u1d1c")
                .replace("v", "\u1d20")
                .replace("w", "\u1d21")
                .replace("x", "\u02e3")
                .replace("y", "\u028f")
                .replace("z", "\u1d22");
    }

    public enum Separator {
        Line("\u23D0", "|"),
        ArrowRight("\u300b", ">>"),
        ArrowLeft("\u300a", "<<"),
        Arrow("\u279C", ">");

        private final String unicode;
        private final String ascii;

        Separator(String unicode, String ascii) {
            this.unicode = unicode;
            this.ascii = ascii;
        }
    }

    public enum Mode {
        Inferno("inferno"),
        Aestheticall("Aestheticall"),
        TooBee("2b2t"),
        Penis("Penis"),
        Custom(null);

        private final String suffix;
        Mode(String suffix) {
            this.suffix = suffix;
        }
    }
}
