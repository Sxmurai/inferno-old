package cope.inferno.core;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // create frame
        JFrame frame = new JFrame();
        // set the title
        frame.setTitle("Invalid environment");
        // set the size
        frame.setSize(300, 100);

        // simple message, im obviously gonna add more laterr
        frame.add(new Label("Please put this JAR into your mods folder. Thanks!"));

        // fuck you resizing my shit
        frame.setResizable(false);
        // show to user
        frame.setVisible(true);
    }
}
