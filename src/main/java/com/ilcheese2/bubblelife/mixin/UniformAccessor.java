package com.ilcheese2.bubblelife.mixin;

import com.mojang.blaze3d.shaders.Uniform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@Mixin(Uniform.class)
public interface UniformAccessor {

    @Accessor
    FloatBuffer getFloatValues();

    @Accessor
    IntBuffer getIntValues();

    @Invoker
    void invokeMarkDirty();

    @Accessor
    @Mutable
    void setType(int type);
}
