package me.runescapejon.enderegg;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.complex.EnderDragon;
import org.spongepowered.api.entity.living.complex.dragon.phase.EnderDragonPhaseTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;
import com.flowpowered.math.vector.Vector3d;

@Plugin(id = "endereggdropper", name = "EnderEggDropper", authors = {
		"runescapejon" }, description = "Place a dragon egg on top of the portal or have the dragon drop the once it's killed", version = "1.1")
public class EnderEgg {
	public static EnderEgg instance;
	private EnderEgg plugin;
	private Logger logger;
	private Config configmsg;
	GuiceObjectMapperFactory factory;
	private final File configDirectory;

	@Inject
	public EnderEgg(Logger logger, @ConfigDir(sharedRoot = false) File configDir, GuiceObjectMapperFactory factory) {
		this.logger = logger;

		this.configDirectory = configDir;
		this.factory = factory;
		instance = this;
	}

	@Listener
	public void onPreInitializationEvent(GamePreInitializationEvent event) {
		plugin = this;
		loadConfig();
	}

	@Listener
	public void onGamePreInitialization(GamePreInitializationEvent event) {

	}

	public Logger getLogger() {
		return logger;
	}

	public GuiceObjectMapperFactory getFactory() {
		return factory;
	}

	public Config getCfg() {
		return configmsg;
	}

	@Listener
	public void onReload(GameReloadEvent event) {
		loadConfig();
	}

	public boolean loadConfig() {
		if (!plugin.getConfigDirectory().exists()) {
			plugin.getConfigDirectory().mkdirs();
		}
		try {
			File configFile = new File(getConfigDirectory(), "Config.conf");
			if (!configFile.exists()) {
				configFile.createNewFile();
				logger.info("Creating Config for EnderEggDropper");
			}
			ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
					.setFile(configFile).build();
			CommentedConfigurationNode config = loader.load(ConfigurationOptions.defaults().setHeader(HeaderMessage)
					.setObjectMapperFactory(plugin.getFactory()).setShouldCopyDefaults(true));
			configmsg = config.getValue(TypeToken.of(Config.class), new Config());
			loader.save(config);
			return true;
		} catch (Exception error) {
			getLogger().error("coudnt make the config", error);

			return false;
		}
	}

	String HeaderMessage = "Thank you for downloading and trying it out. Let me explain how this work please choose ONE option for the egg to drop or place. "
			+ "ONLY CHOOSE 1 because if you choose twice it will spawn the egg twice. If you choose to place the egg make sure that you have the correct height from the top of the portal. By default it's 70 because the egg can just fall into place";

	public File getConfigDirectory() {
		return configDirectory;
	}

	private Task task;

	@Listener
	public void onEntityDeath(DestructEntityEvent.Death event) {
		Living entity = event.getTargetEntity();
		if (event.getTargetEntity() instanceof EnderDragon) {

			Entity e = (Entity) event.getTargetEntity();
			Vector3d position = e.getLocation().getPosition();
			World world = entity.getWorld();
			Entity itemEntity = world.createEntity(EntityTypes.ITEM, position);
			if (Config.DropDragonEgg) {
				Item items = (Item) itemEntity;
				ItemStack itemStack = ItemStack.builder().itemType(ItemTypes.DRAGON_EGG).quantity(1).build();
				items.offer(Keys.REPRESENTED_ITEM, itemStack.createSnapshot());
				world.spawnEntity(items);
			}
			if (Config.PlaceDragonEgg) {
				if (EnderDragonPhaseTypes.DYING != null) {

					task = Task.builder().delay(15, TimeUnit.SECONDS).execute(() -> {
						event.getTargetEntity().getWorld().setBlock(0, Config.setHeight, 0,
								BlockState.builder().blockType(BlockTypes.DRAGON_EGG).build());

						task.cancel();
					}).submit(this);

				}
			}
		}
	}

}
