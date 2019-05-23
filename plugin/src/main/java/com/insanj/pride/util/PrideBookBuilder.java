package com.insanj.pride.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;
import java.text.SimpleDateFormat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.entity.EntityTrackingRegistry;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.fabricmc.fabric.impl.server.EntityTrackerStorageAccessor;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.Material;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.BookItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.Style;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.text.NbtTextComponent;
import net.minecraft.text.event.HoverEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkPos;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import com.google.gson.Gson;

public class PrideBookBuilder {
  private ItemStack underlyingItemStack;
  private CompoundTag underlyingBookMeta;

  public PrideBookBuilder() {
    this.underlyingItemStack = new ItemStack(new WrittenBookItem(new WrittenBookItem.Settings()), 1);
    this.underlyingBookMeta = this.underlyingItemStack.getTag();
  }

  /*public static String dateString() {
      return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
  }*/

  public PrideBookBuilder name(String displayName) {
      TranslatableTextComponent textComponentName = new TranslatableTextComponent(displayName);
      this.underlyingItemStack.setDisplayName(textComponentName);
      return this;
  }

  public PrideBookBuilder author(String authorName) {
      this.underlyingBookMeta.putString("author", authorName);
      return this;
  }

  public PrideBookBuilder title(String titleString) {
      this.underlyingBookMeta.putString("title", titleString);
      return this;
  }

  public PrideBookBuilder setPages(List<String> pages) {
      ListTag pagesList = new ListTag();
      for (int i = 0; i < pages.size(); i++) {
        String pageText = pages.get(i);
        String jsonPageText = String.format("{\"text\":\"%s\"}", pageText);
        pagesList.set(i, new StringTag(jsonPageText));
      }

      this.underlyingBookMeta.put("pages", pagesList);
      return this;
  }

  public ItemStack build() {
    this.underlyingItemStack.setTag(this.underlyingBookMeta);
    return this.underlyingItemStack;
  }
}
