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

public class PrideConfigurator {
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
    
    /*
    static YamlConfiguration getConfig() {

    }

    static void writeConfig(String key, String value) {

    }*/
}
