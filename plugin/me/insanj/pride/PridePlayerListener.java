package me.insanj.pride;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PridePlayerListener implements Listener {
    private final Pride plugin;

    public PridePlayerListener(Pride instance) {
        plugin = instance;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getLogger().info(event.getPlayer().getName() + " joined the server! :D");
    }

    /*@EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) { }
    */

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        plugin.getLogger().info(String.format("From %.2f,%.2f,%.2f to %.2f,%.2f,%.2f", from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ()));
    }

    public void detectPride() {
        // purpose: use a given location to determine if a player has entered/exited an area
        Location playerLocation; // current location
        Location[] playerAreaHistory; // last time this player was detected within an area
        Location[] prideAreas; // detection areas
        Float threshold = 200; // how close you have to be to an area to activate it

        // implementation: go through areas and see if playerLocation is within 200 pixels of a prideArea

        // result: send a message to the server if a player has entered an area


    }
}