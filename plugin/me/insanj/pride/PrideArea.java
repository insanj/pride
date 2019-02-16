package me.insanj.pride;

import java.util.HashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.Location;

import org.bukkit.configuration.file.YamlConfiguration;

public class PrideArea {
    // extracted from Location
    public final double x;
    public final double y;
    public final double z;
    
    // can be extracted from Location or other Bukkit resources
    public final UUID worldUID;

    // custom Pride attributes
    public final double distance;
    public final String name;
    public final String settlerName;
    public final String createdDateString;

    public PrideArea(double x, double y, double z, UUID worldUID, double distance, String name, String settlerName, String createdDateString) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldUID = worldUID;
        this.distance = distance;
        this.name = name;
        this.settlerName = settlerName;
        this.createdDateString = createdDateString;
    }

    public HashMap encodedForYAML() {
        HashMap myObjectAsDict = new HashMap<>();    
        Field[] allFields = this.class.getDeclaredFields();
        for (Field field : allFields) {
                Class<?> targetType = field.getType();
                Object objectValue = targetType.newInstance();
                Object value = field.get(objectValue);
                myObjectAsDict.put(field.getName(), value);
            }
        }

        return myObjectAsDict;
    }

    public HashMap decodedForYAML() {

    }
}
