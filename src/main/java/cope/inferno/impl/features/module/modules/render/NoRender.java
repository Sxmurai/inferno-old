package cope.inferno.impl.features.module.modules.render;

import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;

@Module.Define(name = "NoRender", category = Module.Category.Render)
@Module.Info(description = "Stops things from rendering")
public class NoRender extends Module {
    public static NoRender INSTANCE;

    public static Setting<Boolean> hurtcam = new Setting<>("Hurtcam", false);
    public static Setting<Boolean> fire = new Setting<>("Fire", false);
    public static Setting<Boolean> particles = new Setting<>("Particles", false);
    public static Setting<Boolean> totems = new Setting<>("Totems", false);
    public static Setting<Boolean> blocks = new Setting<>("Blocks", false);
    public static Setting<Boolean> weather = new Setting<>("Weather", false);
    public static Setting<Boolean> fog = new Setting<>("Fog", false);
    public static Setting<Boolean> pumpkin = new Setting<>("Pumpkin", false);
    public static Setting<Boolean> potions = new Setting<>("Potions", false);
    public static Setting<Boolean> scoreboard = new Setting<>("Scoreboard", false);
    public static Setting<Boolean> advancements = new Setting<>("Advancements", false);
    public static Setting<Boolean> xp = new Setting<>("XP", false);
    public static Setting<Boolean> portals = new Setting<>("Portals", false);
    public static Setting<Boolean> fov = new Setting<>("FOV", false);
    public static Setting<Armor> armor = new Setting<>("Armor", Armor.None);
    public static Setting<Bossbar> bossbar = new Setting<>("Bossbar", Bossbar.None);

    public NoRender() {
        INSTANCE = this;
    }

    public enum Armor {
        None, Glint, Remove
    }

    public enum Bossbar {
        None, Remove, Stack
    }
}
