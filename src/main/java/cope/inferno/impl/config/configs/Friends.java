package cope.inferno.impl.config.configs;

import cope.inferno.impl.config.Config;
import cope.inferno.impl.manager.friend.Friend;
import cope.inferno.Inferno;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

@Config.Define(value = "friends", paths = {"friends.json"})
public class Friends extends Config {
    @Override
    public void save() {
        JSONArray array = new JSONArray();
        for (Friend friend : Inferno.friendManager.getFriends()) {
            array.put(new JSONObject()
                .put("uuid", friend.getUuid())
                .put("alias", friend.getAlias())
            );
        }

        this.fileManager.write(this.path, array.toString(4));
    }

    @Override
    public void load() {
        String text = this.fileManager.read(this.path);
        if (text == null || text.isEmpty()) {
            this.save();
            return;
        }

        JSONArray array = new JSONArray(text);
        for (Object value : array) {
            if (!(value instanceof JSONObject)) {
                continue;
            }

            JSONObject json = (JSONObject) value;
            Inferno.friendManager.add(new Friend((UUID) json.get("uuid"), json.getString("alias")));
        }
    }
}
