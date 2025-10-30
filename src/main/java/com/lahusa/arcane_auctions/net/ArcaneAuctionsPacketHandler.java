package com.lahusa.arcane_auctions.net;

import com.lahusa.arcane_auctions.ArcaneAuctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ArcaneAuctionsPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ArcaneAuctions.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;

        INSTANCE.registerMessage(id++,
                ExperienceObeliskTransactionC2SPacket.class,
                ExperienceObeliskTransactionC2SPacket::encode,
                ExperienceObeliskTransactionC2SPacket::new,
                ExperienceObeliskTransactionC2SPacket::handle);

        INSTANCE.registerMessage(id++,
                ExperienceObeliskPermissionUpdateC2SPacket.class,
                ExperienceObeliskPermissionUpdateC2SPacket::encode,
                ExperienceObeliskPermissionUpdateC2SPacket::new,
                ExperienceObeliskPermissionUpdateC2SPacket::handle);
    }
}
