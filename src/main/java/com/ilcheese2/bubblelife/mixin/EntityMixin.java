package com.ilcheese2.bubblelife.mixin;

import com.ilcheese2.bubblelife.DetachedTimesAttachments;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin {


    @ModifyReturnValue(method = "getGravity", at = @At("RETURN"))
    double getGravity(double original) {
        if (((Entity) (Object) this).getData(DetachedTimesAttachments.REWIND)) {
            return 0.0;
        }
        return original;
    }
}
