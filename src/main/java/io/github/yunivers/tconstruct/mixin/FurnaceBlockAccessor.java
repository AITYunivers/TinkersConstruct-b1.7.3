package io.github.yunivers.tconstruct.mixin;

import net.minecraft.block.FurnaceBlock;
import net.minecraft.client.MinecraftApplet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceBlock.class)
public interface FurnaceBlockAccessor {
	@Accessor("ignoreBlockRemoval")
	static void setIgnoreBlockRemoval(boolean ignoreBlockRemoval) {
		throw new AssertionError();
	}
}
