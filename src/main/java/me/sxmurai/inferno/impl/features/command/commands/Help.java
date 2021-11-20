package me.sxmurai.inferno.impl.features.command.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.command.Command;

import java.util.List;

@Command.Define(triggers = {"help", "h", "halp", "commands", "cmds"}, description = "Shows all of the commands or info on one command")
public class Help extends Command {
    @Override
    public void execute(List<String> args) {
        if (args.isEmpty()) {
            this.sendAll(1);
            return;
        }

        Command command = Inferno.commandManager.getCommand(String.join(" ", args));
        if (command == null) {
            int page = 1;
            try {
                page = Integer.parseInt(args.get(0));
            } catch (NumberFormatException ignored) {
            }

            this.sendAll(page);
            return;
        }

        Command.send(new StringBuilder("Help for command")
                .append(" ")
                .append(command.getTriggers().get(0))
                .append(":")
                .append("\n")
                .append(ChatFormatting.GOLD)
                .append("Triggers")
                .append(ChatFormatting.RESET)
                .append(":")
                .append(" ")
                .append(String.join(", ", command.getTriggers()))
                .append("\n")
                .append(ChatFormatting.GOLD)
                .append("Description")
                .append(ChatFormatting.RESET)
                .append(":")
                .append(" ")
                .append(command.getDescription())
                .toString()
        );
    }

    private void sendAll(int page) {
        int maxLength = 5;
        int maxPages = (int) Math.ceil(Inferno.commandManager.getCommands().size() / (double) maxLength);

        if (page > maxPages || page <= 0) {
            page = 1;
        }

        List<Command> commands = Inferno.commandManager.getCommands().subList((page - 1) * maxLength, Math.min(page * maxLength, Inferno.commandManager.getCommands().size()));
        if (!commands.isEmpty()) {
            StringBuilder builder = new StringBuilder("Commands")
                    .append(" ")
                    .append(" - ")
                    .append(" ")
                    .append("Page")
                    .append(" ")
                    .append(page)
                    .append("/")
                    .append(maxPages)
                    .append("\n");

            for (Command command : commands) {
                builder.append(ChatFormatting.GOLD).append(command.getTriggers().get(0)).append(ChatFormatting.RESET).append(" ");
            }

            Command.send(builder.toString());
        }
    }
}
