package cope.inferno.impl.config.configs;

import cope.inferno.Inferno;
import cope.inferno.impl.config.Config;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.manager.FileManager;
import cope.inferno.impl.settings.EnumConverter;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.render.ColorUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lwjgl.input.Keyboard;

public class ModulesConfig extends Config {
    public ModulesConfig() {
        super("modules", ".json");
    }

    @Override
    public void load() {
        String content = this.parse();
        if (content == null || content.isEmpty()) {
            this.save();
            return;
        }

        JSONArray array = new JSONArray(content);
        for (Object object : array)  {
            if (!(object instanceof JSONObject)) {
                continue;
            }

            JSONObject json = (JSONObject) object;

            Module module = Inferno.moduleManager.getModule(json.getString("name"));
            if (module == null) {
                continue;
            }

            if (module.isOn() != json.getBoolean("toggled")) {
                module.toggle(true);
            }

            if (json.get("settings") instanceof JSONObject) {
                JSONObject settings = json.getJSONObject("settings");
                for (String key : settings.keySet()) {
                    Setting setting = module.getSetting(key);
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
                    } catch (Exception ignored) { } // FUCK YOU FUCK YOU FUCK YOU I HATE STUPID ASS NUMBER TYPES NFDJHFSDJHFJF
                }
            }
        }
    }

    @Override
    public void save() {
        JSONArray array = new JSONArray();
        for (Module module : Inferno.moduleManager.getModules()) {
            JSONObject object = new JSONObject().put("name", module.getName()).put("toggled", module.isOn());

            JSONObject settings = new JSONObject();
            for (Setting setting : module.getSettings()) {
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

            object.put("settings", settings);
            array.put(object);
        }

        FileManager.getInstance().write(this.getPath(), array.toString(4));
    }

    @Override
    public void reset() {
        for (Module module : Inferno.moduleManager.getModules()) {
            if (module.isOn()) {
                module.toggle();
            }

            module.getSettings().forEach((setting) -> {
                if (setting.getName().equalsIgnoreCase("bind") && module.getName().equalsIgnoreCase("GUI")) {
                    setting.setValue(Keyboard.KEY_R);
                } else {
                    setting.setValue(setting.getDefaultValue());
                }
            });
        }
    }
}
