package com.lahusa.arcane_auctions.gui.menu;

import com.lahusa.arcane_auctions.ArcaneAuctions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExperienceObeliskMenu extends AbstractContainerMenu {
    public Level level;
    private final Inventory _playerInv;
    private final ContainerLevelAccess _access;
    private ContainerData blockPosData;

    // Client
    public ExperienceObeliskMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, null, ContainerLevelAccess.NULL);

        blockPosData = new SimpleContainerData(3);
        this.addDataSlots(blockPosData);
    }

    // Server
    public ExperienceObeliskMenu(int containerId, Inventory playerInv, @Nullable BlockPos pos, ContainerLevelAccess access) {
        super(ArcaneAuctions.EXPERIENCE_OBELISK_MENU.get(), containerId);
        level = playerInv.player.level();
        _playerInv = playerInv;
        _access = access;

        blockPosData = new SimpleContainerData(3);

        if (pos != null) {
            blockPosData.set(0, pos.getX());
            blockPosData.set(1, pos.getY());
            blockPosData.set(2, pos.getZ());
            this.addDataSlots(blockPosData);

            sendAllDataToRemote();
        }
    }

    public BlockPos getBlockPos() {
        return new BlockPos(blockPosData.get(0), blockPosData.get(1), blockPosData.get(2));
    }

    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int slot) {
        return null;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return AbstractContainerMenu.stillValid(_access, player, ArcaneAuctions.EXPERIENCE_OBELISK_BLOCK.get());
    }
}
