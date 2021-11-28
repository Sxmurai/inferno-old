package cope.inferno.impl.config.configs;

import cope.inferno.impl.features.module.Module;
import cope.inferno.Inferno;
import cope.inferno.util.render.ColorUtil;
import cope.inferno.impl.config.Config;
import cope.inferno.impl.settings.EnumConverter;
import cope.inferno.impl.settings.Setting;
import org.json.JSONArray;
import org.json.JSONObject;

@Config.Define(value = "modules", paths = {"configs", "modules.json"})
public class Modules extends Config {
    @Override
    public void save() {
        JSONArray json = new JSONArray();
        for (Module module : Inferno.moduleManager.getModules()) {
            JSONObject options = new JSONObject();
            for (Setting setting : module.getSettings()) {
                if (setting.getValue() instanceof ColorUtil.Color) {
                    ColorUtil.Color color = (ColorUtil.Color) setting.getValue();
                    options.put(module.getName(), new JSONObject()
                            .put("r", color.getRed())
                            .put("g", color.getGreen())
                            .put("b", color.getBlue())
                            .put("a", color.getAlpha())
                    );
                } else {
                    options.put(setting.getName(), setting.getValue());
                }
            }

            json.put(new JSONObject().put("name", module.getName()).put("toggled", module.isOn()).put("options", options));
        }

        this.fileManager.write(this.path, json.toString(4));
    }

    @Override
    public void load() {
        String text = this.fileManager.read(this.path);
        if (text == null || text.isEmpty()) {
            this.save();
            return;
        }

        JSONArray json = new JSONArray(text);
        for (Object obj : json) {
            if (!(obj instanceof JSONObject)) {
                continue;
            }

            JSONObject object = (JSONObject) obj;
            Module module = Inferno.moduleManager.getModule(object.getString("name"));
            if (module == null) {
                Inferno.LOGGER.debug("Couldn't parse module with name {}", object.getString("name"));
                continue;
            }

            if (object.getBoolean("toggled")) {
                module.toggle(true);
            }

            JSONObject options = object.getJSONObject("options");
            for (String key : options.keySet()) {
                Setting opt = module.getSetting(key);
                if (opt == null) {
                    Inferno.LOGGER.debug("Module {} did not have settings {} present.", module.getName(), key);
                    continue;
                }

                if (opt.getValue() instanceof Enum) {
                    opt.setValue(new EnumConverter((Class<? extends Enum>) opt.getValue().getClass()).doBackward(options.getString(key)));
                } else if (opt.getValue() instanceof Integer) {
                    opt.setValue(options.getInt(key));
                } else if (opt.getValue() instanceof Float) {
                    opt.setValue(options.getFloat(key));
                } else if (opt.getValue() instanceof Double) {
                    opt.setValue(options.getDouble(key));
                } else {
                    opt.setValue(options.get(key));
                }
            }
        }
    }
}
