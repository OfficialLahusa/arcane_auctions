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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class ExperienceObeliskBlockEntity extends BlockEntity implements GeoBlockEntity {
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache _cache = GeckoLibUtil.createInstanceCache(this);
    private long _experiencePoints;
    private UUID _owner;

    public enum TransactionPermissions {
        Everyone,
        Whitelist,
        Owner
    }

    private TransactionPermissions _withdrawPermissions;
    private TransactionPermissions _depositPermissions;
    private TransactionPermissions _logPermissions;

    public ExperienceObeliskBlockEntity(BlockPos pos, BlockState state) {
        super(ArcaneAuctions.EXPERIENCE_OBELISK_BLOCK_ENTITY.get(), pos, state);

        _experiencePoints = 0;
        _owner = null;
        _withdrawPermissions = TransactionPermissions.Everyone;
        _depositPermissions = TransactionPermissions.Everyone;
        _logPermissions = TransactionPermissions.Everyone;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        _experiencePoints = tag.getLong("experience_points");

        if (tag.contains("owner"))
            _owner = tag.getUUID("owner");
        else
            _owner = null;

        if (tag.contains("withdraw_permissions"))
            _withdrawPermissions = TransactionPermissions.valueOf(tag.getString("withdraw_permissions"));
        else
            _withdrawPermissions = TransactionPermissions.Everyone;

        if (tag.contains("deposit_permissions"))
            _depositPermissions = TransactionPermissions.valueOf(tag.getString("deposit_permissions"));
        else
            _depositPermissions = TransactionPermissions.Everyone;

        if (tag.contains("log_permissions"))
            _logPermissions = TransactionPermissions.valueOf(tag.getString("log_permissions"));
        else
            _logPermissions = TransactionPermissions.Everyone;
    }

    protected void save(CompoundTag tag) {
        tag.putLong("experience_points", _experiencePoints);

        if (_owner != null) {
            tag.putUUID("owner", _owner);
        }

        tag.putString("withdraw_permissions", _withdrawPermissions.name());
        tag.putString("deposit_permissions", _depositPermissions.name());
        tag.putString("log_permissions", _logPermissions.name());
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        save(tag);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();

        save(tag);

        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void setExperiencePoints(long experiencePoints) {
        _experiencePoints = experiencePoints;
        setChanged();

        if(level != null && !level.isClientSide)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public long getExperiencePoints() {
        return _experiencePoints;
    }

    public void setOwner(UUID owner) {
        _owner = owner;
        setChanged();

        if(level != null && !level.isClientSide)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public UUID getOwner() {
        return _owner;
    }

    public TransactionPermissions getWithdrawPermissions() {
        return _withdrawPermissions;
    }

    public TransactionPermissions getDepositPermissions() {
        return _depositPermissions;
    }

    public TransactionPermissions getLogPermissions() {
        return _logPermissions;
    }

    public void setWithdrawPermissions(TransactionPermissions value) {
        _withdrawPermissions = value;

        setChanged();

        if(level != null && !level.isClientSide)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void setDepositPermissions(TransactionPermissions value) {
        _depositPermissions = value;

        setChanged();

        if(level != null && !level.isClientSide)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void setLogPermissions(TransactionPermissions value) {
        _logPermissions = value;

        setChanged();

        if(level != null && !level.isClientSide)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof ExperienceObeliskBlockEntity obeliskEntity) {
            //obeliskEntity._experiencePoints = level.random.nextIntBetweenInclusive(1000,9999);
        }
    }

    public boolean handleTransaction(long transactionAmount, ServerPlayer player) {
        long playerXP = ExperienceConverter.getTotalCurrentXPPoints(player.experienceLevel, player.experienceProgress);

        boolean xpAmountValid = transactionAmount != 0 && transactionAmount <= playerXP && transactionAmount >= -_experiencePoints;

        if (!xpAmountValid) return false;

        // Execute transaction
        setExperiencePoints(_experiencePoints + transactionAmount);
        long newPlayerXP = playerXP - transactionAmount;

        float levels = ExperienceConverter.getLevelsAtXPPoints(newPlayerXP);
        int wholeLevels = Mth.floor(levels);
        long sparePoints = newPlayerXP;
        if (wholeLevels > 0) {
            sparePoints -= ExperienceConverter.getTotalXPRequiredToLevel(wholeLevels);
        }

        player.setExperienceLevels(wholeLevels);
        player.setExperiencePoints((int) sparePoints);

        assert level != null;
        level.playSound(null, getBlockPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f, 1.0f);

        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);

        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, this::idleAnimController));
    }

    protected <E extends ExperienceObeliskBlockEntity> PlayState idleAnimController(final AnimationState<E> state) {
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this._cache;
    }
}
