package com.ilcheese2.bubblelife;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = DetachedTimes.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DetachedTimesConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER.comment("Whether to log the dirt block on common setup").define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue BUBBLE_DURATION = BUILDER.comment("How long bubbles should last in ticks").defineInRange("bubbleDuration", 20 * 20, 5, 20 * 60);
    public static final ModConfigSpec.IntValue BUBBLE_COOLDOWN = BUILDER.comment("Time before players can create a new bubble in ticks").defineInRange("bubbleColdown", 20 * 30, 5, 20 * 60);
    public static final ModConfigSpec.IntValue BUBBLE_BUFFER_SIZE = BUILDER.comment("How large the buffer for bubbles is in floats").defineInRange("bubbleBufferSize", 1024, 100, 10000);
    public static final ModConfigSpec.BooleanValue AFFECTS_PLAYERS = BUILDER.comment("Whether bubbles affect the tickrate of players").define("bubbleCooldown", false);
    public static final ModConfigSpec.IntValue REWIND_TIME = BUILDER.comment("How long rewinds last in ticks").defineInRange("rewindTime", 20 * 3, 5, 20 * 60);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER.comment("What you want the introduction message to be for the magic number").define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    //private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER.comment("A list of items to log on common setup.").defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        logDirtBlock = LOG_DIRT_BLOCK.get();
//        bubbleDuration = BUBBLE_DURATION.get();
//
//        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        //items = ITEM_STRINGS.get().stream().map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName))).collect(Collectors.toSet());
    }
}
