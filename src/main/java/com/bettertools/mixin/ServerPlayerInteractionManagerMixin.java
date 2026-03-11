package com.bettertools.mixin;

import com.bettertools.ToolHelper;
import com.bettertools.config.BetterToolsConfig;
import com.bettertools.config.ToolConfig;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    protected ServerPlayerEntity player;

    private static final ThreadLocal<Boolean> IS_MINING_3X3 = ThreadLocal.withInitial(() -> false);

    @Inject(method = "tryBreakBlock", at = @At("RETURN"))
    private void onTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || IS_MINING_3X3.get()) return;

        ItemStack stack = player.getMainHandStack();
        String toolType = ToolHelper.getToolType(stack);
        if (toolType == null) return;

        ToolConfig config = BetterToolsConfig.getConfig(toolType);
        if (!config.threeByThree) return;

        var world = player.getWorld();
        BlockState centerState = world.getBlockState(pos);

        Direction miningFace = getMiningFace(player);
        List<BlockPos> adjacentPositions = get3x3Positions(pos, miningFace);

        IS_MINING_3X3.set(true);
        try {
            ServerPlayerInteractionManager self = (ServerPlayerInteractionManager) (Object) this;
            for (BlockPos adjPos : adjacentPositions) {
                BlockState adjState = world.getBlockState(adjPos);
                if (shouldBreakBlock(adjState, centerState, stack, world)) {
                    self.tryBreakBlock(adjPos);
                }
            }
        } finally {
            IS_MINING_3X3.set(false);
        }
    }

    private Direction getMiningFace(ServerPlayerEntity player) {
        float pitch = player.getPitch();
        if (pitch > 45.0f) return Direction.DOWN;
        if (pitch < -45.0f) return Direction.UP;
        return player.getHorizontalFacing();
    }

    private List<BlockPos> get3x3Positions(BlockPos center, Direction face) {
        List<BlockPos> positions = new ArrayList<>();
        Direction.Axis axis = face.getAxis();

        Direction dir1;
        Direction dir2;
        if (axis == Direction.Axis.Y) {
            dir1 = Direction.NORTH;
            dir2 = Direction.EAST;
        } else if (axis == Direction.Axis.X) {
            dir1 = Direction.UP;
            dir2 = Direction.SOUTH;
        } else {
            dir1 = Direction.UP;
            dir2 = Direction.EAST;
        }

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                BlockPos pos = center.offset(dir1, i).offset(dir2, j);
                positions.add(pos);
            }
        }
        return positions;
    }

    private boolean shouldBreakBlock(BlockState adjState, BlockState centerState, ItemStack tool, net.minecraft.world.World world) {
        if (adjState.getHardness(world, BlockPos.ORIGIN) < 0) return false;
        if (adjState.isOf(centerState.getBlock())) return true;
        return tool.getMiningSpeedMultiplier(adjState) > 1.0f;
    }
}
