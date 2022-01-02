package cope.inferno.asm.impl.network;

import cope.inferno.core.features.module.other.AntiBookBan;
import net.minecraft.network.NettyCompressionDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

// credit goes to https://github.com/yoink-inc/konas/blob/main/build/sources/main/java/me/darki/konas/mixin/mixins/MixinNettyCompressionDecoder.java
// iirc Phobos oldbase had this, but oh well. turns out Konas also skidded from Phobos oldbase, so uh
@Mixin(NettyCompressionDecoder.class)
public class MixinNettyCompressionDecoder {
    @ModifyConstant(method = "decode", constant = @Constant(intValue = 0x200000))
    public int decode(int in) {
        return AntiBookBan.INSTANCE.isToggled() && AntiBookBan.packet.getValue() ? Integer.MAX_VALUE : in;
    }
}
