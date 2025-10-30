package com.lahusa.arcane_auctions.net;

import com.lahusa.arcane_auctions.block.entity.ExperienceObeliskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ExperienceObeliskWhitelistClearC2SPacket {
    public BlockPos pos;

    public ExperienceObeliskWhitelistClearC2SPacket(BlockPos pos) {
        this.pos = pos;
    }

    public ExperienceObeliskWhitelistClearC2SPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
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
                boolean transactionSuccess = obeliskEntity.handleWhitelistClear(sender);
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
