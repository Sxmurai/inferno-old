package me.sxmurai.inferno.impl.manager.notification;

import me.sxmurai.inferno.impl.features.Wrapper;
import me.sxmurai.inferno.impl.features.command.Command;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Optional;

public class NotificationManager implements Wrapper {
    private final ArrayList<Notification> notifications = new ArrayList<>();

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        for (int i = 0; i < this.notifications.size(); ++i) {
            Notification notification = this.notifications.get(i);
            if (notification.shouldDelete()) {
                mc.ingameGUI.getChatGUI().deleteChatLine(notification.getId());
                this.notifications.remove(notification);
            }
        }
    }

    public void notifyNoEdit(String text) {
        Command.send(text);
    }

    public void notify(int id, String text) {
        Notification notification;
        Optional<Notification> n =  this.notifications.stream().filter((note) -> note.getId() != id).findFirst();
        notification = n.orElseGet(() -> new Notification(id, text));

        this.notifications.add(notification);
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(Command.getPrefix() + text), id);
    }

    public ArrayList<Notification> getNotifications() {
        return notifications;
    }
}
