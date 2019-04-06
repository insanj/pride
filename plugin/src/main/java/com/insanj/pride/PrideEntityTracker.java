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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import com.google.gson.Gson;

public class PrideEntityTracker {
  private final PrideConfig config;
  public PrideEntityTracker(PrideConfig config) {
    this.config = config;
  }

  public void register() {

    /*
    int trackingDistance = 50;
    int updateIntervalTicks = 1;
    boolean alwaysUpdateVelocity = false;

    EntityCategory playerCategory = EntityType.PLAYER.category;
    EntityType type = FabricEntityTypeBuilder.create(playerCategory, (entityType, world, entity) -> {
      System.out.println("yo yo yo!");
      System.out.println("tick! entity = " + entity.toString() + " world = " + world.toString());
      return entity;
    }).trackable(trackingDistance, updateIntervalTicks, alwaysUpdateVelocity).build();

    EntityTrackingRegistry.INSTANCE.register(type, trackingDistance, updateIntervalTicks, alwaysUpdateVelocity);*/


    ServerTickCallback.EVENT.register((MinecraftServer server) -> {
      System.out.println("tick!");

      Set<PlayerEntity> entities = PlayerStream.all(server).collect(Collectors.toSet());
      System.out.println("entities = " + entities.toString());
    });
  }
}
