package me.sxmurai.inferno.asm.mixins.render;

import me.sxmurai.inferno.impl.features.module.modules.render.Wallhack;
import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.IntBuffer;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder {
    @Redirect(method = "putColorMultiplier", at = @At(value = "INVOKE", target = "java/nio/IntBuffer.put(II)Ljava/nio/IntBuffer;", remap = false))
    private IntBuffer putColorMultiplier(IntBuffer buffer, int i, int j) {
        // https://stackoverflow.com/questions/23316983/how-to-modify-the-alpha-of-an-argb-hex-value-using-integer-values-in-java
        // had to look this up, i am ass at anything to do with math or bitwise operations
        return buffer.put(i, Wallhack.INSTANCE.isOn() ? j & 0x00ffffff | Wallhack.opacity.getValue() << 24 : j);
    }
}