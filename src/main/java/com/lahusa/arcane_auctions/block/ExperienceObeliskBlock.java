package com.lahusa.arcane_auctions.block;

import com.lahusa.arcane_auctions.ArcaneAuctions;
import com.lahusa.arcane_auctions.block.entity.ExperienceObeliskBlockEntity;
import com.lahusa.arcane_auctions.gui.menu.ExperienceObeliskMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class ExperienceObeliskBlock extends Block implements EntityBlock {

    private static VoxelShape SHAPE = Block.box(4, 0, 4, 12, 14, 12);

    public ExperienceObeliskBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExperienceObeliskBlockEntity(pos, state);
    }

    @Override
    public @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, playerInventory, player) -> new ExperienceObeliskMenu (containerId, playerInventory, pos, ContainerLevelAccess.create(level, pos)),
                Component.literal("Experience Obelisk")
        );
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, state.getMenuProvider(level, pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        super.setPlacedBy(level, pos, state, livingEntity, itemStack);

        // Set block entity owner, if possible
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof ExperienceObeliskBlockEntity obeliskEntity && livingEntity instanceof Player player) {
            obeliskEntity.setOwner(player.getUUID());
        }
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource randomSource) {
        super.animateTick(state, level, pos, randomSource);

        Vec3 dir = new Vec3(2 * randomSource.nextFloat() - 1, 2 * randomSource.nextFloat() - 1, 2 * randomSource.nextFloat() - 1);
        dir = dir.normalize();

        if (randomSource.nextInt(8) == 0) {
            level.addParticle(
                    ParticleTypes.END_ROD,
                    (double) pos.getX() + 0.5 + 0.35 * dir.x,
                    (double) pos.getY() + 0.65 + 0.35 * dir.y,
                    (double) pos.getZ() + 0.5 + 0.35 * dir.z,
                    0,
                    0,
                    0
            );
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ArcaneAuctions.EXPERIENCE_OBELISK_BLOCK_ENTITY.get() ? ExperienceObeliskBlockEntity::tick : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext collisionContext) {
        return SHAPE;
    }

    // Used for registration properties
    public static int getLightLevelForState(BlockState state) {
        return 10;
    }
}
