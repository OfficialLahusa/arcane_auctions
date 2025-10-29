package com.lahusa.arcane_auctions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = ArcaneAuctions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue SHOW_XP_POINTS = BUILDER.comment("Should the XP point balance be shown in the GUI").define("showXpPoints", true);

    private static final ForgeConfigSpec.IntValue X_OFFSET = BUILDER.comment("XP point GUI x offset").defineInRange("xOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue Y_OFFSET = BUILDER.comment("XP point GUI y offset").defineInRange("yOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean showXpPoints;
    public static int xOffset;
    public static int yOffset;



    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        showXpPoints = SHOW_XP_POINTS.get();
        xOffset = X_OFFSET.get();
        yOffset = Y_OFFSET.get();
    }
}
