package me.insanj.pride;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Pride extends JavaPlugin {
	private final PridePlayerListener playerListener = new PridePlayerListener(this);

	@Override
	public void onDisable() { }
	
	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerListener, this);
	 }

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (commandLabel.equalsIgnoreCase("pride")) {
			if (args.length <= 1) {
				sender.sendMessage(ChatColor.RED + "Usage: /pride add <username> or /pride remove <username>");
			} else if (args.length == 2) {
				final Player target = Bukkit.getServer().getPlayer(args[1]);
				if (target == null) {
					sender.sendMessage(ChatColor.RED + "Player not found");
				}
				else if (args[0].equalsIgnoreCase("add")) {
					sender.sendMessage(ChatColor.GREEN + "Adding player to pride");
				}
				else if (args[0].equalsIgnoreCase("remove")) {
					sender.sendMessage(ChatColor.GREEN + "Removing player from pride");
				} else {
					sender.sendMessage(ChatColor.RED + "Unknown command");
				}
			}
		}
	}
}