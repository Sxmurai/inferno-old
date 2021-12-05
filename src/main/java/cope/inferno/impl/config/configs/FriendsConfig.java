package cope.inferno.impl.config.configs;

import cope.inferno.Inferno;
import cope.inferno.impl.config.Config;
import cope.inferno.impl.manager.FileManager;
import cope.inferno.impl.manager.friend.Friend;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.util.UUID;

public class FriendsConfig extends Config {
    public FriendsConfig() {
        super("friends", ".json");
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
            Inferno.friendManager.add(new Friend((UUID) json.get("uuid"), json.getString("alias")));
        }
    }

    @Override
    public void save() {
        JSONArray array = new JSONArray();
        for (Friend friend : Inferno.friendManager.getFriends()) {
            array.put(new JSONObject().put("uuid", friend.getUuid()).put("alias", friend.getAlias()));
        }

        FileManager.getInstance().write(this.getPath(), array.toString(4));
    }

    @Override
    public void reset() {
        Inferno.friendManager.getFriends().clear();
    }

    @Override
    public Path getPath() {
        return FileManager.getInstance().getClientFolder().resolve(this.path);
    }
}
