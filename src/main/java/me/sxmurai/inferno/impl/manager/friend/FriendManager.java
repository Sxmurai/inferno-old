package me.sxmurai.inferno.impl.manager.friend;

import java.util.ArrayList;
import java.util.UUID;

public class FriendManager {
    private final ArrayList<Friend> friends = new ArrayList<>();

    public ArrayList<Friend> getFriends() {
        return friends;
    }

    public boolean isFriend(UUID uuid) {
        return this.friends.stream().anyMatch((friend) -> friend.getUuid().equals(uuid));
    }
}
