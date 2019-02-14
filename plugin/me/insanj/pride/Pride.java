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

		if (commandLabel.equalsIgnoreCase("pride")) {
			Player player = (Player) sender;

			if (args.length == 1 && args[0].equalsIgnoreCase("where")) {
				Location location = player.getLocation();
				String message = playerListener.formatAreaMessageFromActivatedAreas(playerListener.getActivatedPrideAreas(location));
				if (message.length() <= 0) {
					sender.sendMessage("You are not currently in a Pride area!");
				} else {
					sender.sendMessage("You are in the following areas: " + ChatColor.BLUE + message + ChatColor.WHITE + "!");
				}
				return true;
			}

			if (args.length != 2) {
				sender.sendMessage(ChatColor.RED + "Usage: /pride add or /pride remove");
				return true;
			}
			
			if (args[0].equalsIgnoreCase("add")) {
				Location location = player.getLocation();
				String name = args[1];

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
		}

		return false;
	}
}