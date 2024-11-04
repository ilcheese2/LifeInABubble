package com.ilcheese2.bubblelife.mixin;

import com.ilcheese2.bubblelife.DetachedTimesAttachments;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z", ordinal = 0))
    boolean keepPhysicsChange(boolean original) {
        if (((Player) (Object) this).getData(DetachedTimesAttachments.REWIND)) {
            return true;
        }
        return original;
    }
}
