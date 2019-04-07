package com.insanj.pride;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

import net.minecraft.server.command.ServerCommandManager;
import net.minecraft.text.StringTextComponent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.block.BlockItem;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.inventory.Inventory;
import net.minecraft.block.Block;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.block.BlockItem;
import net.minecraft.item.MiningToolItem;
import net.minecraft.server.world.BlockAction;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.Style;
import net.minecraft.text.TextComponent;
import net.minecraft.text.StringTextComponent;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.BlockView;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.client.network.packet.PlayerSpawnPositionS2CPacket;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import com.google.gson.Gson;
import com.google.common.collect.Multimap;

public class PrideCommandExecutor {
    private final PrideConfig config;
    public PrideCommandExecutor(PrideConfig config) {
        this.config = config;
    }

    public void register() {
        registerPrideCommand();
        registerSettleCommand();
        registerAbandonCommand();
        registerCompassCommand();
    }

    private void registerPrideCommand() {
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
    }

    private void registerSettleCommand() {
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
    }

    private void registerAbandonCommand() {
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


    private void registerCompassCommand() {
        CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
            ServerCommandManager.literal("compass")
                .then(ServerCommandManager.argument("name", StringArgumentType.string())
                    .executes(context -> {
                        // translate the area name given in the command to the x/y/z coordinates for the pride area
                        ServerWorld world = context.getSource().getWorld();
                        String areaName = StringArgumentType.getString(context, "name");

                        PridePersistentState persis = PridePersistentState.get(world);
                        Map<String, Double> area = persis.getPrideArea(world, areaName);
                        BlockPos pos = new BlockPos((double)area.get("x"), (double)area.get("y"), (double)area.get("z"));

                        ServerPlayerEntity player = context.getSource().getPlayer();
                        PlayerSpawnPositionS2CPacket packet = new PlayerSpawnPositionS2CPacket(pos);
                        player.networkHandler.sendPacket(packet);

                        StringTextComponent message = new StringTextComponent("Compass pointed towards " + areaName + "!");
                        context.getSource().getPlayer().addChatMessage(message, false);
                        return 1;
                    }))
        ));
    }
}
