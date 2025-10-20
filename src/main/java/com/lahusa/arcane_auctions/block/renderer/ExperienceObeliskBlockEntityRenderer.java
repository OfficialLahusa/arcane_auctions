package com.lahusa.arcane_auctions.block.renderer;

import com.lahusa.arcane_auctions.ArcaneAuctions;
import com.lahusa.arcane_auctions.block.entity.ExperienceObeliskBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ExperienceObeliskBlockEntityRenderer extends GeoBlockRenderer<ExperienceObeliskBlockEntity> {

    public ExperienceObeliskBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new DefaultedBlockGeoModel<>(ResourceLocation.fromNamespaceAndPath(ArcaneAuctions.MODID, "experience_obelisk")));
    }
}
