package cope.inferno.impl.config.configs;

import cope.inferno.Inferno;
import cope.inferno.impl.config.Config;
import cope.inferno.impl.features.hud.HudComponent;
import cope.inferno.impl.manager.FileManager;
import cope.inferno.impl.settings.EnumConverter;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.render.ColorUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;

public class HudConfig extends Config {
    public HudConfig() {
        super("hud_components", ".json");
    }

    @Override
    public void load() {
        String content = this.parse();
        if (content == null || content.isEmpty()) {
            this.save();
            return;
        }

        JSONArray array = new JSONArray(content);
        for (Object object : array) {
            if (!(object instanceof JSONObject)) {
                continue;
            }

            JSONObject json = (JSONObject) object;
            HudComponent component = Inferno.hudManager.getComponent(json.getString("name"));
            if (component == null) {
                return;
            }

            component.setVisible(json.getBoolean("visible"));
            component.setX(json.getDouble("x"));
            component.setY(json.getDouble("y"));

            // snatched right from the modules config lmao
            if (json.get("settings") instanceof JSONObject) {
                JSONObject settings = json.getJSONObject("settings");
                for (String key : settings.keySet()) {
                    Setting setting = component.getSetting(key);
                    if (setting == null) {
                        continue;
                    }

                    Object value = settings.get(key);

                    try {
                        if (setting.getValue() instanceof String) {
                            setting.setValue((String) value);
                        } else if (setting.getValue() instanceof Float) {
                            setting.setValue(settings.getFloat(key));
                        } else if (setting.getValue() instanceof Double) {
                            setting.setValue(settings.getDouble(key));
                        } else if (setting.getValue() instanceof Integer) {
                            setting.setValue(settings.getInt(key));
                        } else if (setting.getValue() instanceof Boolean) {
                            setting.setValue(settings.getBoolean(key));
                        } else if (setting.getValue() instanceof ColorUtil.Color) {
                            // @todo
                        } else if (setting.getValue() instanceof Enum) {
                            setting.setValue(new EnumConverter(((Enum<?>) setting.getValue()).getDeclaringClass()).doBackward((String) value));
                        }
                    } catch (Exception ignored) { }
                }
            }
        }
    }

    @Override
    public void save() {
        JSONArray array = new JSONArray();
        for (HudComponent component : Inferno.hudManager.getComponents()) {
            JSONObject json = new JSONObject()
                    .put("name", component.getName())
                    .put("visible", component.isVisible())
                    .put("x", (double) component.getX())
                    .put("y", (double) component.getY());

            JSONObject settings = new JSONObject();
            for (Setting setting : component.getSettings()) {
                if (setting.getValue() instanceof Enum) {
                    settings.put(setting.getName(), ((Enum<?>) setting.getValue()).name());
                } else if (setting.getValue() instanceof Float) {
                    settings.put(setting.getName(), (float) setting.getValue());
                } else if (setting.getValue() instanceof Double) {
                    settings.put(setting.getName(), (double) setting.getValue());
                } else if (setting.getValue() instanceof Integer) {
                    settings.put(setting.getName(), (int) setting.getValue());
                } else {
                    settings.put(setting.getName(), setting.getValue());
                }
            }

            array.put(json.put("settings", settings));
        }

        FileManager.getInstance().write(this.getPath(), array.toString(4));
    }

    @Override
    public void reset() {
        for (HudComponent component : Inferno.hudManager.getComponents()) {
            if (component.getName().equalsIgnoreCase("arraylist")) {
                component.setVisible(true);
            }

            component.setX(0.0);
            component.setY(0.0);
        }
    }

    @Override
    public Path getPath() {
        return FileManager.getInstance().getClientFolder().resolve(this.path);
    }
}
