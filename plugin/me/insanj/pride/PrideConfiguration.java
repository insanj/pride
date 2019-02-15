package me.insanj.pride;

import java.util.HashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.bukkit.World;
import org.bukkit.Location;

import org.bukkit.configuration.file.YamlConfiguration;

public class PrideConfiguration {
    // const keys
    public final String PRIDE_INITIALIZED_KEY = "initialized";
	public final String PRIDE_FILENAME_KEY = "filename";
    public final String PRIDE_DISTANCE_KEY = "distance";
    public final String PRIDE_AREAS_PATH = "areas";
    
    // private vars & constructor (use getters to get values from outside)
    private String filename;
    private double distance;
    private HashMap areas;

    public PrideConfiguration(World world) {
		// if default value does not exist / is false, assume we need to save a new config file
		Boolean prideInitialized = getConfig().getBoolean(PRIDE_INITIALIZED_KEY);
		if (prideInitialized == false) {
			saveDefaultConfig();
		}

		// config vars
		filename = getConfig().getString(PRIDE_FILENAME_KEY);
        distance = getConfig().getDouble(PRIDE_DISTANCE_KEY);

        areas = readPrideAreas(world, filename);
        if (areas == null) {
            areas = new HashMap();
        }
		getConfig().createSection(PRIDE_AREAS_PATH, areas);
        saveConfig();

		getLogger().info("Read config values: ");
		getLogger().info("prideFilename: " + prideFilename);
		getLogger().info("areaThreshold: " + Double.toString(playerListener.areaThreshold));
    }

    // public getters
    public String getConfigFilename() {
        return filename;
    }

    public double getConfigDistance() {
        return distance;
    }

    public HashMap getConfigAreas() {
        return areas;
    }

    public void setConfigAreas(HashMap givenAreas) {
        //getConfig().createSection(PRIDE_AREAS_PATH, givenAreas);
        //saveConfig();
        areas = givenAreas;
    }

    // private funcs
    static public HashMap readPrideAreas(World world, String filename) {
        try {
            File f = new File(filename);
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            HashMap output = (HashMap) ois.readObject();
            ois.close();

            HashMap parsed = new HashMap();
            output.forEach((k, v) -> {
                Location locationFromString = transformStringToLocation(world, (String)v);
                parsed.put(k, locationFromString);
            });

            return parsed;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    static public Boolean writePrideAreas(String filename, HashMap input) {
        try {
            HashMap parsed = new HashMap();
            input.forEach((k, v) -> {
                String stringFromLocation = transformLocationToString((Location)v);
                parsed.put(k, stringFromLocation);
            });

            File f = new File(filename);
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(parsed);
            oos.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
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
