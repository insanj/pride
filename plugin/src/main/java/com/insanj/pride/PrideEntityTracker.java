package com.insanj.pride;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.ArrayList;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.entity.EntityTrackingRegistry;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.fabricmc.fabric.impl.server.EntityTrackerStorageAccessor;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandManager;
import net.minecraft.text.StringTextComponent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import com.google.gson.Gson;

public class PrideEntityTracker {
  // currently unused config which will be used to configure detection distance, etc
  private final PrideConfig config;

  // arbitrary bottleneck to prevent running the tracker every single tick (more than once per second!)
  private final int superLazyBottleneckLimit = 50;
  private int superLazyBottleneck = 0;

  // how many blocks away in all x/y/z directions required to activate pride area
  private final double areaDetectionDistance = 50.0;

  // when a player is in a pride area, it will be added to the ArrayList keyed by their name
  private Map<String, ArrayList<String>> currentlyActivatedAreas = new HashMap();

  public PrideEntityTracker(PrideConfig config) {
    this.config = config;
  }

  public void register() {
    ServerTickCallback.EVENT.register((MinecraftServer server) -> {
      // only allow method to run once every 50 ticks
      if (++superLazyBottleneck < superLazyBottleneckLimit) {
        return;
      }

      // get all players logged into the server
      Set<ServerPlayerEntity> entities = (Set<ServerPlayerEntity>)PlayerStream.all(server).collect(Collectors.toSet());

      // loop through all players active on server
      for (ServerPlayerEntity player : entities) {
        ServerWorld world = player.getServerWorld();
        calculateActivatedPrideAreasForPlayer(world, player);
      }

      // reset bottleneck counter at end of execution/for loop
      superLazyBottleneck = 0;
    });
  }

  // async/thread-based function to run calculations for pride areas off main
  public void calculateActivatedPrideAreasForPlayer(ServerWorld world, ServerPlayerEntity player) {
    PrideEntityTracker tracker = this;
    new Thread(new Runnable() { 
      public void run() { 
        String playerName = player.getName().getString();
        BlockPos pos = player.getBlockPos();

        // get pride areas for world that player is in (should be cached)
        PridePersistentState persis = PridePersistentState.get(world);
        Map<String, Map<String, Double>> areas = persis.getPrideAreas(world);

        if (areas == null || areas.size() <= 0) {
          return; // no areas in this world
        }

        // System.out.println(String.format("looping through %s in %s seeing if %s is in range of something", areas.toString(), world.toString(), pos.toString()));

        // loop through all areas in world
        for (String areaName : areas.keySet()) {
          Map<String, Double> area = areas.get(areaName);
          BlockPos areaPos = new BlockPos(area.get("x"), area.get("y"), area.get("z"));

          // calculate distance to area
          double distanceBetween = Math.abs(Math.abs(areaPos.getX() - pos.getX()) + Math.abs(areaPos.getY() - pos.getY()) + Math.abs(areaPos.getZ() - pos.getZ()));

          if (distanceBetween <= areaDetectionDistance) {
            // activate!
            if (tracker.currentlyActivatedAreas.get(playerName) == null) {
              ArrayList<String> playerActivatedAreas = new ArrayList<String>();
              playerActivatedAreas.add(areaName);

              tracker.currentlyActivatedAreas.put(playerName, playerActivatedAreas);
              player.addChatMessage(new StringTextComponent(String.format("%s activated %s!", playerName, areaName)), false); 
            }

            // activate!
            else if (tracker.currentlyActivatedAreas.get(playerName).contains(areaName) == false) {
              ArrayList<String> playerActivatedAreas = tracker.currentlyActivatedAreas.get(playerName);
              playerActivatedAreas.add(areaName);

              tracker.currentlyActivatedAreas.put(playerName, playerActivatedAreas);
              player.addChatMessage(new StringTextComponent(String.format("%s activated %s!", playerName, areaName)), false);
            }
          }

          // stop activating...
          else {
            if (tracker.currentlyActivatedAreas.get(playerName) != null && tracker.currentlyActivatedAreas.get(playerName).contains(areaName)) {
              ArrayList<String> playerActivatedAreas = tracker.currentlyActivatedAreas.get(playerName);
              playerActivatedAreas.remove(areaName);
              tracker.currentlyActivatedAreas.put(playerName, playerActivatedAreas);
            }
          }
        }
      }
    }).start(); 
  } 
}
