package com.lahusa.arcane_auctions;

import com.lahusa.arcane_auctions.block.ExperienceObeliskBlock;
import com.lahusa.arcane_auctions.block.entity.ExperienceObeliskBlockEntity;
import com.lahusa.arcane_auctions.block.renderer.ExperienceObeliskBlockEntityRenderer;
import com.lahusa.arcane_auctions.command.BalanceCommand;
import com.lahusa.arcane_auctions.command.BuyCommand;
import com.lahusa.arcane_auctions.command.PayCommand;
import com.lahusa.arcane_auctions.gui.menu.ExperienceObeliskMenu;
import com.lahusa.arcane_auctions.gui.screen.ExperienceObeliskScreen;
import com.lahusa.arcane_auctions.item.ExperienceObeliskBlockItem;
import com.lahusa.arcane_auctions.net.ArcaneAuctionsPacketHandler;
import com.lahusa.arcane_auctions.util.ExperienceConverter;
import com.lahusa.arcane_auctions.util.NumberFormatter;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import static com.lahusa.arcane_auctions.ArcaneAuctions.ClientModEvents.GUI_TOGGLE_MAPPING;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ArcaneAuctions.MODID)
public class ArcaneAuctions {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "arcane_auctions";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "arcane_auctions" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "arcane_auctions" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "arcane_auctions" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    // Creates a new Block with the id "arcane_auctions:example_block", combining the namespace and path
    public static final RegistryObject<Block> EXPERIENCE_OBELISK_BLOCK = BLOCKS.register("experience_obelisk", () -> new ExperienceObeliskBlock(
            BlockBehaviour.Properties.of()
                    .strength(35, 1200)
                    .sound(SoundType.AMETHYST)
                    .emissiveRendering((state, getter, pos) -> true)
                    .mapColor(MapColor.COLOR_GREEN)
                    .lightLevel(ExperienceObeliskBlock::getLightLevelForState)
                    .requiresCorrectToolForDrops()
    ));
    // Creates a new BlockItem with the id "arcane_auctions:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXPERIENCE_OBELISK_BLOCK_ITEM = ITEMS.register("experience_obelisk", () -> new ExperienceObeliskBlockItem(
            EXPERIENCE_OBELISK_BLOCK.get(),
            new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
    ));
    public static final RegistryObject<BlockEntityType<ExperienceObeliskBlockEntity>> EXPERIENCE_OBELISK_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("experience_obelisk", () -> BlockEntityType.Builder.of(ExperienceObeliskBlockEntity::new, EXPERIENCE_OBELISK_BLOCK.get()).build(null));

    // Creates a new food item with the id "arcane_auctions:example_id", nutrition 1 and saturation 2
    //public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().alwaysEat().nutrition(1).saturationMod(2f).build())));

    public static final RegistryObject<MenuType<ExperienceObeliskMenu>> EXPERIENCE_OBELISK_MENU = MENU_TYPES.register("experience_obelisk_menu", () -> new MenuType<>(ExperienceObeliskMenu::new, FeatureFlags.DEFAULT_FLAGS));

    // Creates a creative tab with the id "arcane_auctions:example_tab" for the example item, that is placed after the combat tab
    /*public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder().withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> EXAMPLE_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
        output.accept(EXPERIENCE_OBELISK_BLOCK_ITEM.get());
    }).build());*/

    private static final ResourceLocation XP_OVERLAY_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/xp_overlay.png");

    public static boolean showXpPointGui = true;

    public ArcaneAuctions() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register packets
        ArcaneAuctionsPacketHandler.register();

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("Setting up Arcane Auctions");
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) event.accept(EXPERIENCE_OBELISK_BLOCK_ITEM);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        //LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        PayCommand.register(event.getDispatcher());
        BalanceCommand.register(event.getDispatcher());
        BuyCommand.register(event.getDispatcher());
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        public static final Lazy<KeyMapping> GUI_TOGGLE_MAPPING = Lazy.of(() -> new KeyMapping(
                "key.arcane_auctions.toggle_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F9,
                "key.categories.arcane_auctions"
        ));

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            //LOGGER.info("HELLO FROM CLIENT SETUP");
            //LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

            event.enqueueWork(
                    () -> MenuScreens.register(EXPERIENCE_OBELISK_MENU.get(), ExperienceObeliskScreen::new)
            );
        }

        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(EXPERIENCE_OBELISK_BLOCK_ENTITY.get(), ExperienceObeliskBlockEntityRenderer::new);
            LOGGER.info("REGISTERED RENDERER");
        }

        @SubscribeEvent
        public static void registerBinds(RegisterKeyMappingsEvent event) {
            event.register(GUI_TOGGLE_MAPPING.get());
            LOGGER.info("Registered bindings");
        }
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            // Only call code once as the tick event is called twice every tick
            if (event.phase == TickEvent.Phase.END) {
                while (GUI_TOGGLE_MAPPING.get().consumeClick()) {
                    // Toggle GUI visibility
                    ArcaneAuctions.showXpPointGui = !ArcaneAuctions.showXpPointGui;
                }
            }
        }

        @SubscribeEvent
        public static void onRenderGameOverlayEvent(CustomizeGuiOverlayEvent.DebugText event) {
            final Minecraft instance = Minecraft.getInstance();

            // Don't render if F3 Debug Screen is open
            if(instance.options.renderDebug || !Config.showXpPoints || !ArcaneAuctions.showXpPointGui) return;

            GuiGraphics gfx = event.getGuiGraphics();
            LocalPlayer player = instance.player;

            if (player != null) {
                String text = /*"XP: " +*/ NumberFormatter.longToString(ExperienceConverter.getTotalCurrentXPPoints(player.experienceLevel, player.experienceProgress));
                gfx.blit(XP_OVERLAY_LOCATION, 5 + Config.xOffset, 5 + Config.yOffset, 0, 0, 0,10, 10, 10, 10);
                gfx.drawString(instance.font, text, 5+10-2 + Config.xOffset, 5 + Config.yOffset, 0x80FF20, true);
            }
        }
    }


}
