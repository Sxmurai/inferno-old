package cope.inferno.util.internal.tray;

import cope.inferno.core.Inferno;
import cope.inferno.util.internal.fs.ResourceUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Manages the Inferno system tray icon
 */
public class SystemTrayUtil {
    /**
     * Image location for icon
     */
    private static final String ICON_LOCATION = "/assets/inferno/textures/logo_16px.png";

    /**
     * Tray icon instance. Used to post messages
     */
    private static TrayIcon trayIcon = null;

    /**
     * Creates the tray icon if the system supports it
     */
    public static void createIcon() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            BufferedImage image = ResourceUtil.getResourceAsImage(ICON_LOCATION);
            if (image == null) {
                Inferno.LOGGER.error("Could not find image at location {}", ICON_LOCATION);
                return;
            }

            trayIcon = new TrayIcon(image, "Inferno");
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                Inferno.LOGGER.error("System does not support tray icons.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Removes the icon from the system tray
     */
    public static void remove() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }

    /**
     * Shows a message coming from the tray icon
     * @param message The message you'd like to show
     */
    public static void showMessage(String message) {
        if (SystemTray.isSupported() && trayIcon != null) {
            trayIcon.displayMessage("", message, TrayIcon.MessageType.NONE);
        }
    }

    /**
     * If the treyIcon field is null or not
     * @return false if the tray icon wasn't successfully created, or true if it was created
     */
    public static boolean isCreated() {
        return trayIcon != null;
    }

    /**
     * Gets the tray icon object
     * @return the TrayIcon object if it exists
     */
    public static TrayIcon getTrayIcon() {
        return trayIcon;
    }
}
