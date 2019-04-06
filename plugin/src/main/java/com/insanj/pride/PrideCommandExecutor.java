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

public class PrideCommandExecutor {
    private final PrideConfig config;
    public PrideCommandExecutor(PrideConfig config) {
        this.config = config;
    }

    public void register() {
        /*
        CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
                ServerCommandManager.literal("vcolor")
                        .then(ServerCommandManager.argument("r", IntegerArgumentType.integer())
                        .then(ServerCommandManager.argument("g", IntegerArgumentType.integer())
                        .then(ServerCommandManager.argument("b", IntegerArgumentType.integer())
                        .executes(context -> {
                            int r = IntegerArgumentType.getInteger(context,"r");
                            int g = IntegerArgumentType.getInteger(context,"g");
                            int b = IntegerArgumentType.getInteger(context,"b");

                            config.hudColor = b + (g << 8) + (r << 16);
                            config.saveConfig(configPath);
                            return 1;
                        }))))
        ));*/

        // PRIDE
        CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
                ServerCommandManager.literal("pride")
                        .executes(context -> {
                            ServerWorld world = context.getSource().getWorld();
                            PridePersistentState persis = PridePersistentState.get(world);
                            String messageString = "";

                            Map<String, Map<String, Double>> prideAreas = persis.getPrideAreas(world);
                            if (prideAreas == null || prideAreas.size() <= 0) {
                                messageString = "No Pride areas found in this world.";
                            }
                            
                            else {
                                for (String areaName: prideAreas.keySet()) {
                                  Map<String, Double> prideArea = prideAreas.get(areaName);
                                  messageString += areaName + " " + prideArea.toString();
                                }
                            }

                            StringTextComponent message = new StringTextComponent(messageString);
                            context.getSource().getPlayer().addChatMessage(message, false);
                            return 1;
                        })
        ));

        CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
                ServerCommandManager.literal("settle")
                        .then(ServerCommandManager.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            ServerWorld world = context.getSource().getWorld();
                            String areaName = StringArgumentType.getString(context, "name");

                            BlockPos pos = context.getSource().getPlayer().getBlockPos();
                            Map<String, Double> area = new HashMap();
                            area.put("x", new Double(pos.getX()));
                            area.put("y", new Double(pos.getY()));
                            area.put("z", new Double(pos.getZ()));

                            PridePersistentState persis = PridePersistentState.get(world);
                            persis.setPrideArea(world, areaName, area);

                            StringTextComponent message = new StringTextComponent("Founded " + areaName + "!");
                            context.getSource().getPlayer().addChatMessage(message, false);
                            return 1;
                        }))
        ));

        CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
                ServerCommandManager.literal("abandon")
                        .then(ServerCommandManager.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            ServerWorld world = context.getSource().getWorld();
                            String areaName = StringArgumentType.getString(context, "name");

                            PridePersistentState persis = PridePersistentState.get(world);
                            persis.removePrideArea(world, areaName);

                            StringTextComponent message = new StringTextComponent("Removed " + areaName + "!");
                            context.getSource().getPlayer().addChatMessage(message, false);
                            return 1;
                        }))
        ));
    }
}
