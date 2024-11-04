package com.ilcheese2.bubblelife.mixin;

import com.ilcheese2.bubblelife.BubbleLifeAttachments;
import com.ilcheese2.bubblelife.client.BubbleControllerClient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow @Final private static Logger LOGGER;

    @ModifyArgs(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
    void modifyPartialTicks(Args args) {
        Entity entity = args.get(0);
        if (!BubbleControllerClient.instance().inBubble(entity)) {
            return;
        }
        args.set(4, entity.getData(BubbleLifeAttachments.TICK_RESIDUAL));
    }
}
