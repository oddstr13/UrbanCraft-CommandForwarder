package no.urbancraft.mod.website.commandforwarder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;

import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = CommandForwarder.ID, name = CommandForwarder.ID, version = CommandForwarder.VERSION)
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class CommandForwarder {
	public static final String ID = "UrbanCraft CommandForwarder";
	public static final String VERSION = "@VERSION@";

	// public String post_url;
	public String identifier;
	public boolean debug;
	public List<Command> commands = new ArrayList<Command>();

	// The instance of your mod that Forge uses.
	@Instance(ID)
	public static CommandForwarder instance;

	public static Logger logger;
	
	public static LinkedBlockingQueue msgqueue = new LinkedBlockingQueue();

	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		logger = Logger.getLogger(ID);
		logger.setParent(FMLLog.getLogger());

		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		// post_url = config.get(config.CATEGORY_GENERAL, "post_url",
		// "http://localhost/post/",
		// "This is the url of which the mod posts updates to.").value;
		identifier = config.get(config.CATEGORY_GENERAL, "identifier", "commandforwarder", "This string determines the value of the id field in the post request.").value;
		debug = config.get(Configuration.CATEGORY_GENERAL, "debug", false, "Enable debuging?").getBoolean(true);

		ConfigCategory cmdcat = config.getCategory("commands");
		cmdcat.setComment("This is a list of command=url.");
		Map<String, Property> cmdmap = cmdcat.getValues();

		if (cmdmap.isEmpty()) {
			config.get("commands", "example", "http://localhost/post/");
		}
		for (Map.Entry i : cmdmap.entrySet()) {
			String k = (String) i.getKey();
			Property v = (Property) i.getValue();
			Command cmd = new Command(k, v.value);
			this.commands.add(cmd);
		}
		// public Map<String,Property> getValues()

		config.save();

		logger.info("debug: " + debug);
		logger.info("identifier: " + identifier);
		// logger.info("post_url: " + post_url);
	}

	@Init
	public void load(FMLInitializationEvent event) {
		// GameRegistry.registerPlayerTracker(new PlayerTracker());
		TickRegistry.registerTickHandler(new TickHandler(), Side.SERVER);
		// MinecraftForge.EVENT_BUS.register(new EventHandler());
		logger.info("Loaded.");
	}

	@PostInit
	public void postInit(FMLPostInitializationEvent event) {
		// Stub Method
	}

	@ServerStarting
	public void serverStarting(FMLServerStartingEvent e) {
		for (Command cmd : commands) {
			e.registerServerCommand(cmd);
			logger.info("Command " + cmd.getCommandName() + " registred.");
		}
	}
}
