package com.ilcheese2.bubblelife.mixin;

import com.ilcheese2.bubblelife.client.BubbleLifeClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "resize", at = @At("HEAD"))
    void resizeBubbleChain(int width, int height, CallbackInfo ci) {
        if (BubbleLifeClient.bubbleShader != null) BubbleLifeClient.bubbleShader.resize(width, height);
    }
}
