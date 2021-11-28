package cope.inferno.impl.features.module;

import cope.inferno.impl.event.inferno.ModuleToggledEvent;
import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.settings.Bind;
import cope.inferno.impl.settings.Setting;
import net.minecraftforge.common.MinecraftForge;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;

public class Module implements Wrapper {
    private final String name;
    private final Category category;

    private final String description;

    private final Bind bind = new Bind("Bind", -1);
    private final Setting<Boolean> drawn = new Setting<>("Drawn", true);

    private final ArrayList<Setting> settings = new ArrayList<>();

    private boolean toggled = false;

    public Module() {
        Define definition = this.getClass().getDeclaredAnnotation(Define.class);

        this.name = definition.name();
        this.category = definition.category();

        Info info = this.getClass().getDeclaredAnnotation(Info.class);

        this.description = info == null ? "No description provided." : info.description();
        this.bind.setValue(info == null ? -1 : info.bind());
        this.drawn.setValue(info == null || info.drawn());

        this.settings.add(this.bind);
        this.settings.add(this.drawn);
    }

    public void registerAllSettings() {
        Arrays.stream(this.getClass().getDeclaredFields())
                .filter((field) -> Setting.class.isAssignableFrom(field.getType()))
                .forEach((field) -> {
                    try {
                        field.setAccessible(true);
                        this.settings.add((Setting) field.get(this));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDrawn() {
        return this.drawn.getValue();
    }

    public void setDrawn(boolean drawn) {
        this.drawn.setValue(drawn);
    }

    public int getBind() {
        return this.bind.getValue();
    }

    public void setBind(int bind) {
        this.bind.setValue(bind);
    }

    protected void onActivated() { }
    protected void onDeactivated() { }

    public void toggle() {
        this.toggle(false);
    }

    public void toggle(boolean silent) {
        this.toggled = !this.toggled;

        if (this.toggled) {
            MinecraftForge.EVENT_BUS.register(this);
            this.onActivated();
        } else {
            MinecraftForge.EVENT_BUS.unregister(this);
            this.onDeactivated();
        }

        if (!silent) {
            MinecraftForge.EVENT_BUS.post(new ModuleToggledEvent(this));
        }
    }

    public boolean isOn() {
        return this.toggled;
    }

    public boolean isOff() {
        return !this.toggled;
    }

    public void onUpdate() { }
    public void onTick() { }
    public void onRenderWorld() { }
    public void onRenderHud() { }

    public Setting getSetting(String name) {
        for (Setting setting : this.settings) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }

        return null;
    }

    public ArrayList<Setting> getSettings() {
        return settings;
    }

    public String getDisplayInfo() {
        return null;
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Define {
        String name();
        Category category() default Category.Miscellaneous;
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Info {
        String description() default "No description provided.";
        int bind() default -1;
        boolean drawn() default true;
    }

    public enum Category {
        Combat, Miscellaneous, Movement, Player, Render, Client
    }
}
