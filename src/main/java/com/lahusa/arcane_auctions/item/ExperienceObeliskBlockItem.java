package com.lahusa.arcane_auctions.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExperienceObeliskBlockItem extends BlockItem {
    public ExperienceObeliskBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);

        components.add(Component.literal("A pulsating monolith that stores orbs of experience for safekeeping.").withStyle(ChatFormatting.GRAY));
        components.add(Component.literal(""));
        components.add(Component.literal("WARNING: Contained experience is lost when broken!").withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.BOLD));
    }
}
