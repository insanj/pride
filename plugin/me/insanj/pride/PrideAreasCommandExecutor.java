package me.insanj.pride;

import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Math;

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

public class PrideAreasCommandExecutor implements CommandExecutor {
    private final PrideConfiguration config;


    public PrideAreasCommandExecutor(PrideConfiguration config) {
        this.config = config;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("✿  Pride worlds:");
            this.config.getConfigWorlds().forEach((k, v) -> {
                sender.sendMessage(ChatColor.BLUE + k.toString() + ChatColor.WHITE + v.toString());
            });
        }

        else {
            Player player = (Player) sender;

            World world = player.getWorld();
            HashMap saved = this.config.getConfigAreas(world);
            if (saved == null) {
                sender.sendMessage(ChatColor.RED + "☹  Pride area not found");
                return false;
            }

            Location playerLocation = player.getLocation();

            // mini-routine:
            // 1. sort all areas by name OR distance (TODO allow for providing custom world arg)
            // 2. split up into groups of 8, each representing a "page"
            // 3. render page 1 and allow for going to next page with arg
            // Example: /areas <page_number>
            // Output:
            //      PRIDE AREAS in WORLD
            //      Page 1 of 10
            //      AREA NAME ---- LOCATION
            // Future: /areas <page_number> <world_name>
            
            HashMap<Object, Object> janky = (HashMap<Object, Object>)saved;
            HashMap<Integer, ArrayList<String>> pages = new HashMap<Integer, ArrayList<String>>();
            Integer pageIndex = 0;
            ArrayList<String> page = new ArrayList<String>();
            for (HashMap.Entry<Object, Object> entry : janky.entrySet()) {
                Object k = entry.getKey();
                Object v = entry.getValue();

                if (page.size() >= 8) {
                    pages.put(pageIndex++, page);
                    page = new ArrayList<String>();
                }

                String name = (String)k;
                Location areaLocation = (Location)v;
                double xDiff = Math.abs(areaLocation.getX() - playerLocation.getX());
                double zDiff = Math.abs(areaLocation.getZ() - playerLocation.getZ());
                double totalDiff = xDiff + zDiff;
                String diffString = String.format("%.2f", totalDiff);
                String message = ChatColor.BLUE + name + ChatColor.WHITE +  " " + diffString + " blocks away";
                page.add(message);
            }

            if (page.size() > 0) {
                pages.put(pageIndex, page); // pick up any items in last page < 8
            }

            // get page number from arguments
            Integer pageNumber = 0;
            if (args.length > 0) {
                pageNumber = Integer.parseInt(args[0]) - 1; // for humans! start counting at 1
            }

            System.out.println("Total pages = " + pages.size() + " Getting pageNumber = " + pageNumber);
            if (pageNumber >= pages.size()) {
                sender.sendMessage(ChatColor.RED + "Page not found. There are only " + pages.size() + " pages available.");
                return true;
            }
            
            Integer humanPageNumber = pageNumber + 1;
            sender.sendMessage("✿  Pride areas page " + humanPageNumber + " of " + pages.size());
            ArrayList<String> pageToSend = pages.get(pageNumber);
            for (String message : pageToSend) {
                sender.sendMessage(message);
            }
        }

        return true;
    }
}