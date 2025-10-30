package com.lahusa.arcane_auctions.block.entity;

import com.lahusa.arcane_auctions.ArcaneAuctions;
import com.lahusa.arcane_auctions.data.TransactionLogEntry;
import com.lahusa.arcane_auctions.util.ExperienceConverter;
import com.lahusa.arcane_auctions.util.NumberFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ExperienceObeliskBlockEntity extends BlockEntity implements GeoBlockEntity {
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache _cache = GeckoLibUtil.createInstanceCache(this);
    private long _experiencePoints;
    private UUID _owner;
    private List<String> _whitelist;
    private List<TransactionLogEntry> _transactionLog;
    public static final int TRANSACTION_LOG_LENGTH = 8;

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
        _whitelist = new ArrayList<>();
        _transactionLog = new ArrayList<>();
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

        if (tag.contains("whitelist")) {
            _whitelist.clear();
            ListTag whitelistTag = (ListTag) tag.get("whitelist");

            assert whitelistTag != null;

            for (Tag entryTag : whitelistTag) {
                CompoundTag compoundEntryTag = (CompoundTag) entryTag;

                String username = compoundEntryTag.getString("username");

                _whitelist.add(username);
            }
        }

        if (tag.contains("transaction_log")) {
            _transactionLog.clear();
            ListTag transactionLogTag = (ListTag) tag.get("transaction_log");

            assert transactionLogTag != null;

            for (Tag entryTag : transactionLogTag) {
                CompoundTag compoundEntryTag = (CompoundTag) entryTag;

                String username = compoundEntryTag.getString("username");
                long amount = compoundEntryTag.getLong("amount");

                _transactionLog.add(new TransactionLogEntry(username, amount));
            }
        }

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

        if (_whitelist != null) {
            ListTag whitelistTag = new ListTag();

            for (String entry : _whitelist) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString("username", entry);

                whitelistTag.add(entryTag);
            }

            tag.put("whitelist", whitelistTag);
        }

        if (_transactionLog != null) {
            ListTag transactionLogTag = new ListTag();

            for (TransactionLogEntry entry : _transactionLog) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString("username", entry.username);
                entryTag.putLong("amount", entry.amount);

                transactionLogTag.add(entryTag);
            }

            tag.put("transaction_log", transactionLogTag);
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

    public List<TransactionLogEntry> getTransactionLog() {
        return Collections.unmodifiableList(_transactionLog);
    }

    public List<String> getWhitelist() {
        return Collections.unmodifiableList(_whitelist);
    }

    private void addTransactionLogEntry(TransactionLogEntry entry) {
        _transactionLog.add(entry);

        // Limit log length
        while(_transactionLog.size() > TRANSACTION_LOG_LENGTH) {
            _transactionLog.remove(0);
        }
    }

    public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof ExperienceObeliskBlockEntity obeliskEntity) {
            //obeliskEntity._experiencePoints = level.random.nextIntBetweenInclusive(1000,9999);
        }
    }

    public boolean handleTransaction(long transactionAmount, ServerPlayer player) {
        // Enforce permissions
        boolean hasPermission = (transactionAmount < 0) ? mayWithdraw(player) : mayDeposit(player);
        if (!hasPermission) {
            player.sendSystemMessage(Component.literal("You do not have sufficient permission to perform this transaction.").withStyle(ChatFormatting.RED));
            return false;
        }

        long playerXP = ExperienceConverter.getTotalCurrentXPPoints(player.experienceLevel, player.experienceProgress);

        boolean xpAmountValid = transactionAmount != 0 && transactionAmount <= playerXP && transactionAmount >= -_experiencePoints;

        if (!xpAmountValid) return false;

        long prevXpPoints = _experiencePoints;

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

        // Write log entry
        addTransactionLogEntry(new TransactionLogEntry(player.getName().getString(), transactionAmount));

        ArcaneAuctions.LOGGER.info(
                player.getName().getString()
                        + ((transactionAmount < 0) ? " withdrew " : " deposited ")
                        + NumberFormatter.longToString(Math.abs(transactionAmount))
                        + " points of experience"
                        + ((transactionAmount < 0) ? " from" : " into")
                        + " experience obelisk at "
                        + getBlockPos().toShortString()
                        + " (Obelisk: "
                        + NumberFormatter.longToString(prevXpPoints)
                        + " -> "
                        + NumberFormatter.longToString(_experiencePoints)
                        + ", Player: "
                        + NumberFormatter.longToString(playerXP)
                        + " -> "
                        + NumberFormatter.longToString(newPlayerXP)
                        + ").");

        return true;
    }

    public boolean handlePermissionUpdate(TransactionPermissions withdrawPermissions, TransactionPermissions depositPermissions, TransactionPermissions logPermissions, ServerPlayer player) {
        // Check for owner permissions
        if (!_owner.equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("Only the owner may change the permissions.").withStyle(ChatFormatting.RED));
            return false;
        }

        // Change permissions
        _withdrawPermissions = withdrawPermissions;
        _depositPermissions = depositPermissions;
        _logPermissions = logPermissions;

        assert level != null;
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);

        ArcaneAuctions.LOGGER.info(
                player.getName().getString()
                        + " updated permissions of experience obelisk at "
                        + getBlockPos().toShortString()
                        + "to Deposit: " + depositPermissions.name()
                        + ", Withdraw: " + withdrawPermissions.name()
                        + ", Log: " + logPermissions.name()
                        + ".");

        return true;
    }

    public boolean handleWhitelistAdd(String username, ServerPlayer player) {
        // Check for owner permissions
        if (!_owner.equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("Only the owner may modify the whitelist.").withStyle(ChatFormatting.RED));
            return false;
        }

        if (!_whitelist.contains(username))
            _whitelist.add(username);

        assert level != null;
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);

        ArcaneAuctions.LOGGER.info(
                player.getName().getString()
                        + " added username " + username
                        + " to whitelist of experience obelisk at "
                        + getBlockPos().toShortString()
                        + ".");

        return true;
    }

    public boolean handleWhitelistRemove(String username, ServerPlayer player) {
        // Check for owner permissions
        if (!_owner.equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("Only the owner may modify the whitelist.").withStyle(ChatFormatting.RED));
            return false;
        }

        if (_whitelist.contains(username))
            _whitelist.removeAll(Collections.singletonList(username));

        assert level != null;
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);

        ArcaneAuctions.LOGGER.info(
                player.getName().getString()
                        + " removed username " + username
                        + " from whitelist of experience obelisk at "
                        + getBlockPos().toShortString()
                        + ".");

        return true;
    }

    public boolean handleWhitelistClear(ServerPlayer player) {
        // Check for owner permissions
        if (!_owner.equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("Only the owner may modify the whitelist.").withStyle(ChatFormatting.RED));
            return false;
        }

        _whitelist.clear();

        assert level != null;
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);

        ArcaneAuctions.LOGGER.info(
                player.getName().getString()
                        + " cleared whitelist of experience obelisk at "
                        + getBlockPos().toShortString()
                        + ".");

        return true;
    }

    protected boolean hasPermission(Player player, TransactionPermissions permission) {
        if (_owner.equals(player.getUUID()))
            // Player is owner
            return true;

        if(permission == TransactionPermissions.Everyone) {
            // Everyone is allowed
            return true;
        }
        else if(permission == TransactionPermissions.Whitelist) {
            for (String whitelistEntry : _whitelist) {
                if (whitelistEntry.equals(player.getName().getString())) {
                    // Player is whitelisted
                    return true;
                }
            }
        }

        return false;
    }

    public boolean mayWithdraw(Player player) {
        return hasPermission(player, _withdrawPermissions);
    }

    public boolean mayDeposit(Player player) {
        return hasPermission(player, _depositPermissions);
    }

    public boolean mayViewLog(Player player) {
        return hasPermission(player, _logPermissions);
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
