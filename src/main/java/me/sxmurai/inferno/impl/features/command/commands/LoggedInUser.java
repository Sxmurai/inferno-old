package me.sxmurai.inferno.impl.features.command.commands;

import me.sxmurai.inferno.impl.features.command.Command;

import java.util.List;

@Command.Define(triggers = {"loggedinuser", "username"}, description = "Tells you the user you are currently logged into, dumbass")
public class LoggedInUser extends Command {
    @Override
    public void execute(List<String> args) {
        if (mc.player != null) {
            Command.send("You are logged in as " + mc.player.getName());
        } else {
            Command.send("wtf");
        }
    }
}
