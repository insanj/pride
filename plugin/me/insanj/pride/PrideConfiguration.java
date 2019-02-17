package me.insanj.pride;

import java.util.HashMap;
import java.util.UUID;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.bukkit.World;
import org.bukkit.Location;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class PrideConfiguration {
    // const keys
    public final String PRIDE_INITIALIZED_KEY = "initialized";
	public final String PRIDE_FILENAME_KEY = "filename";
    public final String PRIDE_DISTANCE_KEY = "distance";
    public final String PRIDE_WORLDS_PATH = "worlds";
    
    // private vars & constructor (use getters to get values from outside)
    private String filename;
    private double distance;
    private HashMap worlds;
    
    private Pride plugin;

    public PrideConfiguration(Pride givenPlugin) {
        plugin = givenPlugin;

        plugin.reloadConfig();

		// if default value does not exist / is false, assume we need to save a new config file
		Boolean prideInitialized = plugin.getConfig().getBoolean(PRIDE_INITIALIZED_KEY);
		if (prideInitialized == null || prideInitialized == false) {
			plugin.saveDefaultConfig();
		}

		// config vars
		filename = plugin.getConfig().getString(PRIDE_FILENAME_KEY);
        distance = plugin.getConfig().getDouble(PRIDE_DISTANCE_KEY);
        worlds = loadConfigWorldsFromDisk();
    }

    private void loadConfigWorldsFromDisk() {
        // first load from disk
        ConfigurationSection unparsedWorldsSection = plugin.getConfig().getConfigurationSection(PRIDE_WORLDS_PATH);
        if (unparsedWorldsSection != null) {
            HashMap unparsedWorlds = (HashMap)unparsedWorldsSection.getValues(true);
            HashMap parsedWorlds = new HashMap();

            for (Object worldUIDStringObject : unparsedWorlds.keySet()) {
                String worldUIDString = (String)worldUIDStringObject;
                ConfigurationSection worldAreas = (ConfigurationSection)unparsedWorlds.get(worldUIDString);
                HashMap unparsedWorldAreas = (HashMap)worldAreas.getValues(true);
                HashMap parsedWorldAreas = new HashMap();
                UUID worldUID = UUID.fromString((String)worldUIDString);

                for (Object areaNameObject : unparsedWorldAreas.keySet()) {
                    String areaName = (String)areaNameObject;
                    Object areaLocation = unparsedWorldAreas.get(areaName);
                    World world = plugin.getServer().getWorld(worldUID);
                    Location locationFromString = transformStringToLocation(world, (String)areaLocation);
                    parsedWorldAreas.put(areaName, locationFromString);
                }

                parsedWorlds.put(worldUID, parsedWorldAreas);
            }

            return parsedWorlds;
        } else {
            return new HashMap();
        }
    }

    // public getters
    public String getConfigFilename() {
        return filename;
    }

    public double getConfigDistance() {
        return distance;
    }

    public HashMap getConfigWorlds() {
        return worlds;
    }

    public HashMap getConfigAreas(World world) {
        Object result = worlds.get(world.getUID());
        if (result == null) {
            return new HashMap();
        } else {
            return (HashMap)result;
        }
    }

    public void setConfigAreas(World world, HashMap givenAreas) {
        if (world == null || givenAreas == null) {
            return;
        }

        HashMap encodedAreas = new HashMap();
        givenAreas.forEach((areaName, areaLocation) -> {
            String stringFromLocation = transformLocationToString((Location)areaLocation);
            encodedAreas.put(areaName, stringFromLocation);
        });

        String worldUIDString = world.getUID().toString();
        String worldSectionPath = PRIDE_WORLDS_PATH + "." + worldUIDString;
        plugin.getConfig().createSection(worldSectionPath, encodedAreas);
        plugin.saveConfig();
        worlds.put(world.getUID(), givenAreas);
    }

    // static helper functions
    static String transformLocationToString(Location location) {
        return String.format("%.2f,%.2f,%.2f", location.getX(),location.getY(),location.getZ());
    }

    static Location transformStringToLocation(World world, String string) {
        String[] components = string.split(",");
        double x = Double.parseDouble(components[0]);
        double y = Double.parseDouble(components[1]);
        double z = Double.parseDouble(components[2]);
        return new Location(world, x, y, z);
    }
}
