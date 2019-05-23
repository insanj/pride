package com.insanj.pride;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

import net.minecraft.server.command.CommandManager;
import net.minecraft.text.StringTextComponent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import com.google.gson.Gson;

import com.insanj.pride.util.*;
import com.insanj.pride.save.*;

public class PrideMod implements ModInitializer {
    public static final String MOD_ID = "pride";

    public PrideConfig config;
    private PrideCommandExecutor executor;
    private PrideEntityTracker tracker;

    @Override
    public void onInitialize() {
        File configFile = new File(PrideMod.getConfigPath());
        if (!configFile.exists()) {
            config = PrideConfig.writeConfigToFile(configFile);
        }
        else {
            config = PrideConfig.configFromFile(configFile);
        }

        executor = new PrideCommandExecutor(this);
        executor.register();

        tracker = new PrideEntityTracker(this);
        tracker.register();
    }

    public static String getConfigPath() {
        return FabricLoader.getInstance().getConfigDirectory() + "/" + MOD_ID + ".json";
    }
}
