package com.lahusa.arcane_auctions.command;

import com.lahusa.arcane_auctions.util.ExperienceConverter;
import com.lahusa.arcane_auctions.util.NumberFormatter;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;

import java.util.Collection;

public class PayCommand {
    private static final SimpleCommandExceptionType ERROR_SINGLE_TARGET = new SimpleCommandExceptionType(Component.literal("Payment target cannot encompass multiple players."));
    private static final SimpleCommandExceptionType ERROR_TRANSACTION_AMOUNT = new SimpleCommandExceptionType(Component.literal("Cannot afford transaction."));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pay")
        .then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((sourceStack, suggestionsBuilder) -> {
            return SharedSuggestionProvider.suggest(sourceStack.getSource().getServer().getPlayerList().getPlayerNamesArray(), suggestionsBuilder);
        })
        .then(Commands.argument("amount", LongArgumentType.longArg(1))
        .executes((sourceStack) -> {
            return payPlayer(sourceStack.getSource(), GameProfileArgument.getGameProfiles(sourceStack, "targets"), LongArgumentType.getLong(sourceStack, "amount"));
        }))));
    }

    private static int payPlayer(CommandSourceStack sourceStack, Collection<GameProfile> playerProfiles, long transactionAmount) throws CommandSyntaxException {
        if (playerProfiles.size() != 1) {
            throw ERROR_SINGLE_TARGET.create();
        }

        // Get target player
        GameProfile playerProfile = (GameProfile) playerProfiles.toArray()[0];
        PlayerList playerList = sourceStack.getServer().getPlayerList();
        ServerPlayer targetPlayer = playerList.getPlayer(playerProfile.getId());

        // Get source player
        ServerPlayer sourcePlayer = sourceStack.getPlayer();

        // Check if source player can afford transaction
        assert sourcePlayer != null;
        long sourcePlayerXP = ExperienceConverter.getTotalCurrentXPPoints(sourcePlayer.experienceLevel, sourcePlayer.experienceProgress);
        boolean xpAmountValid = transactionAmount > 0 && transactionAmount <= sourcePlayerXP;

        if (!xpAmountValid) throw ERROR_TRANSACTION_AMOUNT.create();

        // Execute transaction (Source)
        long newSourcePlayerXP = sourcePlayerXP - transactionAmount;

        float levelsSrc = ExperienceConverter.getLevelsAtXPPoints(newSourcePlayerXP);
        int wholeLevelsSrc = Mth.floor(levelsSrc);
        long sparePointsSrc = newSourcePlayerXP;
        if (wholeLevelsSrc > 0) {
            sparePointsSrc -= ExperienceConverter.getTotalXPRequiredToLevel(wholeLevelsSrc);
        }

        sourcePlayer.setExperienceLevels(wholeLevelsSrc);
        sourcePlayer.setExperiencePoints((int) sparePointsSrc);

        // Execute transaction (Target)
        assert targetPlayer != null;
        long targetPlayerXP = ExperienceConverter.getTotalCurrentXPPoints(targetPlayer.experienceLevel, targetPlayer.experienceProgress);
        long newTargetPlayerXP = targetPlayerXP + transactionAmount;

        float levelsTgt = ExperienceConverter.getLevelsAtXPPoints(newTargetPlayerXP);
        int wholeLevelsTgt = Mth.floor(levelsTgt);
        long sparePointsTgt = newTargetPlayerXP;
        if (wholeLevelsTgt > 0) {
            sparePointsTgt -= ExperienceConverter.getTotalXPRequiredToLevel(wholeLevelsTgt);
        }

        targetPlayer.setExperienceLevels(wholeLevelsTgt);
        targetPlayer.setExperiencePoints((int) sparePointsTgt);

        targetPlayer.sendSystemMessage(
            Component.literal("Received ")
                    .append(Component.literal(NumberFormatter.longToString(transactionAmount)).withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(" points of experience from "))
                    .append(targetPlayer.getDisplayName())
                    .append(Component.literal("."))
        );

        // Mark success
        sourceStack.sendSuccess(() -> {
            return Component.literal("Transferred ")
                    .append(Component.literal(NumberFormatter.longToString(transactionAmount)).withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(" points of experience to "))
                    .append(targetPlayer.getDisplayName())
                    .append(Component.literal("."));
        }, true);

        return 1;
    }
}
