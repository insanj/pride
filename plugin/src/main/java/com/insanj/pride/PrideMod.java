package com.insanj.pride;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Math;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.lang.reflect.Constructor;

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

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

public class PrideMod implements ModInitializer {
    private final PrideConfiguration config = new PrideConfiguration(this);
    private final PridePlayerListener playerListener = new PridePlayerListener(this, config);

    
    
/*
    public void registerPrideRecipe() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta bm = (BookMeta)book.getItemMeta();
        bm.setAuthor("Pride");
        bm.setTitle("Areas " + new Date().toString());

        ArrayList<String> pages = new ArrayList<String>();
        this.config.getConfigWorlds().forEach((k, v) -> {
            pages.add("World " + ChatColor.BLUE + k.toString() + "\n" + ChatColor.BLACK + "Areas: " + ChatColor.GREEN + v.toString());
        });

        bm.setPages(pages);
        book.setItemMeta(bm);

        ItemMeta meta = book.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Pride Book");
        book.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(this, "pride");
        ShapedRecipe recipe = new ShapedRecipe(key, book);
        recipe.shape("S");
        recipe.setIngredient('S', Material.ACACIA_SAPLING);
        Bukkit.addRecipe(recipe);
    }
*/


}