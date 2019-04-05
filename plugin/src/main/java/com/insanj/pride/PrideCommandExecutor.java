package com.insanj.pride;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.block.FabricBlockSettings;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.block.BlockItem;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.text.StringTextComponent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.Identifier;

public class PrideCommandExecutor {
  private final PrideMod plugin;
  private final PrideConfiguration config;
  private final PridePlayerListener playerListener;

  public PrideCommandExecutor(PrideMod plugin, PrideConfiguration config, PridePlayerListener playerListener) {
    this.plugin = plugin;
    this.config = config;
    this.playerListener = playerListener;
  }

  public void register() {
    Pride globalPlugin = plugin;
    PrideConfiguration globalConfig = config;
    PridePlayerListener globalPlayerListener = playerListener;

    getCommand("areas").setExecutor(new PrideAreasCommandExecutor(config));

    getCommand("settle").setExecutor(new CommandExecutor() {
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length <= 0) {
                sender.sendMessage(ChatColor.RED + "☹  Need to give area name in order to settle it");
                return false;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "☹  Need to be a player to execute this command because it uses your current world");
                return false;
            }

            Player player = (Player) sender;
            World world = player.getWorld();
            Location location = player.getLocation();
            String name = Pride.areaNameFromArgs(args);
            HashMap saved = globalConfig.getConfigAreas(world);
            if (saved == null) {
                sender.sendMessage(ChatColor.RED + "☹  Pride area not found");
                return false;
            }

            saved.put(name, location);
            globalConfig.setConfigAreas(world, saved);    

            sender.sendMessage("☆  Created " + ChatColor.BLUE + name + ChatColor.WHITE + "!");

            return true;
        }
    });

    getCommand("abandon").setExecutor(new CommandExecutor() {
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length <= 0) {
                sender.sendMessage(ChatColor.RED + "☹  Need to give area name in order to abandon it");
                return false;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "☹  Need to be a player to execute this command because it uses your current world");
                return false;
            }
            
            Player player = (Player) sender;
            World world = player.getWorld();
            Location location = player.getLocation();
            String name = Pride.areaNameFromArgs(args);
            HashMap saved = globalConfig.getConfigAreas(world);
            if (saved == null) {
                sender.sendMessage(ChatColor.RED + "☹  Pride area not found");
                return false;
            }

            saved.remove(name);
            globalConfig.setConfigAreas(world, saved);

            sender.sendMessage("☆  Removed " + ChatColor.BLUE + name + ChatColor.WHITE + "!");
    
            return true;
        }
    });

    getCommand("here").setExecutor(new CommandExecutor() {
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            Player player;
            String playerName;
            if (args.length == 1) {
                playerName = args[0];
                player = Bukkit.getPlayer(playerName);
                if (player == null) {
                    sender.sendMessage("☹  Player not found with name:" + playerName);
                    return false;
                }
            } else if (!(sender instanceof Player)) {
                sender.sendMessage("☹  Cannot get player location if you are not a player");
                return false;
            } else {
                player = (Player)sender;
                playerName = player.getName();
            }
            
            Location location = player.getLocation();
            String message = globalPlayerListener.formatAreaMessageFromActivatedAreas(globalPlayerListener.getActivatedPrideAreas(location));
            
            if (message == null || message.length() <= 0) {
                sender.sendMessage("☆  " + ChatColor.GREEN + playerName + ChatColor.WHITE + " is not currently in a Pride area!");
            } else {
                sender.sendMessage("☆  " + ChatColor.GREEN + playerName + ChatColor.WHITE + " is in the following areas: " + ChatColor.BLUE + message + ChatColor.WHITE + "!");
            }
            
            return true;
        }
    });

    getCommand("far").setExecutor(new CommandExecutor() {
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length <= 0) {
                sender.sendMessage(ChatColor.RED + "☹  Need to give area name in order to figure out how far you are from it");
                return false;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "☹  Need to be a player to execute this command because it uses your current location");
                return false;
            }

            Player player = (Player) sender;
            World world = player.getWorld();
            Location location = player.getLocation();
            String areaName = Pride.areaNameFromArgs(args);
            HashMap saved = globalConfig.getConfigAreas(world);
            if (saved == null) {
                sender.sendMessage(ChatColor.RED + "☹  Pride area not found");
                return false;
            }

            Location areaLocation = (Location)saved.get(areaName);
            if (areaLocation == null) {
                sender.sendMessage("☹  Could not find an area called " + ChatColor.RED + areaName);
                return false;
            }
    
            // 1 calcualte x
            double xDiff = location.getX() - areaLocation.getX();
            double zDiff = location.getZ() - areaLocation.getZ();
            double yDiff = location.getY() - areaLocation.getY();
    
            sender.sendMessage("✄  How far away you are from " + ChatColor.BLUE + areaName + ChatColor.WHITE + ":");
            sender.sendMessage("x " + ChatColor.GREEN + Double.toString(xDiff));
            sender.sendMessage("y " + ChatColor.GREEN + Double.toString(yDiff));
            sender.sendMessage("z " + ChatColor.GREEN + Double.toString(zDiff));

            return true;
        }
    });

    getCommand("between").setExecutor(new CommandExecutor() {
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length <= 0) {
                sender.sendMessage(ChatColor.RED + "☹  Need to give area name in order to figure out how far you are from it");
                return false;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "☹  Need to be a player to execute this command because it uses your world");
                return false;
            }

            Player player = (Player) sender;
            World world = player.getWorld();
            HashMap saved = globalConfig.getConfigAreas(world);
            if (saved == null) {
                sender.sendMessage(ChatColor.RED + "☹  Pride area not found");
                return false;
            }

            String argString = "";
            for (String arg : args) {
                argString += arg + " ";
            }

            String[] argSplit = argString.split(",");
            if (argSplit.length != 2) {
                sender.sendMessage(ChatColor.RED + "☹  Use a comma to separate the two Pride areas");
                return false;
            }

            String firstAreaName = argSplit[0].trim();
            String secondAreaName = argSplit[1].trim();

            Location firstAreaLocation = (Location)saved.get(firstAreaName);
            if (firstAreaLocation == null) {
                sender.sendMessage("☹  Could not find area: " + ChatColor.RED + firstAreaName);
                return false;
            }

            Location secondAreaLocation = (Location)saved.get(secondAreaName);
            if (secondAreaLocation == null) {
                sender.sendMessage("☹  Could not find area: " + ChatColor.RED + secondAreaName);
                return false;
            }
    
            // 1 calcualte x
            double xDiff = firstAreaLocation.getX() - secondAreaLocation.getX();
            double zDiff = firstAreaLocation.getZ() - secondAreaLocation.getZ();
            double yDiff = firstAreaLocation.getY() - secondAreaLocation.getY();
    
            sender.sendMessage("✄  Distance between " + ChatColor.BLUE + firstAreaName + ChatColor.WHITE + " -> " + ChatColor.BLUE + secondAreaName + ChatColor.WHITE + ":");
            sender.sendMessage("x " + ChatColor.GREEN + Double.toString(xDiff));
            sender.sendMessage("y " + ChatColor.GREEN + Double.toString(yDiff));
            sender.sendMessage("z " + ChatColor.GREEN + Double.toString(zDiff));

            return true;
        }
    });

    getCommand("compass").setExecutor(new CommandExecutor() {
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "☹  Need to be a player to execute this command because it uses your compass");
                return false;
            }

            Player player = (Player) sender;
            World world = player.getWorld();

            if (args.length <= 0) {
                Location originalCompassLocation = world.getSpawnLocation​();
                player.setCompassTarget(originalCompassLocation);
                sender.sendMessage("➹  Compass reset to your world's original spawn!");
                return true;
            }

            String areaName = Pride.areaNameFromArgs(args);
            HashMap saved = globalConfig.getConfigAreas(world);
            if (saved == null) {
                sender.sendMessage(ChatColor.RED + "☹  Pride area not found");
                return false;
            }

            Location areaLocation = (Location)saved.get(areaName);
            if (areaLocation == null) {
                sender.sendMessage("☹  Could not find an area called " + ChatColor.RED + areaName);
                return false;
            }

            player.setCompassTarget(areaLocation);
            sender.sendMessage("➹  Compass pointed to " + ChatColor.BLUE + areaName + ChatColor.WHITE + "!");
            return true;
        }
    });

    getCommand("north").setExecutor(new CommandExecutor() {
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "☹  Need to be a player to execute this command because it uses your compass");
                return false;
            }

            else {
                Player player = (Player) sender;
                World world = player.getWorld();

                if (args.length > 0) {
                    Location originalCompassLocation = world.getSpawnLocation​();
                    player.setCompassTarget(originalCompassLocation);
                    sender.sendMessage("➹  Compass reset to your world's original spawn!");
                    return true;
                }

                Location location = player.getLocation();
                Location northLocation = new Location(world, location.getX(), location.getY(), Math.abs(location.getZ()) * -999.0);
                player.setCompassTarget(northLocation);

                sender.sendMessage("➹  Compass pointed " + ChatColor.RED + " true north " + ChatColor.WHITE + " based on your current location!");
            }

            return true;
        }
    });

    PluginManager pm = getServer().getPluginManager();
    pm.registerEvents(playerListener, this);

    registerPrideRecipe();
  }

  static String areaNameFromArgs(String[] args) {
      String name = "";
      if (args.length > 1) {
          for (String argName : args) {
              name += argName + " ";
          }
          name = name.trim();
      } else {
          name = args[0];
      }
      return name;
  }
}