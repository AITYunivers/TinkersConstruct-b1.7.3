package io.github.yunivers.tconstruct.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor
{
    @Accessor("INSTANCE")
    static Minecraft getInstance()
    {
        throw new AssertionError();
    }
}
