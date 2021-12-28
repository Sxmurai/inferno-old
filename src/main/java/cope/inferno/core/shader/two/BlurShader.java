package cope.inferno.core.shader.two;

import cope.inferno.core.shader.Shader2D;
import net.minecraft.util.ResourceLocation;

public class BlurShader extends Shader2D {
    private float intensity = 5.0f;

    public BlurShader() {
        super("blur", new ResourceLocation("inferno:shaders/post/blur.json"));
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    @Override
    public void config() {
        getShaderUniform(0, "Radius").set(intensity);
        getShaderUniform(1, "Radius").set(intensity);
    }
}
