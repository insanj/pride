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

import net.minecraft.server.command.ServerCommandManager;
import net.minecraft.text.StringTextComponent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import com.google.gson.Gson;

public class PrideMod implements ModInitializer {
    public static final String MOD_ID = "pride";

    private String configPath;
    private PrideConfig config;
    private PrideCommandExecutor executor;
    private PrideEntityTracker tracker;

    @Override
    public void onInitialize() {
        configPath = FabricLoader.getInstance().getConfigDirectory() + "/" + MOD_ID + ".json";

        File configFile = new File(configPath);
        if (!configFile.exists()) {
            config = PrideConfig.writeConfigToFile(configFile);
        }
        else {
            config = PrideConfig.configFromFile(configFile);
        }

        executor = new PrideCommandExecutor(config);
        executor.register();

        tracker = new PrideEntityTracker(config);
        tracker.register();
    }
}
