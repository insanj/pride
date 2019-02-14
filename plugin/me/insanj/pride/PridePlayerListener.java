package me.insanj.pride;

import java.util.HashMap;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PridePlayerListener implements Listener {
    public HashMap prideAreas;
    public int bottleneckThreshold;
    public double areaThreshold;
    public String prideFilename;

    private final Pride plugin;
    private HashMap playerAreaHistory;
    private int bottleneck;

    public PridePlayerListener(Pride instance, String filename) {
        prideFilename = filename;
        plugin = instance;
        playerAreaHistory = new HashMap();
        bottleneckThreshold = 1;
        bottleneck = 0;
        areaThreshold = 50;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (++bottleneck>=bottleneckThreshold) {
            bottleneck = 0;
            Location to = event.getTo();
            detectPride(event.getPlayer().getName(), to);
        }
    }

    private HashMap getPrideAreas(World world) {
        if (prideAreas == null) {
            // setup pride areas
            String filename = prideFilename;
            prideAreas = PrideConfigurator.readPrideAreas(world, filename);
            if (prideAreas == null) {
                //plugin.getLogger("Unable to get prideAreas from config file");
                prideAreas = new HashMap();
                PrideConfigurator.writePrideAreas(filename, prideAreas);
                plugin.getLogger().info("Wrote new config file because we couldn't find one already");
            } 
        }

        return prideAreas;
    }

    public ArrayList<String> getActivatedPrideAreas(Location playerLocation) {
        double threshold = areaThreshold; // how close you have to be to an area to activate it
        HashMap prideAreas = getPrideAreas(playerLocation.getWorld());
        ArrayList<String> activatedAreas = new ArrayList<String>();
        prideAreas.forEach((key, value) -> {
            Location areaLocation = (Location)value;
            double xDiff = Math.abs(areaLocation.getX() - playerLocation.getX());
            double zDiff = Math.abs(areaLocation.getZ() - playerLocation.getZ());
            double totalDiff = xDiff + zDiff;
            // plugin.getLogger().info("Checking if player " + playerName + " is inside area " + (String)key + ". diff = " + Double.toString(totalDiff));

            if (totalDiff <= threshold) {
                String areaName = (String)key;
                activatedAreas.add(areaName);
            }
        });

        return activatedAreas;
    }

    public String formatAreaMessageFromActivatedAreas(ArrayList<String> activatedAreas) {
        String areaMessage = null;
        for (String areaName : activatedAreas) {
            if (areaMessage == null) {
                areaMessage = areaName;
            } else if (areaMessage.contains("and")) {
                areaMessage = areaName + " ," + areaMessage;
            } else {
                areaMessage = areaMessage + " & " + areaName;
            }
        }

        if (areaMessage == null) { // wat?
            areaMessage = "";
        }
        return areaMessage;
    }

    public void detectPride(String playerName, Location playerLocation) {
        if (playerName == null || playerLocation == null) {
            plugin.getLogger().info("Unable to do anything will null params");
            return;
        }

        // purpose: use a given location to determine if a player has entered/exited an area
        // 1 go through areas and see if playerLocation is within threshold pixels of a prideArea
        ArrayList<String> activatedAreas = getActivatedPrideAreas(playerLocation);

        // 2 remove all old areas from newly activated areas by checking the last time this player was detected within n area(s)
        ArrayList<String> lastPlayerActivatedAreas = (ArrayList<String>)playerAreaHistory.getOrDefault(playerName, new ArrayList<String>());
        ArrayList<String> newlyActivatedAreas = new ArrayList<String>();
        for (String areaName : activatedAreas) {
            if (lastPlayerActivatedAreas.indexOf(areaName) == -1) {
                // ignore if > -1 because we have already activated here
                newlyActivatedAreas.add(areaName);
                lastPlayerActivatedAreas.add(areaName); // update history
            }
        }

        // result: send a message to the server if a player has entered an area
        String areaMessage = formatAreaMessageFromActivatedAreas(newlyActivatedAreas);
        Bukkit.broadcastMessage("♔  " + ChatColor.GREEN + playerName + ChatColor.WHITE + " entered " + ChatColor.BLUE + areaMessage + ChatColor.WHITE + "!");
        playerAreaHistory.put(playerName, activatedAreas); // update history
    }
}