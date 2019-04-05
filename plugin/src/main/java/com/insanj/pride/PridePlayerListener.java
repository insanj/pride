package com.insanj.pride;

import java.util.HashMap;
import java.util.ArrayList;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.block.FabricBlockSettings;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.block.BlockItem;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.text.StringTextComponent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.Identifier;

public class PridePlayerListener implements Listener {
    private final Pride plugin;
    private final PrideConfiguration config;

    private HashMap playerAreaHistory;
    private int bottleneck;
    public int bottleneckThreshold;

    public PridePlayerListener(Pride instance, PrideConfiguration givenConfig) {
        plugin = instance;
        config = givenConfig;

        playerAreaHistory = new HashMap();
        bottleneckThreshold = 50;
        bottleneck = 0;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        PridePlayerListener listener = this;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                if (++listener.bottleneck>=listener.bottleneckThreshold) {
                    listener.bottleneck = 0;
                    Location to = event.getTo();
                    String playerName = event.getPlayer().getName();
                    listener.detectPride(playerName, to);
                }
            }
        });
    }

    public ArrayList<String> getActivatedPrideAreas(Location playerLocation) {
        double threshold = config.getConfigDistance(); // how close you have to be to an area to activate it
        World world = playerLocation.getWorld();
        HashMap prideAreas = config.getConfigAreas(world);
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

        if (newlyActivatedAreas.size() <= 0) {
            // nothing was activated, do nothing in response
        } else {
            // result: send a message to the server if a player has entered an area
            String areaMessage = formatAreaMessageFromActivatedAreas(newlyActivatedAreas);
            Bukkit.broadcastMessage("♔  " + ChatColor.GREEN + playerName + ChatColor.WHITE + " entered " + ChatColor.BLUE + areaMessage + ChatColor.WHITE + "!");
        }
        
        playerAreaHistory.put(playerName, activatedAreas); // update history
    }
}