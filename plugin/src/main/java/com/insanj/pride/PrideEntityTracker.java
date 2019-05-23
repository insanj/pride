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
import java.util.UUID;

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
import net.minecraft.text.StringTextComponent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import net.minecraft.text.Style;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.text.NbtTextComponent;
import net.minecraft.text.event.HoverEvent;
import net.minecraft.command.EntitySelector;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import com.google.gson.Gson;

import com.insanj.pride.util.*;
import com.insanj.pride.save.*;

public class PrideEntityTracker {
  private final PrideMod plugin;

  // arbitrary bottleneck to prevent running the tracker every single tick (more than once per second!)
  private int superLazyBottleneck = 0;

  // when a player is in a pride area, it will be added to the ArrayList keyed by their name
  private Map<String, ArrayList<String>> currentlyActivatedAreas = new HashMap();

  public PrideEntityTracker(PrideMod plugin) {
    this.plugin = plugin;
  }

  public void register() {
    ServerTickCallback.EVENT.register((MinecraftServer server) -> {
      // only allow method to run once every 50 ticks
      if (++superLazyBottleneck < plugin.config.bottleneckLimit) {
        return;
      }

      // get all players logged into the server
      Set<ServerPlayerEntity> entities = (Set<ServerPlayerEntity>)PlayerStream.all(server).collect(Collectors.toSet());

      // loop through all players active on server
      for (ServerPlayerEntity player : entities) {
        ServerWorld world = player.getServerWorld();
        calculateActivatedPrideAreasForPlayer(server, world, player);
      }

      // reset bottleneck counter at end of execution/for loop
      superLazyBottleneck = 0;
    });
  }

  // async/thread-based function to run calculations for pride areas off main
  public void calculateActivatedPrideAreasForPlayer(MinecraftServer server, ServerWorld world, ServerPlayerEntity player) {
    PrideEntityTracker tracker = this;
    double areaDetectionDistance = plugin.config.activationDistance;

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

        // loop through all areas in world
        for (String areaName : areas.keySet()) {
          Map<String, Double> area = areas.get(areaName);
          BlockPos areaPos = new BlockPos(area.get("x"), area.get("y"), area.get("z"));

          // calculate distance to area
          double distanceBetween = Math.abs(Math.abs(areaPos.getX() - pos.getX()) + Math.abs(areaPos.getY() - pos.getY()) + Math.abs(areaPos.getZ() - pos.getZ()));

          if (distanceBetween <= areaDetectionDistance) {
            String areaDescription = String.format("x: %d, y: %d, z: %d", (Integer)areaPos.getX(), (Integer)areaPos.getY(), (Integer)areaPos.getZ());

            // activate!
            if (tracker.currentlyActivatedAreas.get(playerName) == null) {
              ArrayList<String> playerActivatedAreas = new ArrayList<String>();
              playerActivatedAreas.add(areaName);

              tracker.currentlyActivatedAreas.put(playerName, playerActivatedAreas);

              sendServerMessage(server, player, areaName, areaDescription);
              sendPlayerMessage(player, areaName);
            }

            // activate!
            else if (tracker.currentlyActivatedAreas.get(playerName).contains(areaName) == false) {
              ArrayList<String> playerActivatedAreas = tracker.currentlyActivatedAreas.get(playerName);
              playerActivatedAreas.add(areaName);

              tracker.currentlyActivatedAreas.put(playerName, playerActivatedAreas);

              sendServerMessage(server, player, areaName, areaDescription);
              sendPlayerMessage(player, areaName);
            }
          }

          // only stop tracking when you've gone the detection distance AWAY from the area (so if it's 50, 100 blocks away)
          else if (distanceBetween > (areaDetectionDistance * 2.0)) {
            // stop activating...
            if (tracker.currentlyActivatedAreas.get(playerName) != null && tracker.currentlyActivatedAreas.get(playerName).contains(areaName)) {
                ArrayList<String> playerActivatedAreas = tracker.currentlyActivatedAreas.get(playerName);
                playerActivatedAreas.remove(areaName);
                tracker.currentlyActivatedAreas.put(playerName, playerActivatedAreas);
            }
          }
        }
      }

      private void sendPlayerMessage(ServerPlayerEntity player, String areaName) {
        TextComponent component = new PrideTextComponentBuilder(areaName).color(TextFormat.GOLD).bold(true).build();
        player.addChatMessage(component, true);
      }

      private void sendServerMessage(MinecraftServer server, ServerPlayerEntity player, String areaName, String areaDescription) {
        String playerName = player.getName().getString();
        BlockPos playerPos = player.getBlockPos();
          
        // 1 build player name component (includes hover event)
        String playerHoverDescriptionString = String.format("Entered at location, x: %d, y: %d, z: %d", playerPos.getX(), playerPos.getY(), playerPos.getZ());
        TextComponent playerHoverComponent = new PrideTextComponentBuilder(playerHoverDescriptionString).build();
        TextComponent playerComponent = new PrideTextComponentBuilder(playerName).color(TextFormat.BLUE).hover(playerHoverComponent).build();

        // 2 build filler text "is entering" which has no styling
        TextComponent fillerComponent = new PrideTextComponentBuilder(" is entering ").color(TextFormat.WHITE).build();
  
        // 3 build area name component (includes hover event)
        TextComponent areaHoverComponent = new PrideTextComponentBuilder(areaDescription).build();
        TextComponent areaComponent = new PrideTextComponentBuilder(areaName).color(TextFormat.GOLD).bold(true).hover(areaHoverComponent).build();

        // 4 build punctuation / exclamation mark at end of sentence (without formatting)
        TextComponent punctuationComponent = new PrideTextComponentBuilder("!").color(TextFormat.WHITE).bold(true).build();

        // 5 combine all components and send the message to everyone!
        TextComponent concatComponent = playerComponent.append(fillerComponent).append(areaComponent).append(punctuationComponent);

        Set<ServerPlayerEntity> players = (Set<ServerPlayerEntity>)PlayerStream.all(server).collect(Collectors.toSet());

        for (ServerPlayerEntity onlinePlayer : players) {
          if (plugin.config.suppressedUUIDs.contains(onlinePlayer.getUuid()) == false) {
            onlinePlayer.addChatMessage(concatComponent, false);
          }
        }
      }
    }).start();
  } 
}
