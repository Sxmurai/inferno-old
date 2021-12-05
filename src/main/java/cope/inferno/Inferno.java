package cope.inferno;

import cope.inferno.impl.manager.*;
import cope.inferno.impl.manager.friend.FriendManager;
import cope.inferno.impl.manager.notification.NotificationManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

@Mod(name = Inferno.NAME, modid = Inferno.ID, version = Inferno.VERSION)
public class Inferno {
    public static final String NAME = "Inferno";
    public static final String ID = "inferno";
    public static final String VERSION = "1.1.0-beta";

    @Mod.Instance
    public static Inferno INSTANCE;
    public static Logger LOGGER = LogManager.getLogger(Inferno.class);

    public static FontManager fontManager;
    public static ConfigManager configManager;
    public static ModuleManager moduleManager;
    public static CommandManager commandManager;
    public static NotificationManager notificationManager;
    public static RotationManager rotationManager;
    public static TotemPopManager totemPopManager;
    public static ServerManager serverManager;
    public static HoleManager holeManager;
    public static FriendManager friendManager;
    public static HudManager hudManager;
    public static InteractionManager interactionManager;
    public static InventoryManager inventoryManager;
    public static TickManager tickManager;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        if (!FileManager.getInstance().exists(FileManager.getInstance().getClientFolder())) {
            LOGGER.info("Creating Inferno folder as it did not exist...");
            FileManager.getInstance().makeDirectory(FileManager.getInstance().getClientFolder());
        }

        LOGGER.info("get out of my logs cunt - aesthetical");

        configManager = new ConfigManager();
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        LOGGER.info("Initializing {} {}", Inferno.NAME, Inferno.VERSION);

        Display.setTitle(Inferno.NAME + " " + Inferno.VERSION);

        fontManager = new FontManager();
        fontManager.resetCustomFont();
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();
        notificationManager = new NotificationManager();
        rotationManager = new RotationManager();
        totemPopManager = new TotemPopManager();
        serverManager = new ServerManager();
        holeManager = new HoleManager();
        friendManager = new FriendManager();
        hudManager = new HudManager();
        interactionManager = new InteractionManager();
        inventoryManager = new InventoryManager();
        tickManager = new TickManager();

        MinecraftForge.EVENT_BUS.register(new EventManager());
        MinecraftForge.EVENT_BUS.register(moduleManager);
        MinecraftForge.EVENT_BUS.register(commandManager);
        MinecraftForge.EVENT_BUS.register(notificationManager);
        MinecraftForge.EVENT_BUS.register(configManager);
        MinecraftForge.EVENT_BUS.register(rotationManager);
        MinecraftForge.EVENT_BUS.register(totemPopManager);
        MinecraftForge.EVENT_BUS.register(serverManager);

        LOGGER.info("Initialized {} {}. Welcome!", Inferno.NAME, Inferno.VERSION);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        configManager.load();
    }
}
