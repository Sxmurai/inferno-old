package me.sxmurai.inferno.impl.manager.friend;

import java.util.ArrayList;
import java.util.UUID;

public class FriendManager {
    private final ArrayList<Friend> friends = new ArrayList<>();

    public ArrayList<Friend> getFriends() {
        return friends;
    }

    public void add(Friend friend) {
        this.friends.add(friend);
    }

    public void remove(Friend friend) {
        this.friends.remove(friend);
    }

    public Friend getFriend(UUID uuid) {
        return this.friends.stream().filter((friend) -> friend.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public boolean isFriend(UUID uuid) {
        return this.friends.stream().anyMatch((friend) -> friend.getUuid().equals(uuid));
    }
}
