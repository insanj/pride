package me.insanj.pride;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;

public class Pride extends JavaPlugin {
	private final PrideConfiguration config = new PrideConfiguration(this);
	private final PridePlayerListener playerListener = new PridePlayerListener(this, config);

	@Override
	public void onEnable() {
		PrideConfiguration globalConfig = config;
		PridePlayerListener globalPlayerListener = playerListener;

		getCommand("settle").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if (!(sender instanceof Player)) {
					return false;
				}

				Player player = (Player) sender;
				World world = player.getWorld();
				Location location = player.getLocation();
				String name = Pride.areaNameFromArgs(args);
				HashMap saved = globalConfig.getConfigAreas(world);

				saved.put(name, location);
				globalConfig.setConfigAreas(world, saved);		

				sender.sendMessage("Created " + ChatColor.BLUE + name + ChatColor.WHITE + "!");

				return true;
            }
		});

		getCommand("abandon").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if (!(sender instanceof Player)) {
					return false;
				}
				
				Player player = (Player) sender;
				World world = player.getWorld();
				Location location = player.getLocation();
				String name = Pride.areaNameFromArgs(args);
				HashMap saved = globalConfig.getConfigAreas(world);

				saved.remove(name);
				globalConfig.setConfigAreas(world, saved);

				sender.sendMessage("Removed " + ChatColor.BLUE + name + ChatColor.WHITE + "!");
		
				return true;
            }
		});

		getCommand("here").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if (!(sender instanceof Player)) {
					return false;
				}
				
				Player player = (Player) sender;
				Location location = player.getLocation();
				String message = globalPlayerListener.formatAreaMessageFromActivatedAreas(globalPlayerListener.getActivatedPrideAreas(location));
				
				if (message == null || message.length() <= 0) {
					sender.sendMessage("You are not currently in a Pride area!");
				} else {
					sender.sendMessage("You are in the following areas: " + ChatColor.BLUE + message + ChatColor.WHITE + "!");
				}
				
				return true;
            }
		});

		getCommand("far").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if (!(sender instanceof Player)) {
					return false;
				}

				Player player = (Player) sender;
				World world = player.getWorld();
				Location location = player.getLocation();
				String areaName = Pride.areaNameFromArgs(args);
				HashMap saved = globalConfig.getConfigAreas(world);
				Location areaLocation = (Location)saved.get(areaName);
		
				if (areaLocation == null) {
					sender.sendMessage("Could not find an area called " + ChatColor.RED + areaName);
					return true;
				}
		
				// 1 calcualte x
				double xDiff = location.getX() - areaLocation.getX();
				double zDiff = location.getZ() - areaLocation.getZ();
				double yDiff = location.getY() - areaLocation.getY();
		
				sender.sendMessage("How far away you are from " + ChatColor.BLUE + areaName + ChatColor.WHITE + ":");
				sender.sendMessage("x " + ChatColor.GREEN + Double.toString(xDiff));
				sender.sendMessage("y " + ChatColor.GREEN + Double.toString(yDiff));
				sender.sendMessage("z " + ChatColor.GREEN + Double.toString(zDiff));

				return true;
            }
		});

		getCommand("pride").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if (!(sender instanceof Player)) {
					sender.sendMessage("Pride worlds:");
					globalConfig.getConfigWorlds().forEach((k, v) -> {
						sender.sendMessage(ChatColor.BLUE + k.toString() + ChatColor.WHITE + v.toString());
					});
				}

				else {
					Player player = (Player) sender;
					World world = player.getWorld();
					HashMap saved = globalConfig.getConfigAreas(world);
					Location playerLocation = player.getLocation();

					sender.sendMessage("Pride areas:");
					saved.forEach((k, v) -> {
						String name = (String)k;
						Location areaLocation = (Location)v;
						double xDiff = Math.abs(areaLocation.getX() - playerLocation.getX());
						double zDiff = Math.abs(areaLocation.getZ() - playerLocation.getZ());
						double totalDiff = xDiff + zDiff;
						String diffString = String.format("%.2f", totalDiff);
						sender.sendMessage(ChatColor.BLUE + name + ChatColor.WHITE +  " " + diffString + " blocks away");
					});
				}

				return true;
			}
		});

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(playerListener, this);
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