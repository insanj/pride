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
			if (args.length == 1 && args[0] == "where") {
				Location location = player.getLocation();
				String message = playerListener.formatAreaMessageFromActivatedAreas(getActivatedPrideAreas(location));
				sender.sendMessage("You are in the following areas: " + ChatColor.Blue + message + ChatColor.WHITE + "!");
				return true;
			}

			if (args.length != 2) {
				sender.sendMessage(ChatColor.RED + "Usage: /pride add or /pride remove");
				return true;
			}
			
			if (args[0].equalsIgnoreCase("add")) {
				Player player = (Player) sender;
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

				return true;
			}		
		}

		return false;
	}
}