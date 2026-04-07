package com.sshakusora.simplytentsblacklist.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.List;

public class BlacklistConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<List<String>> BLACKLIST_BLOCKS;
    public static final ForgeConfigSpec.ConfigValue<List<String>> BLACKLIST_ITEMS;
    public static final ForgeConfigSpec.ConfigValue<List<String>> BLACKLIST_BLOCK_TAGS;
    public static final ForgeConfigSpec.ConfigValue<List<String>> BLACKLIST_ITEM_TAGS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Simply Tents Blacklist Configuration");
        builder.push("blacklist");

        BLACKLIST_BLOCKS = builder
                .comment("List of block registry names that should not be picked up by tents.",
                        "Format: modid:block_name (e.g., \"minecraft:chest\", \"minecraft:diamond_block\")")
                .define("blacklistBlocks", new ArrayList<>());

        BLACKLIST_ITEMS = builder
                .comment("List of item registry names that should prevent entities/block entities from being picked up.",
                        "If an entity or block entity contains any of these items, it will not be picked up.",
                        "Format: modid:item_name (e.g., \"minecraft:diamond\", \"minecraft:emerald\")")
                .define("blacklistItems", new ArrayList<>());

        BLACKLIST_BLOCK_TAGS = builder
                .comment("List of block tag names that should not be picked up by tents.",
                        "Format: #modid:tag_name (e.g., \"#minecraft:logs\", \"#forge:ores\")")
                .define("blacklistBlockTags", new ArrayList<>());

        BLACKLIST_ITEM_TAGS = builder
                .comment("List of item tag names that should prevent entities/block entities from being picked up.",
                        "If an entity or block entity contains any items with these tags, it will not be picked up.",
                        "Format: #modid:tag_name (e.g., \"#minecraft:logs\", \"#forge:gems\")")
                .define("blacklistItemTags", new ArrayList<>());

        builder.pop();
        SPEC = builder.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
