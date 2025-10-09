package com.lahusa.arcane_auctions.block.entity;

import com.lahusa.arcane_auctions.ArcaneAuctions;
import com.lahusa.arcane_auctions.util.ExperienceConverter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExperienceObeliskBlockEntity extends BlockEntity {
    private int _experiencePoints;

    public ExperienceObeliskBlockEntity(BlockPos pos, BlockState state) {
        super(ArcaneAuctions.EXPERIENCE_OBELISK_BLOCK_ENTITY.get(), pos, state);

        _experiencePoints = 1000;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("experience_points", _experiencePoints);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        _experiencePoints = tag.getInt("experience_points");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("experience_points", _experiencePoints);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void setExperiencePoints(int experiencePoints) {
        _experiencePoints = experiencePoints;
        setChanged();

        if(level != null && !level.isClientSide)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public int getExperiencePoints() {
        return _experiencePoints;
    }

    public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof ExperienceObeliskBlockEntity obeliskEntity) {
            //obeliskEntity._experiencePoints = level.random.nextIntBetweenInclusive(1000,9999);
        }
    }

    public boolean handleTransaction(int transactionAmount, ServerPlayer player) {
        int playerXP = ExperienceConverter.getTotalCurrentXPPoints(player.experienceLevel, player.experienceProgress);

        boolean xpAmountValid = transactionAmount != 0 && transactionAmount <= playerXP && transactionAmount >= -_experiencePoints;

        if (!xpAmountValid) return false;

        // Execute transaction
        setExperiencePoints(_experiencePoints + transactionAmount);
        int newPlayerXP = playerXP - transactionAmount;

        float levels = ExperienceConverter.getLevelsAtXPPoints(newPlayerXP);
        int wholeLevels = Mth.floor(levels);
        int sparePoints = newPlayerXP;
        if (wholeLevels > 0) {
            sparePoints -= ExperienceConverter.getTotalXPRequiredToLevel(wholeLevels);
        }

        player.setExperienceLevels(wholeLevels);
        player.setExperiencePoints(sparePoints);

        assert level != null;
        level.playSound(null, getBlockPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f, 1.0f);

        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);

        return true;
    }
}
