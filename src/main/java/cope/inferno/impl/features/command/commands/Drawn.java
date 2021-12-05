package cope.inferno.impl.features.command.commands;

import cope.inferno.Inferno;
import cope.inferno.impl.features.command.Command;
import cope.inferno.impl.features.module.Module;

import java.util.List;

@Command.Define(triggers = {"drawn", "shown", "visible"}, description = "Sets a module's drawn state")
public class Drawn extends Command {
    @Override
    public void execute(List<String> args) {
        if (args.isEmpty()) {
            Command.send("Please provide a module name.");
            return;
        }

        Module module = Inferno.moduleManager.getModule(args.get(0));
        if (module == null) {
            Command.send("Could not find module.");
            return;
        }

        boolean state = !module.isDrawn();
        if (args.size() > 1) {
            String possible = args.get(1);
            if (possible.equalsIgnoreCase("true")) {
                state = true;
            } else if (possible.equalsIgnoreCase("false")) {
                state = false;
            }
        }

        module.setDrawn(state);
        Command.send("Module " + module.getName() + "'s drawn state is now " + state + ".");
    }
}
