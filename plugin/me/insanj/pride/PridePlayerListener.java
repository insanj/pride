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
    private final Pride plugin;
    public HashMap playerAreaHistory;

    public PridePlayerListener(Pride instance) {
        plugin = instance;
        playerAreaHistory = new HashMap();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getLogger().info(event.getPlayer().getName() + " joined the server! :D");
    }

    /*@EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) { }
    */

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        //Location from = event.getFrom();
        Location to = event.getTo();
        detectPride(event.getPlayer().getName(), to, event.getPlayer().getWorld());
    }

    public void detectPride(String playerName, Location playerLocation, World world) {
        // purpose: use a given location to determine if a player has entered/exited an area
        // 0 read detection areas
        String filename = "pride.txt";
        HashMap prideAreas = PrideConfigurator.readPrideAreas(world, filename);
        if (prideAreas == null) {
            //plugin.getLogger("Unable to get prideAreas from pride.txt file");
            prideAreas = new HashMap();
            PrideConfigurator.writePrideAreas(filename, prideAreas);
            plugin.getLogger().info("Wrote new pride.txt file because we couldn't find one already");
        } 

        double threshold = 50; // how close you have to be to an area to activate it

        // 1 go through areas and see if playerLocation is within threshold pixels of a prideArea
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
        String areaMessage = null;
        for (String areaName : newlyActivatedAreas) {
            if (areaMessage == null) {
                areaMessage = areaName;
            } else if (areaMessage.contains("and")) {
                areaMessage = areaName + " ," + areaMessage;
            } else {
                areaMessage = areaMessage + " & " + areaName;
            }
        }

        if (areaMessage == null) {
            // wat ?
        } else {
            Bukkit.broadcastMessage("♔  " + ChatColor.GREEN + playerName + ChatColor.WHITE + " entered " + ChatColor.BLUE + areaMessage + ChatColor.WHITE + "!");
        }

        playerAreaHistory.put(playerName, activatedAreas); // update history
    }
}