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

public class Pride extends JavaPlugin {
	public final String prideFilename = "pride.txt";
	private final PridePlayerListener playerListener = new PridePlayerListener(this, "pride.txt");

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerListener, this);
	 }

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}

		if (commandLabel.equalsIgnoreCase("settle")) {
			return settleCommand(sender, cmd, commandLabel, args);
		} else if (commandLabel.equalsIgnoreCase("abandon")) {
			return abandonCommand(sender, cmd, commandLabel, args);
		} else if (commandLabel.equalsIgnoreCase("here")) {
			return hereCommand(sender, cmd, commandLabel, args);
		} else if (commandLabel.equalsIgnoreCase("far")) {
			return farCommand(sender, cmd, commandLabel, args);
		} else if (commandLabel.equalsIgnoreCase("pride")) {
			return prideCommand(sender, cmd, commandLabel, args);
		}
		
		return false;
	}

	public Boolean settleCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender;
		Location location = player.getLocation();

		String name = "";
		if (args.length > 1) {
			for (String argName : args) {
				name += argName + " ";
			}
			name = name.trim();
		} else {
			name = args[0];
		}

		HashMap map = new HashMap();
		HashMap saved = PrideConfigurator.readPrideAreas(player.getWorld(), prideFilename);
		if (saved != null) {
			map = saved;
		}
		map.put(name, location);
		
		PrideConfigurator.writePrideAreas(prideFilename, map);
		playerListener.prideAreas = saved; // update in-memory store of areas from listener
		
		sender.sendMessage("Created " + ChatColor.BLUE + name + ChatColor.WHITE + "!");

		return true;
	}

	public Boolean abandonCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender;
		Location location = player.getLocation();

		String name = "";
		if (args.length > 1) {
			for (String argName : args) {
				name += argName + " ";
			}
			name = name.trim();
		} else {
			name = args[0];
		}

		HashMap map = new HashMap();
		HashMap saved = PrideConfigurator.readPrideAreas(player.getWorld(), prideFilename);
		if (saved != null) {
			map = saved;
		}
		map.remove(name);
		
		PrideConfigurator.writePrideAreas(prideFilename, map);
		playerListener.prideAreas = saved; // update in-memory store of areas from listener
		
		sender.sendMessage("Removed " + ChatColor.BLUE + name + ChatColor.WHITE + "!");

		return true;
	}

	public Boolean hereCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender;
		Location location = player.getLocation();
		String message = playerListener.formatAreaMessageFromActivatedAreas(playerListener.getActivatedPrideAreas(location));
		if (message == null || message.length() <= 0) {
			sender.sendMessage("You are not currently in a Pride area!");
		} else {
			sender.sendMessage("You are in the following areas: " + ChatColor.BLUE + message + ChatColor.WHITE + "!");
		}
		return true;
	}

	public Boolean farCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender;
		Location location = player.getLocation();
		
		String name = "";
		if (args.length > 1) {
			for (String argName : args) {
				name += argName + " ";
			}
			name = name.trim();
		} else {
			name = args[0];
		}

		String areaName = name;
		HashMap saved = PrideConfigurator.readPrideAreas(player.getWorld(), prideFilename);
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

	public Boolean prideCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender;
		Location playerLocation = player.getLocation();
		HashMap saved = PrideConfigurator.readPrideAreas(player.getWorld(), prideFilename);
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
		return true;
	}
}