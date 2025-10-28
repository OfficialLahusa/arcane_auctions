package com.lahusa.arcane_auctions.command;

import com.lahusa.arcane_auctions.util.ExperienceConverter;
import com.lahusa.arcane_auctions.util.NumberFormatter;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
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

public class BalanceCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> literalcommandnode = dispatcher.register(Commands.literal("balance")
        .requires((sourceStack) -> {
            return sourceStack.hasPermission(0);
        })
        .executes((sourceStack) -> {
            return getBalance(sourceStack.getSource());
        })
        .then(Commands.literal("get")
        .requires((sourceStack) -> {
            return sourceStack.hasPermission(3);
        })
        .then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((sourceStack, suggestionsBuilder) -> {
            return SharedSuggestionProvider.suggest(sourceStack.getSource().getServer().getPlayerList().getPlayerNamesArray(), suggestionsBuilder);
        })
        .executes((sourceStack) -> {
            return getBalanceMultiple(sourceStack.getSource(), GameProfileArgument.getGameProfiles(sourceStack, "targets"));
        })))
        .then(Commands.literal("set")
        .requires((sourceStack) -> {
            return sourceStack.hasPermission(3);
        })
        .then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((sourceStack, suggestionsBuilder) -> {
            return SharedSuggestionProvider.suggest(sourceStack.getSource().getServer().getPlayerList().getPlayerNamesArray(), suggestionsBuilder);
        })
        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
        .executes((sourceStack) -> {
            return setBalanceMultiple(sourceStack.getSource(), GameProfileArgument.getGameProfiles(sourceStack, "targets"), IntegerArgumentType.getInteger(sourceStack, "amount"));
        })))));

        dispatcher.register(Commands.literal("bal")
        .requires((sourceStack) -> {
            return sourceStack.hasPermission(0);
        })
        .executes((sourceStack) -> {
            return getBalance(sourceStack.getSource());
        })
        .redirect(literalcommandnode));
    }

    private static int getBalance(CommandSourceStack sourceStack) throws CommandSyntaxException {
        // Get player
        ServerPlayer player = sourceStack.getPlayer();

        assert player != null;
        int playerXP = ExperienceConverter.getTotalCurrentXPPoints(player.experienceLevel, player.experienceProgress);

        // Mark success
        sourceStack.sendSuccess(() -> {
            return Component.literal("Current balance: ")
                    .append(Component.literal(NumberFormatter.intToString(playerXP)).withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(" points of experience."));
        }, true);

        return 1;
    }

    private static int getBalanceMultiple(CommandSourceStack sourceStack, Collection<GameProfile> playerProfiles) throws CommandSyntaxException {
        PlayerList playerList = sourceStack.getServer().getPlayerList();

        for (GameProfile profile : playerProfiles) {
            ServerPlayer player = playerList.getPlayer(profile.getId());

            assert player != null;
            int playerXP = ExperienceConverter.getTotalCurrentXPPoints(player.experienceLevel, player.experienceProgress);

            // Mark success
            sourceStack.sendSuccess(() -> {
                return Component.literal("")
                        .append(player.getDisplayName())
                        .append(Component.literal("'s current balance: "))
                        .append(Component.literal(NumberFormatter.intToString(playerXP)).withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(" points of experience."));
            }, true);
        }

        return 1;
    }

    private static int setBalanceMultiple(CommandSourceStack sourceStack, Collection<GameProfile> playerProfiles, int newBalance) throws CommandSyntaxException {
        PlayerList playerList = sourceStack.getServer().getPlayerList();

        for (GameProfile profile : playerProfiles) {
            ServerPlayer player = playerList.getPlayer(profile.getId());

            assert player != null;

            int targetPlayerXP = ExperienceConverter.getTotalCurrentXPPoints(player.experienceLevel, player.experienceProgress);

            float levelsTgt = ExperienceConverter.getLevelsAtXPPoints(newBalance);
            int wholeLevelsTgt = Mth.floor(levelsTgt);
            int sparePointsTgt = newBalance;
            if (wholeLevelsTgt > 0) {
                sparePointsTgt -= ExperienceConverter.getTotalXPRequiredToLevel(wholeLevelsTgt);
            }

            player.setExperienceLevels(wholeLevelsTgt);
            player.setExperiencePoints(sparePointsTgt);

            // Mark success
            sourceStack.sendSuccess(() -> {
                return Component.literal("Set ")
                        .append(player.getDisplayName())
                        .append(Component.literal("'s balance to "))
                        .append(Component.literal(NumberFormatter.intToString(newBalance)).withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(" points of experience (was "))
                        .append(Component.literal(NumberFormatter.intToString(targetPlayerXP)).withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(")."));
            }, true);
        }

        return 1;
    }
}
