package com.insanj.pride.util;

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
import net.minecraft.server.MinecraftServer;;
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

public class PrideBlockPosUtil {

  public static String areaDescription(BlockPos areaPos) {
      return String.format("x: %d, y: %d, z: %d", (Integer)areaPos.getX(), (Integer)areaPos.getY(), (Integer)areaPos.getZ());
  }

  public static double distanceBetween(BlockPos p1, BlockPos p2) {
    return Math.abs(Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY()) + Math.abs(p1.getZ() - p2.getZ()));
  }

  public static boolean isInsideThresholdOfPos(BlockPos pos, double threshold, BlockPos areaPos) {
    return distanceBetween(pos, areaPos) <= threshold;
  }

  public static BlockPos posFromPrideArea(Map<String, Double> area) {
    return new BlockPos(area.get("x"), area.get("y"), area.get("z"));
  }

  public static Map<String, Map<String, Double>> prideAreasInsidePosThreshold(Map<String, Map<String, Double>> areas, BlockPos pos, double threshold) {
    HashMap<String, Map<String, Double>> activatedAreas = new HashMap();
    for (String areaName : areas.keySet()) {
        Map<String, Double> area = areas.get(areaName);
        BlockPos areaPos = posFromPrideArea(area);
        if (isInsideThresholdOfPos(pos, 50, areaPos) == true) {
          activatedAreas.put(areaName, area);         
        }
    }

    return activatedAreas;
  }
}
