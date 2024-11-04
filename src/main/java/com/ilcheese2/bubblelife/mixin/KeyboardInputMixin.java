package com.ilcheese2.bubblelife.mixin;

import com.ilcheese2.bubblelife.BubbleLifeAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    void overrideInput(boolean isSneaking, float sneakingSpeedMultiplier, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getData(BubbleLifeAttachments.REWIND)) {
            var input = (Input) (Object) this;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
            input.forwardImpulse = 0.0F;
            input.leftImpulse = 0.0F;
            input.jumping = false;
            input.shiftKeyDown = false;
        }
    }
}
