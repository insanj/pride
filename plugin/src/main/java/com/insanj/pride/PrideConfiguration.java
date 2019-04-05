package com.insanj.pride;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class PrideConfiguration {
    // const keys
    public final String PRIDE_INITIALIZED_KEY = "initialized";
    public final String PRIDE_DISTANCE_KEY = "distance";
    public final String PRIDE_WORLDS_PATH = "worlds";
    
    // private vars & constructor (use getters to get values from outside)
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
        distance = plugin.getConfig().getDouble(PRIDE_DISTANCE_KEY);
        worlds = loadConfigWorldsFromDisk();
    }

    private HashMap loadConfigWorldsFromDisk() {
        // first load from disk
        ConfigurationSection unparsedWorldsSection = plugin.getConfig().getConfigurationSection(PRIDE_WORLDS_PATH);
        if (unparsedWorldsSection != null) {
            HashMap parsedWorlds = new HashMap();
            Map<String, Object> unparsedWorlds = (Map<String, Object>)unparsedWorldsSection.getValues(false);

            for (String worldUIDString : unparsedWorlds.keySet()) {
                ConfigurationSection worldAreasSection = (ConfigurationSection) unparsedWorlds.get(worldUIDString);
                String worldAreasSectionPath = worldAreasSection.getCurrentPath();
                Map<String, Object> unparsedWorldAreas = (Map<String, Object>) plugin.getConfig().getConfigurationSection(worldAreasSectionPath).getValues(false);
               
                HashMap parsedWorldAreas = new HashMap();
                UUID worldUID = UUID.fromString((String)worldUIDString);
                World world = plugin.getServer().getWorld(worldUID);
                for (String areaName : unparsedWorldAreas.keySet()) {
                    ConfigurationSection areaDataSection = (ConfigurationSection) unparsedWorldAreas.get(areaName);
                    String areaDataSectionPath = areaDataSection.getCurrentPath();
                    Map<String, Object> areaDataMap = (Map<String, Object>) plugin.getConfig().getConfigurationSection(areaDataSectionPath).getValues(false);
                    
                    Object xObj = areaDataMap.get("x");
                    Object yObj = areaDataMap.get("y");
                    Object zObj = areaDataMap.get("z");
                    Double x, y, z;
                    if (xObj instanceof Integer) {
                        x = new Double((Integer)xObj);
                    } else {
                        x = (Double)xObj;
                    }

                    if (yObj instanceof Integer) {
                        y = new Double((Integer)yObj);
                    } else {
                        y = (Double)yObj;
                    }

                    if (zObj instanceof Integer) {
                        z = new Double((Integer)zObj);
                    } else {
                        z = (Double)zObj;
                    }

                    Location locationFromData = new Location(world, (double)x, (double)y, (double)z);
                    parsedWorldAreas.put(areaName, locationFromData);
                }

                parsedWorlds.put(worldUID, parsedWorldAreas);
            }

            return parsedWorlds;
        } else {
            return new HashMap();
        }
    }

    // public getters
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
            HashMap areaData = new HashMap();
            Location areaLocation2 = (Location)areaLocation;
            areaData.put("x", areaLocation2.getX());
            areaData.put("y", areaLocation2.getY());
            areaData.put("z", areaLocation2.getZ());
            encodedAreas.put(areaName, areaData);
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
