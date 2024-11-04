package com.ilcheese2.bubblelife.client.render;

import com.ilcheese2.bubblelife.bubbles.Bubble;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BubbleRenderer extends EntityRenderer<Bubble> {
    public BubbleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Bubble entity) {
        return null;
    }
}
