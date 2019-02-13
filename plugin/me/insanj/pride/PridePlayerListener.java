package me.insanj.pride;

import java.util.HashMap;

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
        //plugin.getLogger().info(String.format("From %.2f,%.2f,%.2f to %.2f,%.2f,%.2f", from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ()));

        Location to = event.getTo();
        detectPride(event.getPlayer().getName(), to, event.getPlayer().getWorld());
    }

    public void detectPride(String playerName, Location playerLocation, World world) {
        // purpose: use a given location to determine if a player has entered/exited an area
        // 1 last time this player was detected within an area
        String lastPlayerActivatedArea = (String)playerAreaHistory.getOrDefault(playerName, "");

        // 2 detection areas
        String filename = "pride.txt";
        HashMap prideAreas = PrideConfigurator.readPrideAreas(world, filename);
        if (prideAreas == null) {
            //plugin.getLogger("Unable to get prideAreas from pride.txt file");
            prideAreas = new HashMap();
            PrideConfigurator.writePrideAreas(filename, prideAreas);
            plugin.getLogger().info("Write new pride.txt file because we couldn't find one already");
        } 

        double threshold = 50; // how close you have to be to an area to activate it

        // 3 go through areas and see if playerLocation is within threshold pixels of a prideArea
        prideAreas.forEach((key, value) -> {
            Location areaLocation = (Location)value;
            double xDiff = Math.abs(areaLocation.getX() - playerLocation.getX());
            double zDiff = Math.abs(areaLocation.getZ() - playerLocation.getZ());
            double totalDiff = xDiff + zDiff;
            plugin.getLogger().info("Checking if player " + playerName + " is inside area " + (String)key + ". diff = " + Double.toString(totalDiff));

            if (totalDiff <= threshold) {
                String areaName = (String)key;
                if (!lastPlayerActivatedArea.equals(areaName)) {
                    // activate! ---- ignore if true because we have already activated here
                    playerAreaHistory.put(playerName, areaName);

                    // result: send a message to the server if a player has entered an area
                    Bukkit.broadcastMessage(ChatColor.GREEN + playerName + ChatColor.WHITE + " has activated area " + ChatColor.BLUE + areaName + ChatColor.WHITE + "!");
                }
            }
        });
    }
}