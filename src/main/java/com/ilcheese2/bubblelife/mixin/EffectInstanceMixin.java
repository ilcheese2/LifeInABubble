package com.ilcheese2.bubblelife.mixin;

import com.ilcheese2.bubblelife.client.BubbleControllerClient;
import com.ilcheese2.bubblelife.datapacks.CustomBubbleShaders;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.EffectInstance;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectInstance.class)
public class EffectInstanceMixin {

    @Shadow @Final private String name;

    @Inject(method = "attachToProgram", at = @At(value = "HEAD"))
    void linkSphereTexture(CallbackInfo ci) {
        if (name.contains("bubble")) {
           CustomBubbleShaders.SHADERS.forEach((shader) -> {
               shader.compiledShader.attachToShader((EffectInstance) (Object) this);
           });
        }
    }

    @WrapOperation(method = "apply", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;bindTexture(I)V"))
    void changeToTexture1D(int texture, Operation<Void> original) {
        if (texture != 0 && texture == BubbleControllerClient.instance().dataTexture) {
            GL11.glBindTexture(GL11.GL_TEXTURE_1D, BubbleControllerClient.instance().dataTexture);
        } else {
            original.call(texture);
        }
    }
}
