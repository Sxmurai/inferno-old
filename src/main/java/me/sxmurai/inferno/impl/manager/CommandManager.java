package me.sxmurai.inferno.impl.manager;

import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.features.command.Command;
import me.sxmurai.inferno.impl.features.command.commands.Font;
import me.sxmurai.inferno.impl.features.command.commands.Help;
import me.sxmurai.inferno.impl.features.command.commands.LoggedInUser;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {
    private final ArrayList<Command> commands = new ArrayList<>();
    private String prefix = ",";

    public CommandManager() {
        this.commands.add(new Font());
        this.commands.add(new Help());
        this.commands.add(new LoggedInUser());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketChatMessage) {
            CPacketChatMessage packet = event.getPacket();
            if (packet.message.startsWith(prefix)) {
                event.setCanceled(true);
                this.handle(packet.message);
            }
        }
    }

    private void handle(String message) {
        if (message == null || message.isEmpty() || !message.startsWith(prefix)) {
            return;
        }

        List<String> args = Arrays.asList(message.substring(this.prefix.length()).split(" "));
        if (args.isEmpty()) {
            return;
        }

        for (Command command : this.commands) {
            if (command.getTriggers().stream().anyMatch((trigger) -> trigger.equalsIgnoreCase(args.get(0)))) {
                try {
                    command.execute(args.subList(1, args.size()));
                } catch (Exception e) {
                    e.printStackTrace();
                    Command.send("An exception occurred.");
                }

                return;
            }
        }

        Command.send("That was an invalid command. Please run the help command.");
    }

    public <T extends Command> T getCommand(String name) {
        for (Command command : this.commands) {
            if (command.getTriggers().stream().anyMatch((trigger) -> trigger.equalsIgnoreCase(name))) {
                return (T) command;
            }
        }

        return null;
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
