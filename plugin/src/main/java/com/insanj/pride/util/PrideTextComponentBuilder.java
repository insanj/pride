package com.insanj.pride.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

import net.minecraft.server.command.CommandManager;
import net.minecraft.text.StringTextComponent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.inventory.Inventory;
import net.minecraft.block.Block;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.EquipmentSlot;

import net.minecraft.item.MiningToolItem;
import net.minecraft.server.world.BlockAction;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.Style;
import net.minecraft.text.TextComponent;
import net.minecraft.text.StringTextComponent;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.BlockView;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.client.network.packet.PlayerSpawnPositionS2CPacket;
import net.minecraft.text.Style;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.text.event.HoverEvent;
import net.minecraft.text.event.ClickEvent;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import com.google.gson.Gson;
import com.google.common.collect.Multimap;

public class PrideTextComponentBuilder {
    private StringTextComponent underlyingTextComponent;
    private Style underlyingStyle;

    public PrideTextComponentBuilder(String message) {
      this.underlyingTextComponent = new StringTextComponent(message);
      this.underlyingStyle = new Style();

      setupDefaults();
    }

    public void setupDefaults() {
        this.color(TextFormat.WHITE);
        this.bold(false);
    }

    public PrideTextComponentBuilder color(TextFormat c) {
        this.underlyingStyle.setColor(c);
        return this;
    }

    public PrideTextComponentBuilder bold(boolean b) {
        this.underlyingStyle.setBold(b);
        return this;
    }

    public PrideTextComponentBuilder hover(TextComponent c) {
        HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, c);
        this.underlyingStyle.setHoverEvent(event);
        return this;
    }

    public PrideTextComponentBuilder click(String c) {
        ClickEvent event = new ClickEvent(ClickEvent.Action.RUN_COMMAND, c);
        this.underlyingStyle.setClickEvent(event);
        return this;
    }

    public TextComponent build() {
        this.underlyingTextComponent.setStyle(this.underlyingStyle);
        return this.underlyingTextComponent;
    }
}
