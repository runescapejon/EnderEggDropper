package me.runescapejon.enderegg;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {

	@Setting(value = "Drop Dragon egg", comment = "If true once the ender dragon is dead it just drop the dragon egg without placing it")
	public static boolean DropDragonEgg = false;

	@Setting(value = "Place Dragon egg", comment = "If true once the ender dragon is dead it place the dragon egg on top of the portal")
	public static boolean PlaceDragonEgg = true;

	@Setting(value = "set Height", comment = "If you have Place Dragon Egg true then configure your height of the top of the portal on where you like the dragon egg to spawn")
	public static int setHeight = 70;
}
