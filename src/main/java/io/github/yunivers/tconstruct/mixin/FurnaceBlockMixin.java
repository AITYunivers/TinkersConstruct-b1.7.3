package io.github.yunivers.tconstruct.mixin;

import io.github.yunivers.tconstruct.blocks.FurnaceSlab;
import io.github.yunivers.tconstruct.events.init.InitListener;
import net.minecraft.block.Block;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceBlock.class)
public abstract class FurnaceBlockMixin {
    @Inject(
        method = "updateLitState",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void updateLitState(boolean lit, World world, int x, int y, int z, CallbackInfo ci) {
        int blockId = world.getBlockId(x, y, z);
        if (blockId == InitListener.furnaceSlab.id)
        {
            FurnaceSlab.updateLitState(lit, world, x, y, z);
            ci.cancel();
        }
    }
}
