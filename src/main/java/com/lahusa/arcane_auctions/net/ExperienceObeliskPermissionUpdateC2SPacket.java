package com.lahusa.arcane_auctions.net;

import com.lahusa.arcane_auctions.block.entity.ExperienceObeliskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ExperienceObeliskPermissionUpdateC2SPacket {
    public BlockPos pos;
    public ExperienceObeliskBlockEntity.TransactionPermissions withdrawPermissions;
    public ExperienceObeliskBlockEntity.TransactionPermissions depositPermissions;
    public ExperienceObeliskBlockEntity.TransactionPermissions logPermissions;

    public ExperienceObeliskPermissionUpdateC2SPacket(BlockPos pos,
                                                      ExperienceObeliskBlockEntity.TransactionPermissions withdrawPermissions,
                                                      ExperienceObeliskBlockEntity.TransactionPermissions depositPermissions,
                                                      ExperienceObeliskBlockEntity.TransactionPermissions logPermissions) {
        this.pos = pos;
        this.withdrawPermissions = withdrawPermissions;
        this.depositPermissions = depositPermissions;
        this.logPermissions = logPermissions;
    }

    public ExperienceObeliskPermissionUpdateC2SPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        withdrawPermissions = buf.readEnum(ExperienceObeliskBlockEntity.TransactionPermissions.class);
        depositPermissions = buf.readEnum(ExperienceObeliskBlockEntity.TransactionPermissions.class);
        logPermissions = buf.readEnum(ExperienceObeliskBlockEntity.TransactionPermissions.class);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeEnum(this.withdrawPermissions);
        buf.writeEnum(this.depositPermissions);
        buf.writeEnum(this.logPermissions);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {

        final var success = new AtomicBoolean(false);

        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            assert sender != null;

            // Check if chunk at pos is loaded
            if (!sender.level().hasChunkAt(pos)) {
                success.set(false);
                return;
            }

            BlockEntity serverEntity = sender.level().getBlockEntity(pos);

            if(serverEntity instanceof ExperienceObeliskBlockEntity obeliskEntity){
                boolean transactionSuccess = obeliskEntity.handlePermissionUpdate(withdrawPermissions, depositPermissions, logPermissions, sender);
                success.set(transactionSuccess);
            }
            else {
                success.set(false);
            }
        });

        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
