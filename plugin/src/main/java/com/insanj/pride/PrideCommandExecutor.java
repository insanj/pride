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
        registerFarCommand();
        registerHereCommand();
        registerBetweenCommand();
        registerNorthCommand();
    }

    private void registerPrideCommand() {
      CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
            ServerCommandManager.literal("pride")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();

                    ArrayList<TextComponent> components = new ArrayList<TextComponent>();
                    components.add(new StringTextComponent("Thanks for using Pride v0.5.3! Commands:"));
                    components.add(new StringTextComponent("/compass <area_name> -- Point compass towards an area"));
                    components.add(new StringTextComponent("/nearby <area_name> -- List nearby areas"));
                    components.add(new StringTextComponent("/areas <page_number> -- List areas alphabetically"));
                    components.add(new StringTextComponent("/settle <area_name> -- Create a new area"));
                    components.add(new StringTextComponent("/abandon <area_name> -- Remove an existing area"));
                    components.add(new StringTextComponent("/far <area_name> -- Check your distance from an area"));
                    components.add(new StringTextComponent("/here -- List the areas at your location"));
                    components.add(new StringTextComponent("/between <area_1>,<area_2> -- Distance between 2 areas"));
                    components.add(new StringTextComponent("/north -- Point compass north from your position"));

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

                    Comparator<String> caseInsensitiveComparator = new Comparator<String>() {
                        @Override
                        public int compare(String s1, String s2) {
                            return s1.toLowerCase().compareTo(s2.toLowerCase());
                        }
                    };
                    
                    Collections.sort(sortedPrideAreaNames, caseInsensitiveComparator);

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

                        double totalDiff = PrideCommandExecutor.distanceBetween(areaLocation, playerLocation);
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

                    if (pageNumber >= pages.size()) {
                        TextComponent message = new PrideTextComponentBuilder("Page not found. There are only " + pages.size() + " pages available.").color(TextFormat.RED).build();
                        player.addChatMessage(message, false);
                        return 1;
                    }
                    
                    TextComponent titleComponent = new PrideTextComponentBuilder("✿  Pride areas page " + humanPageNumber + " of " + pages.size()).build();
                    player.addChatMessage(titleComponent, false);

                    ArrayList<TextComponent> pageToSend = pages.get(pageNumber);
                    for (TextComponent pageToSendComponent : pageToSend) {
                        player.addChatMessage(pageToSendComponent, false);
                    }

                    return 1;
                }))
        ));
    }

    private static double distanceBetween(BlockPos p1, BlockPos p2) {
        double xDiff = Math.abs(p1.getX() - p2.getX());
        double yDiff = Math.abs(p1.getY() - p2.getY());
        double zDiff = Math.abs(p1.getZ() - p2.getZ());
        return Math.abs(xDiff + zDiff + yDiff);
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

                            double d1 = PrideCommandExecutor.distanceBetween(p1, playerLocation);
                            double d2 = PrideCommandExecutor.distanceBetween(p2, playerLocation);

                            return (int)(d1 - d2);
                        }
                    };
                    
                    List<Entry<String, Map<String, Double>>> listOfEntries = new ArrayList<Entry<String, Map<String, Double>>>(entries);
                    Collections.sort(listOfEntries, valueComparator);

                    LinkedHashMap<String, Map<String, Double>> sortedByValue = new LinkedHashMap<String, Map<String, Double>>(listOfEntries.size());

                    Map<Integer, ArrayList<TextComponent>> pages = new HashMap<Integer, ArrayList<TextComponent>>();
                    ArrayList<TextComponent> page = new ArrayList<TextComponent>();
                    Integer pageIndex = 0;

                    for (Entry<String, Map<String, Double>> entry : listOfEntries) {
                        String areaName = entry.getKey();
                        Map<String, Double> prideArea = entry.getValue();

                        if (page.size() >= 8) {
                            pages.put(pageIndex++, page);
                            page = new ArrayList<TextComponent>();
                        }

                        BlockPos areaLocation = new BlockPos(prideArea.get("x"), prideArea.get("y"), prideArea.get("z"));
                        double diff = PrideCommandExecutor.distanceBetween(areaLocation, playerLocation);

                        String diffString = String.format("%.2f", diff);

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

                    if (pageNumber >= pages.size()) {
                        TextComponent message = new PrideTextComponentBuilder("Page not found. There are only " + pages.size() + " pages available.").color(TextFormat.RED).build();
                        player.addChatMessage(message, false);
                        return 1;
                    }
                    
                    TextComponent titleComponent = new PrideTextComponentBuilder("✿  Pride nearby page " + humanPageNumber + " of " + pages.size()).build();
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

                        TextComponent message = new PrideTextComponentBuilder("Settled ").build().append(new PrideTextComponentBuilder(areaName).color(TextFormat.BLUE).build()).append(new PrideTextComponentBuilder("!").build());
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

                        TextComponent message = new PrideTextComponentBuilder("Abandoned ").build().append(new PrideTextComponentBuilder(areaName).color(TextFormat.BLUE).build()).append(new PrideTextComponentBuilder("!").build());
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
                    
                    TextComponent startTextComponent = new PrideTextComponentBuilder("Compass pointed towards ").color(TextFormat.WHITE).build();
                    TextComponent areaHoverTextComponent = new PrideTextComponentBuilder(areaDescription).build();
                    TextComponent areaTextComponent = new PrideTextComponentBuilder(areaName).color(TextFormat.GOLD).bold(true).hover(areaHoverTextComponent).build();
                    TextComponent endTextComponent = new PrideTextComponentBuilder("!").build();

                    TextComponent concatComponent = startTextComponent.append(areaTextComponent).append(endTextComponent);
                    context.getSource().getPlayer().addChatMessage(concatComponent, false);
                    return 1;
                }))
        ));
    }


    private void registerFarCommand() {
        CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
            ServerCommandManager.literal("far")
                .then(ServerCommandManager.argument("name", StringArgumentType.greedyString())
                .executes(context -> {
                    ServerWorld world = context.getSource().getWorld();
                    String areaName = StringArgumentType.getString(context, "name");

                    PridePersistentState persis = PridePersistentState.get(world);
                    Map<String, Double> area = persis.getPrideArea(world, areaName);
                    BlockPos pos = PrideBlockPosUtil.posFromPrideArea(area);

                    ServerPlayerEntity player = context.getSource().getPlayer();
                    BlockPos playerLocation = player.getBlockPos();

                    String areaDescription = String.format("x: %d, y: %d, z: %d", (Integer)pos.getX(), (Integer)pos.getY(), (Integer)pos.getZ());
                    TextComponent startTextComponent = new PrideTextComponentBuilder(areaName).color(TextFormat.GOLD).bold(true).hover(new PrideTextComponentBuilder(areaDescription).build()).build();
                    context.getSource().getPlayer().addChatMessage(startTextComponent, false);

                    double xDiff = Math.abs(pos.getX() - playerLocation.getX());
                    double yDiff = Math.abs(pos.getY() - playerLocation.getY());
                    double zDiff = Math.abs(pos.getZ() - playerLocation.getZ());

                    TextComponent xComponent = new PrideTextComponentBuilder(String.format("x: %.2f", xDiff)).build();
                    context.getSource().getPlayer().addChatMessage(xComponent, false);

                    TextComponent yComponent = new PrideTextComponentBuilder(String.format("y: %.2f", yDiff)).build();
                    context.getSource().getPlayer().addChatMessage(yComponent, false);

                    TextComponent zComponent = new PrideTextComponentBuilder(String.format("z: %.2f", zDiff)).build();
                    context.getSource().getPlayer().addChatMessage(zComponent, false);

                    return 1;
                }))
        ));
    }

    private void registerHereCommand() {
        CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
            ServerCommandManager.literal("here")
                .executes(context -> {
                    ServerWorld world = context.getSource().getWorld();
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    BlockPos playerLocation = player.getBlockPos();

                    PridePersistentState persis = PridePersistentState.get(world);
                    Map<String, Map<String, Double>> prideAreas = persis.getPrideAreas(world);
                    Map<String, Map<String, Double>> activatedAreas = PrideBlockPosUtil.prideAreasInsidePosThreshold(prideAreas, playerLocation, 50.0);

                    if (activatedAreas.size() <= 0) {
                        TextComponent startTextComponent = new PrideTextComponentBuilder("You are not in any Pride areas!").build();
                        context.getSource().getPlayer().addChatMessage(startTextComponent, false);
                        return 1;
                    }
                   
                    List<String> sortedPrideAreaNames = new ArrayList<>(activatedAreas.keySet());
                    Comparator<String> caseInsensitiveComparator = new Comparator<String>() {
                        @Override
                        public int compare(String s1, String s2) {
                            return s1.toLowerCase().compareTo(s2.toLowerCase());
                        }
                    };
                    
                    Collections.sort(sortedPrideAreaNames, caseInsensitiveComparator);

                    TextComponent startTextComponent = new PrideTextComponentBuilder("You are in the following Pride Areas: ").build();
                    context.getSource().getPlayer().addChatMessage(startTextComponent, false);

                    for (String areaName: sortedPrideAreaNames) {
                        Map<String, Double> prideArea = activatedAreas.get(areaName);

                        BlockPos areaLocation = PrideBlockPosUtil.posFromPrideArea(prideArea);
                        double totalDiff = PrideBlockPosUtil.distanceBetween(areaLocation, playerLocation);
                        String diffString = String.format("%.2f", totalDiff);
                        String areaDescription = PrideBlockPosUtil.areaDescription(areaLocation);

                        TextComponent hoverComponent = new PrideTextComponentBuilder(areaDescription).build();
                        TextComponent areaComponent = new PrideTextComponentBuilder(areaName).color(TextFormat.BLUE).bold(true).hover(hoverComponent).build();
                        TextComponent distComponent = new PrideTextComponentBuilder(" " + diffString + " blocks away").build();
                        TextComponent pageComponent = areaComponent.append(distComponent);
                        context.getSource().getPlayer().addChatMessage(pageComponent, false);
                    }

                    return 1;
                }))
        );
    }

    private void registerNorthCommand() {
        CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
            ServerCommandManager.literal("north")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    BlockPos playerLocation = player.getBlockPos();
                    BlockPos northPos = new BlockPos(playerLocation.getX(), playerLocation.getY(), Math.abs(playerLocation.getZ()) * -999.0);

                    PlayerSpawnPositionS2CPacket packet = new PlayerSpawnPositionS2CPacket(northPos);
                    player.networkHandler.sendPacket(packet);
                    
                    TextComponent component = new PrideTextComponentBuilder("Compass pointed north!").build();
                    context.getSource().getPlayer().addChatMessage(component, false);
                    return 1;
                }))
        );
    }

    private void registerBetweenCommand() {
        CommandRegistry.INSTANCE.register(false, serverCommandSourceCommandDispatcher -> serverCommandSourceCommandDispatcher.register(
            ServerCommandManager.literal("between")
                .then(ServerCommandManager.argument("names", StringArgumentType.greedyString())
                .executes(context -> {
                    ServerWorld world = context.getSource().getWorld();                    
                    PridePersistentState persis = PridePersistentState.get(world);

                    String argString = StringArgumentType.getString(context, "names");
                    String[] argSplit = argString.split(",");
                    if (argSplit.length != 2) {
                        TextComponent component = new PrideTextComponentBuilder("☹  Use a comma to separate the two Pride areas").color(TextFormat.RED).build();
                        context.getSource().getPlayer().addChatMessage(component, false);
                        return 1;
                    }


                    String firstAreaName = argSplit[0].trim();
                    Map<String, Double> firstPrideArea = persis.getPrideArea(world, firstAreaName);

                    if (firstPrideArea == null) {
                        TextComponent component = new PrideTextComponentBuilder("☹  Could not find area: " + firstAreaName).color(TextFormat.RED).build();
                        context.getSource().getPlayer().addChatMessage(component, false);
                        return 1;
                    }

                    String secondAreaName = argSplit[1].trim();
                    Map<String, Double> secondPrideArea = persis.getPrideArea(world, secondAreaName);

                    if (secondPrideArea == null) {
                        TextComponent component = new PrideTextComponentBuilder("☹  Could not find area: " + secondAreaName).color(TextFormat.RED).build();
                        context.getSource().getPlayer().addChatMessage(component, false);
                        return 1;
                    }


                    BlockPos firstAreaLocation = PrideBlockPosUtil.posFromPrideArea(firstPrideArea);
                    BlockPos secondAreaLocation = PrideBlockPosUtil.posFromPrideArea(secondPrideArea);

                    double xDiff = Math.abs(firstAreaLocation.getX() - secondAreaLocation.getX());
                    double yDiff = Math.abs(firstAreaLocation.getY() - secondAreaLocation.getY());
                    double zDiff = Math.abs(firstAreaLocation.getZ() - secondAreaLocation.getZ());

                    TextComponent betweenComponent = new PrideTextComponentBuilder("✄  Distance between ").build().append(new PrideTextComponentBuilder(firstAreaName).color(TextFormat.BLUE).build()).append(new PrideTextComponentBuilder(" -> ").build()).append(new PrideTextComponentBuilder(secondAreaName).color(TextFormat.BLUE).build());
                    context.getSource().getPlayer().addChatMessage(betweenComponent, false);

                    TextComponent xComponent = new PrideTextComponentBuilder(String.format("x: %.2f", xDiff)).build();
                    context.getSource().getPlayer().addChatMessage(xComponent, false);

                    TextComponent yComponent = new PrideTextComponentBuilder(String.format("y: %.2f", yDiff)).build();
                    context.getSource().getPlayer().addChatMessage(yComponent, false);

                    TextComponent zComponent = new PrideTextComponentBuilder(String.format("z: %.2f", zDiff)).build();
                    context.getSource().getPlayer().addChatMessage(zComponent, false);

                    return 1;
                }))
        ));
    }
}
