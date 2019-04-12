package com.insanj.pride;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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
import net.minecraft.text.Style;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.text.event.HoverEvent;

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
        registerAreasCommand();
        registerNearbyCommand();
        registerSettleCommand();
        registerAbandonCommand();
        registerCompassCommand();
    }

    private void registerPrideCommand() {
      CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
            ServerCommandManager.literal("pride")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();

                    ArrayList<TextComponent> components = new ArrayList<TextComponent>();
                    components.add(new StringTextComponent("Available Pride commands:"));
                    components.add(new StringTextComponent("/compass <area_name>"));
                    components.add(new StringTextComponent("/nearby <area_name>"));
                    components.add(new StringTextComponent("/areas <page_number>"));
                    components.add(new StringTextComponent("/settle <area_name>"));
                    components.add(new StringTextComponent("/abandon <area_name>"));

                    for (TextComponent c : components) {
                        player.addChatMessage(c, false);
                    }

                    return 1;
                })
        ));
    }

    private void registerAreasCommand() {
        CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
            ServerCommandManager.literal("areas")
                .then(ServerCommandManager.argument("pageNumber", IntegerArgumentType.integer())
                .executes(context -> {
                    ServerWorld world = context.getSource().getWorld();
                    PridePersistentState persis = PridePersistentState.get(world);
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    Map<String, Map<String, Double>> prideAreas = persis.getPrideAreas(world);

                    if (prideAreas == null) {
                        StringTextComponent component = new StringTextComponent("No Pride areas found :(");
                        player.addChatMessage(component, false);
                        return 1;
                    }

                    BlockPos playerLocation = player.getBlockPos();
                    List<String> sortedPrideAreaNames = new ArrayList<>(prideAreas.keySet());
                    Collections.sort(sortedPrideAreaNames);

                    Map<Integer, ArrayList<TextComponent>> pages = new HashMap<Integer, ArrayList<TextComponent>>();
                    ArrayList<TextComponent> page = new ArrayList<TextComponent>();
                    Integer pageIndex = 0;

                    for (String areaName: sortedPrideAreaNames) {
                        Map<String, Double> prideArea = prideAreas.get(areaName);

                        if (page.size() >= 8) {
                            pages.put(pageIndex++, page);
                            page = new ArrayList<TextComponent>();
                        }

                        BlockPos areaLocation = new BlockPos(prideArea.get("x"), prideArea.get("y"), prideArea.get("z"));
                        double xDiff = Math.abs(areaLocation.getX() - playerLocation.getX());
                        double yDiff = Math.abs(areaLocation.getY() - playerLocation.getY());
                        double zDiff = Math.abs(areaLocation.getZ() - playerLocation.getZ());
                        double totalDiff = Math.abs(xDiff + zDiff + yDiff);

                        String diffString = String.format("%.2f", totalDiff);

                        String areaDescription = String.format("x: %d, y: %d, z: %d", (Integer)areaLocation.getX(), (Integer)areaLocation.getY(), (Integer)areaLocation.getZ());

                        TextComponent hoverComponent = new PrideTextComponentBuilder(areaDescription).build();
                        TextComponent areaComponent = new PrideTextComponentBuilder(areaName).color(TextFormat.BLUE).bold(true).hover(hoverComponent).build();
                        TextComponent distComponent = new PrideTextComponentBuilder(" " + diffString + " blocks away").build();
                        TextComponent pageComponent = areaComponent.append(distComponent);
                        page.add(pageComponent);
                    }

                    if (page.size() > 0) {
                        pages.put(pageIndex, page); // pick up any items in last page < 8
                    }

                    // get page number from arguments
                    int humanPageNumber = IntegerArgumentType.getInteger(context, "pageNumber");
                    Integer pageNumber = humanPageNumber - 1;
                    System.out.println("Total pages = " + pages.size() + " Getting pageNumber = " + pageNumber);

                    if (pageNumber >= pages.size()) {
                        TextComponent message = new PrideTextComponentBuilder("Page not found. There are only " + pages.size() + " pages available.").color(TextFormat.RED).build();
                        player.addChatMessage(message, false);
                        return 1;
                    }
                    
                    TextComponent titleComponent = new PrideTextComponentBuilder("✿  Pride areas page " + humanPageNumber + " of " + pages.size()).color(TextFormat.BLUE).build();
                    player.addChatMessage(titleComponent, false);

                    ArrayList<TextComponent> pageToSend = pages.get(pageNumber);
                    for (TextComponent pageToSendComponent : pageToSend) {
                        player.addChatMessage(pageToSendComponent, false);
                    }

                    return 1;
                }))
        ));
    }

    private void registerNearbyCommand() {
        CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
            ServerCommandManager.literal("nearby")
                .then(ServerCommandManager.argument("pageNumber", IntegerArgumentType.integer())
                .executes(context -> {
                    ServerWorld world = context.getSource().getWorld();
                    PridePersistentState persis = PridePersistentState.get(world);
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    Map<String, Map<String, Double>> prideAreas = persis.getPrideAreas(world);

                    if (prideAreas == null) {
                        StringTextComponent component = new StringTextComponent("No Pride areas found :(");
                        player.addChatMessage(component, false);
                        return 1;
                    }

                    BlockPos playerLocation = player.getBlockPos();

                    Set<Entry<String, Map<String, Double>>> entries = prideAreas.entrySet();
                    Comparator<Entry<String, Map<String, Double>>> valueComparator = new Comparator<Entry<String, Map<String, Double>>>() {
                        @Override
                        public int compare(Entry<String, Map<String, Double>> e1, Entry<String, Map<String, Double>> e2) {
                            BlockPos p1 = new BlockPos(e1.getValue().get("x"), e1.getValue().get("y"), e1.getValue().get("z"));
                            BlockPos p2 = new BlockPos(e2.getValue().get("x"), e2.getValue().get("y"), e2.getValue().get("z"));

                            double xDiff = Math.abs(p1.getX() - p2.getX());
                            double yDiff = Math.abs(p1.getY() - p2.getY());
                            double zDiff = Math.abs(p1.getZ() - p2.getZ());
                            int totalDiff = (int)Math.floor(Math.abs(xDiff + zDiff + yDiff));
                            return totalDiff;
                        }

                    };
                    
                    List<Entry<String, Map<String, Double>>> listOfEntries = new ArrayList<Entry<String, Map<String, Double>>>(entries);
                    Collections.sort(listOfEntries, valueComparator);

                    LinkedHashMap<String, Map<String, Double>> sortedByValue = new LinkedHashMap<String, Map<String, Double>>(listOfEntries.size());
                    for(Entry<String, Map<String, Double>> entry : listOfEntries){
                        sortedByValue.put(entry.getKey(), entry.getValue());
                    }
                    
                    Map<Integer, ArrayList<TextComponent>> pages = new HashMap<Integer, ArrayList<TextComponent>>();
                    ArrayList<TextComponent> page = new ArrayList<TextComponent>();
                    Integer pageIndex = 0;

                    for (String areaName: sortedByValue.keySet()) {
                        Map<String, Double> prideArea = prideAreas.get(areaName);

                        if (page.size() >= 8) {
                            pages.put(pageIndex++, page);
                            page = new ArrayList<TextComponent>();
                        }

                        BlockPos areaLocation = new BlockPos(prideArea.get("x"), prideArea.get("y"), prideArea.get("z"));
                        double xDiff = Math.abs(areaLocation.getX() - playerLocation.getX());
                        double yDiff = Math.abs(areaLocation.getY() - playerLocation.getY());
                        double zDiff = Math.abs(areaLocation.getZ() - playerLocation.getZ());
                        double totalDiff = Math.abs(xDiff + zDiff + yDiff);

                        String diffString = String.format("%.2f", totalDiff);

                        String areaDescription = String.format("x: %d, y: %d, z: %d", (Integer)areaLocation.getX(), (Integer)areaLocation.getY(), (Integer)areaLocation.getZ());

                        TextComponent hoverComponent = new PrideTextComponentBuilder(areaDescription).build();
                        TextComponent areaComponent = new PrideTextComponentBuilder(areaName).color(TextFormat.BLUE).bold(true).hover(hoverComponent).build();
                        TextComponent distComponent = new PrideTextComponentBuilder(" " + diffString + " blocks away").build();
                        TextComponent pageComponent = areaComponent.append(distComponent);
                        page.add(pageComponent);
                    }

                    if (page.size() > 0) {
                        pages.put(pageIndex, page); // pick up any items in last page < 8
                    }

                    // get page number from arguments
                    int humanPageNumber = IntegerArgumentType.getInteger(context, "pageNumber");
                    Integer pageNumber = humanPageNumber - 1;
                    System.out.println("Total pages = " + pages.size() + " Getting pageNumber = " + pageNumber);

                    if (pageNumber >= pages.size()) {
                        TextComponent message = new PrideTextComponentBuilder("Page not found. There are only " + pages.size() + " pages available.").color(TextFormat.RED).build();
                        player.addChatMessage(message, false);
                        return 1;
                    }
                    
                    TextComponent titleComponent = new PrideTextComponentBuilder("✿  Pride areas page " + humanPageNumber + " of " + pages.size()).color(TextFormat.BLUE).build();
                    player.addChatMessage(titleComponent, false);

                    ArrayList<TextComponent> pageToSend = pages.get(pageNumber);
                    for (TextComponent pageToSendComponent : pageToSend) {
                        player.addChatMessage(pageToSendComponent, false);
                    }

                    return 1;
                }))
        ));
    }

    private void registerSettleCommand() {
        CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
                ServerCommandManager.literal("settle")
                    .then(ServerCommandManager.argument("name", StringArgumentType.greedyString())
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

                        TextComponent message = new PrideTextComponentBuilder("Founded " + areaName + "!").color(TextFormat.GREEN).build();
                        context.getSource().getPlayer().addChatMessage(message, false);
                        return 1;
                    }))
        ));
    }

    private void registerAbandonCommand() {
        CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
                ServerCommandManager.literal("abandon")
                    .then(ServerCommandManager.argument("name", StringArgumentType.greedyString())
                    .executes(context -> {
                        ServerWorld world = context.getSource().getWorld();
                        String areaName = StringArgumentType.getString(context, "name");

                        PridePersistentState persis = PridePersistentState.get(world);
                        persis.removePrideArea(world, areaName);

                        TextComponent message = new PrideTextComponentBuilder("Removed " + areaName + "!").color(TextFormat.GREEN).build();
                        context.getSource().getPlayer().addChatMessage(message, false);
                        return 1;
                    }))
        ));
    }

    private void registerCompassCommand() {
        CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
            ServerCommandManager.literal("compass")
                .then(ServerCommandManager.argument("name", StringArgumentType.greedyString())
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

                    String areaDescription = String.format("x: %d, y: %d, z: %d", (Integer)pos.getX(), (Integer)pos.getY(), (Integer)pos.getZ());
                    
                    TextComponent startTextComponent = new PrideTextComponentBuilder("Compass pointed towards ").color(TextFormat.GREEN).build();
                    TextComponent areaHoverTextComponent = new PrideTextComponentBuilder(areaDescription).build();
                    TextComponent areaTextComponent = new PrideTextComponentBuilder(areaName).color(TextFormat.GOLD).bold(true).hover(areaHoverTextComponent).build();
                    TextComponent endTextComponent = new PrideTextComponentBuilder("!").build();

                    TextComponent concatComponent = startTextComponent.append(areaTextComponent).append(endTextComponent);
                    context.getSource().getPlayer().addChatMessage(concatComponent, false);
                    return 1;
                }))
        ));
    }
}
