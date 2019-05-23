package com.insanj.pride.save;

import net.fabricmc.fabric.api.util.NbtType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TagHelper;
import net.minecraft.world.PersistentState;
import net.minecraft.world.WorldSaveHandler;

import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.HashMap;
import java.util.Set;

public class PridePersistentState extends PersistentState {
    // format of pride save files:
    // worlds:
    //    abcde-12345....: (1st map)
    //      Birch & Brick: (2nd map)
    //        x: .... (3rd map)
    public Map<String, Map<String, Map<String, Double>>> worlds = new HashMap();

    public static PridePersistentState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(PridePersistentState::new, "PridePersistentState");
    }

    public PridePersistentState() {
        super("PridePersistentState");
    }

    // CRUD
    private String keyForWorld(ServerWorld world) {
      return world.getSaveHandler().getWorldDir().getName(); //getSaveHandler().readProperties().get();
    }

    public Map<String, Map<String, Double>> getPrideAreas(ServerWorld world) {
        String key = keyForWorld(world);
        return this.worlds.get(key);
    }

    public boolean doPrideAreasExist(ServerWorld world) {
        String key = keyForWorld(world);
        return this.worlds.containsKey(key);
    }

    public Map<String, Double> getPrideArea(ServerWorld world, String areaName) {
        String key = keyForWorld(world);
        return this.worlds.get(key).get(areaName);
    }

    public boolean doesPrideAreaExist(ServerWorld world, String areaName) {
        String key = keyForWorld(world);
        return this.worlds.containsKey(key) && this.worlds.get(key).containsKey(areaName);
    }

    public void setPrideArea(ServerWorld world, String areaName, Map<String, Double> area) {
        String key = keyForWorld(world);
        Map<String, Map<String, Double>> areasInThisWorld = null;

        if (!doPrideAreasExist(world)) {
          areasInThisWorld = new HashMap();
        } else {
          areasInThisWorld = this.worlds.get(key);
        }

        areasInThisWorld.put(areaName, area);
        this.worlds.put(key, areasInThisWorld);
        this.markDirty();
    }

    public void removePrideArea(ServerWorld world, String areaName) {
        if (!doesPrideAreaExist(world, areaName)) {
          return;
        }

        String key = keyForWorld(world);
        Map<String, Map<String, Double>> areasInThisWorld = this.worlds.get(key);
        areasInThisWorld.remove(areaName);
        this.worlds.put(key, areasInThisWorld);
        this.markDirty();
    }

    @Override
    public void fromTag(CompoundTag compoundTag) {
        CompoundTag worldsTag = (CompoundTag) compoundTag.getTag("pride_worlds");
        Set<String> worldKeys = worldsTag.getKeys();

        for (String key : worldKeys) {
            CompoundTag areasTag = (CompoundTag) worldsTag.getTag(key);
            Set<String> areaNames = areasTag.getKeys();
            Map<String, Map<String, Double>> parsedAreas = new HashMap();

            for (String areaName : areaNames) {
                CompoundTag area = (CompoundTag) areasTag.getTag(areaName); //areasTag.getList(areaName, NbtType.COMPOUND);
                Map<String, Double> areaMap = new HashMap();
                DoubleTag x = (DoubleTag) area.getTag("x");
                DoubleTag y = (DoubleTag) area.getTag("y");
                DoubleTag z = (DoubleTag) area.getTag("z");

                areaMap.put("x", x.getDouble());
                areaMap.put("y", y.getDouble());
                areaMap.put("z", z.getDouble());
                parsedAreas.put(areaName, areaMap);
            }

            worlds.put(key, parsedAreas);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag compoundTag) {
        CompoundTag worldsTag = new CompoundTag();

        for (String key : worlds.keySet()) {
          Map<String, Map<String, Double>> areas = worlds.get(key);
          CompoundTag areasTags = new CompoundTag();

          for (String areaName : areas.keySet()) {
              Map<String, Double> areaMap = areas.get(areaName);
              CompoundTag areaTag = new CompoundTag();

              DoubleTag x = new DoubleTag(areaMap.get("x"));
              DoubleTag y = new DoubleTag(areaMap.get("y"));
              DoubleTag z = new DoubleTag(areaMap.get("z"));

              areaTag.put("x", x);
              areaTag.put("y", y);
              areaTag.put("z", z);

              areasTags.put(areaName, areaTag);
          }

          worldsTag.put(key, areasTags);
        }

        compoundTag.put("pride_worlds", worldsTag);
        return compoundTag;
    }
}
