package com.lahusa.arcane_auctions.command;

import com.lahusa.arcane_auctions.util.ExperienceConverter;
import com.lahusa.arcane_auctions.util.NumberFormatter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ObjectHolder;

public class BuyCommand {
    @ObjectHolder(registryName = "minecraft:item", value = "skilltree:wisdom_scroll")
    public static final Item WISDOM_SCROLL = null;
    public static final int SCROLL_COST = 5000;

    private static final SimpleCommandExceptionType ERROR_MISSING_ITEM = new SimpleCommandExceptionType(Component.literal("Item type not found."));


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("buy")
        .then(Commands.literal("wisdom_scroll")
        .executes((sourceStack) -> {
            return buyScroll(sourceStack.getSource());
        })));
    }

    private static int buyScroll(CommandSourceStack sourceStack) throws CommandSyntaxException {
        // Get source player
        ServerPlayer sourcePlayer = sourceStack.getPlayer();

        // Check if source player can afford transaction
        assert sourcePlayer != null;
        long sourcePlayerXP = ExperienceConverter.getTotalCurrentXPPoints(sourcePlayer.experienceLevel, sourcePlayer.experienceProgress);
        boolean xpAmountValid = SCROLL_COST <= sourcePlayerXP;

        if (!xpAmountValid) throw new SimpleCommandExceptionType(
                Component.literal("Insufficient XP to buy wisdom scroll for 5,000xp (Missing " + NumberFormatter.longToString(SCROLL_COST - sourcePlayerXP) + "xp).")
        ).create();

        // Execute transaction
        if (WISDOM_SCROLL != null) {
            sourcePlayer.addItem(new ItemStack(WISDOM_SCROLL));
        }
        else {
            throw ERROR_MISSING_ITEM.create();
        }

        long newSourcePlayerXP = sourcePlayerXP - SCROLL_COST;

        float levelsSrc = ExperienceConverter.getLevelsAtXPPoints(newSourcePlayerXP);
        int wholeLevelsSrc = Mth.floor(levelsSrc);
        long sparePointsSrc = newSourcePlayerXP;
        if (wholeLevelsSrc > 0) {
            sparePointsSrc -= ExperienceConverter.getTotalXPRequiredToLevel(wholeLevelsSrc);
        }

        sourcePlayer.setExperienceLevels(wholeLevelsSrc);
        sourcePlayer.setExperiencePoints((int) sparePointsSrc);

        // Mark success
        sourceStack.sendSuccess(() -> {
            return Component.literal("Bought wisdom scroll for ")
                    .append(Component.literal(NumberFormatter.longToString(SCROLL_COST)).withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(" points of experience."));
        }, true);

        return 1;
    }
}
