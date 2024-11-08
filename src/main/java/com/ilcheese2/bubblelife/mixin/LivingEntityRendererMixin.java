package com.ilcheese2.bubblelife.mixin;

import com.ilcheese2.bubblelife.BubbleLifeAttachments;
import com.ilcheese2.bubblelife.client.BubbleLifeClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "getRenderType", at = @At("RETURN"), cancellable = true)
    void applyGlitch(LivingEntity livingEntity, boolean bodyVisible, boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        if (((Object) this instanceof PlayerRenderer renderer) && livingEntity.getData(BubbleLifeAttachments.REWIND)) {
            cir.setReturnValue(BubbleLifeClient.PLAYER_GLITCH.apply(renderer.getTextureLocation((AbstractClientPlayer) livingEntity)));
            BubbleLifeClient.playerGlitch.getUniform("GameTime2").set(Minecraft.getInstance().level.getGameTime());
        }
    }
}