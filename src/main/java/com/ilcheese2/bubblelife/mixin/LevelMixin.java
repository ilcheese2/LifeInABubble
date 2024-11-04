package com.ilcheese2.bubblelife.mixin;

import com.ilcheese2.bubblelife.bubbles.BubbleController;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(Level.class)
public class LevelMixin {

    @Shadow @Final public boolean isClientSide;

    @WrapMethod(method = "guardEntityTick")
    void cancelTick(Consumer<Entity> consumerEntity, Entity entity, Operation<Void> original) {
        if (BubbleController.inBubble(entity, entity.level().isClientSide)) {
            return;
        }
        original.call(consumerEntity, entity);
    }

    @WrapOperation(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;shouldTickBlocksAt(Lnet/minecraft/core/BlockPos;)Z"))
    boolean cancelBlockEntityTick(Level instance, BlockPos pos, Operation<Boolean> original) {
        return (BubbleController.inBubblePosition(Vec3.atLowerCornerOf(pos), isClientSide) == null);
    }
}
