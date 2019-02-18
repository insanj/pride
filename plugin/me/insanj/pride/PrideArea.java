package me.insanj.pride;

import java.util.Map;
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

public class PrideArea {
    public final Location location;
    public final double distance;

    public PrideArea(Location location, double distance) {
        this.location = location;
        this.distance = distance;
    }

    // static converters
    public static PrideArea prideAreaFromString(String string) {
        
    }

    public static String stringFromPrideArea(PrideArea area) {
        String locationString = PrideArea.transformLocationToString(area.location);
        String areaString = locationString + ",d=" + Double.toString(area.distance);
        return areaString;
    }

    // static helper functions
    public static String transformLocationToString(Location location) {
        return String.format("%.2f,%.2f,%.2f", location.getX(),location.getY(),location.getZ());
    }

    public static Location transformStringToLocation(World world, String string) {
        String[] components = string.split(",");
        double x = Double.parseDouble(components[0]);
        double y = Double.parseDouble(components[1]);
        double z = Double.parseDouble(components[2]);
        return new Location(world, x, y, z);
    }
}