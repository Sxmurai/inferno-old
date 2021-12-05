package cope.inferno.impl.features.command.commands;

import cope.inferno.Inferno;
import cope.inferno.impl.features.command.Command;
import cope.inferno.impl.manager.FileManager;

import java.util.List;

@Command.Define(triggers = {"profile", "prof", "config"}, description = "Manages config profiles")
public class Profile extends Command {
    @Override
    public void execute(List<String> args) {
        if (args.isEmpty()) {
            Command.send("Current profile is " + Inferno.configManager.getCurrentProfile() + ".");
            return;
        }

        String arg = args.get(0).toLowerCase();
        switch (arg) {
            case "list": {
                Command.send("The current loaded profiles are: " + String.join(", ", Inferno.configManager.getProfiles()) + ".");
                break;
            }

            case "create": {
                if (args.size() <= 1) {
                    Command.send("Please provide a profile name.");
                    return;
                }

                String name = args.get(1).toLowerCase();
                if (Inferno.configManager.getProfiles().contains(name)) {
                    Command.send("That profile already exists.");
                    return;
                }

                FileManager.getInstance().makeDirectory(FileManager.getInstance().getClientFolder().resolve("profiles").resolve(name));
                Inferno.configManager.getProfiles().add(name);

                Command.send("Created a new profile with the name " + name + ".");
                break;
            }

            case "delete": {
                if (args.size() <= 1) {
                    Command.send("Please provide a profile name.");
                    return;
                }

                String name = args.get(1).toLowerCase();
                if (!Inferno.configManager.getProfiles().contains(name)) {
                    Command.send("That profile does not exist.");
                    return;
                }

                if (name.equalsIgnoreCase("default")) {
                    Command.send("You cannot delete the default config.");
                    return;
                }

                FileManager.getInstance().delete(FileManager.getInstance().getClientFolder().resolve("profiles").resolve(name));
                Inferno.configManager.getProfiles().remove(name);
                Inferno.configManager.setCurrentProfile("default");
                Inferno.configManager.load();

                Command.send("Deleted profile " + name + " and set the profile to default.");
                break;
            }

            case "reload": {
                Inferno.configManager.load();
                Command.send("Reloaded current profile.");
                break;
            }

            case "load": {
                if (args.size() <= 1) {
                    Command.send("Please provide a profile name.");
                    return;
                }

                String name = args.get(1).toLowerCase();
                if (!Inferno.configManager.getProfiles().contains(name)) {
                    Command.send("That profile does not exist.");
                }

                if (Inferno.configManager.getCurrentProfile().equalsIgnoreCase(name)) {
                    Command.send("That is already the currently loaded profile.");
                    return;
                }

                Inferno.configManager.save(); // save the current config
                Inferno.configManager.reset();

                Inferno.configManager.setCurrentProfile(name);
                Command.send("Set the config profile to " + name + ". Loading into the profile now.");

                Inferno.configManager.load();
                break;
            }

            case "save": {
                Inferno.configManager.save();
                Command.send("Synced the current profile!");
                break;
            }

            default: {
                Command.send("Invalid option. Valid options: list, create, delete, reload, load, save");
                break;
            }
        }
    }
}
