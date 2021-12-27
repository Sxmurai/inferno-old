package cope.inferno.core;

import cope.inferno.core.events.ShutdownEvent;
import cope.inferno.core.manager.managers.EventManager;
import cope.inferno.core.manager.managers.ModuleManager;
import cope.inferno.core.manager.managers.relationships.RelationshipManager;
import cope.inferno.util.internal.fs.FileUtil;
import cope.inferno.util.internal.tray.SystemTrayUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Inferno.MODID, name = Inferno.NAME, version = Inferno.VERSION)
public class Inferno {
    public static final String MODID = "inferno";
    public static final String NAME = "Inferno";
    public static final String VERSION = "2.0.0-beta";

    @Mod.Instance
    public static Inferno INSTANCE;

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    // client related shit
    private ModuleManager moduleManager;
    private RelationshipManager relationshipManager;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Forge found the mod Inferno v{}", VERSION);
        FileUtil.mkDir(FileUtil.INFERNO_FOLDER, false); // create the Inferno config folder
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("Initializing client managers");

        moduleManager = new ModuleManager();
        moduleManager.init();
        MinecraftForge.EVENT_BUS.register(moduleManager);

        relationshipManager = new RelationshipManager();

        MinecraftForge.EVENT_BUS.register(new EventManager());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("Doing some last minute things...");

        SystemTrayUtil.createIcon();
        if (SystemTrayUtil.isCreated()) {
            LOGGER.info("Created tray icon on system tray");
        } else {
            LOGGER.info("Could not create system tray icon.");
        }

        LOGGER.info("Initialized Inferno v{}", VERSION);
    }

    @SubscribeEvent
    public void onShutdown(ShutdownEvent event) {
        LOGGER.info("Removing icon from system tray");
        SystemTrayUtil.remove();
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public RelationshipManager getRelationshipManager() {
        return relationshipManager;
    }
}
