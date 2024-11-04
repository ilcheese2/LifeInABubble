package com.ilcheese2.bubblelife.client;

import com.ilcheese2.bubblelife.DetachedTimes;
import com.ilcheese2.bubblelife.bubbles.BubbleController;
import com.ilcheese2.bubblelife.datapacks.CustomBubbleShaders;
import com.ilcheese2.bubblelife.bubbles.Bubble;
import com.ilcheese2.bubblelife.mixin.PostChainAccessor;
import com.ilcheese2.bubblelife.mixin.UniformAccessor;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class BubbleControllerClient extends BubbleController {
    private static BubbleControllerClient instance;
    public int dataTexture;
    private FloatBuffer bubbleData;


    public BubbleControllerClient() {
        super();
        instance = this;
        this.bubbles.clear();
    }

    public static BubbleControllerClient instance() {
        return instance;
    }

    @Override
    protected void runOnMainThread(Runnable runnable) {
        Minecraft.getInstance().execute(runnable);
    }

    @Override
    protected boolean isPaused() {
        return Minecraft.getInstance().isPaused();
    }

    @Override
    public void addBubble(Bubble bubble) {
        super.addBubble(bubble);
        updateShader();
    }

    @Override
    public void removeBubble(Bubble bubble) {
        int length = bubbles.size();
        super.removeBubble(bubble);
        if (length != bubbles.size()) {
            updateShader();
        }
    }

    public void updateShader() {
        if (DetachedTimesClient.bubbleShader == null || dataTexture == 0) return;
        EffectInstance effect = ((PostChainAccessor) DetachedTimesClient.bubbleShader).getPasses().getFirst().getEffect();

        var offsetsUniform = effect.safeGetUniform("BubbleOffsets");
        IntBuffer intBuffer = ((UniformAccessor) offsetsUniform).getIntValues();
        intBuffer.clear();
        bubbleData.clear();
        int i = 0;
        for (Bubble bubble: this.bubbles) {
            intBuffer.put(i/4);
            var shader = CustomBubbleShaders.getShader(bubble.info.shader());
            int finalI = i + 8;

            bubbleData.put((float) bubble.getX());
            bubbleData.put((float) bubble.getY());
            bubbleData.put((float) bubble.getZ());
            bubbleData.put((float) bubble.info.range());

            bubbleData.put((float) CustomBubbleShaders.getShaderIndex(bubble.info.shader())); // waste twelve bytes
            bubbleData.put(0);
            bubbleData.put(0);
            bubbleData.put((float) 0xDEADC0DE);

            shader.uniforms.forEach((name, value) -> {
                bubbleData.position(finalI + value.offset + value.innerOffset);
                if (bubbleData.remaining() < 4) {
                    //DetachedTimes.LOGGER.warn("Too many bubbles to render, consider increasing the buffer size");
                    return; // ok
                }
                if (bubble.info.uniforms().containsKey(name)) {
                    bubbleData.put(ArrayUtils.toPrimitive(bubble.info.uniforms().get(name).toArray(Float[]::new)));
                } else {
                    bubbleData.put(ArrayUtils.toPrimitive(value.defaultValues));
                }
            });


            bubbleData.position((bubbleData.position() + 3) / 4 * 4);
            i = bubbleData.position();
        }


        float[] data = new float[bubbleData.position()];
        var c = bubbleData.duplicate();
        c.rewind();

        c.get(data);

        //bubbleData.rewind();
        DetachedTimes.LOGGER.info("Bubble data: " + Arrays.toString(data) + " something: " + bubbleData.position());
//        bubbleData.clear();
//        bubbleData.put(0);
//        bubbleData.put(1);
//        bubbleData.put(0);
//        bubbleData.put(1);
        bubbleData.rewind();
        GL11.glBindTexture(GL11.GL_TEXTURE_1D, dataTexture);
        GL11.glTexImage1D(GL11.GL_TEXTURE_1D, 0, GL30.GL_RGBA32F, i/4, 0, GL11.GL_RGBA, GL11.GL_FLOAT, bubbleData);
        GL11.glBindTexture(GL11.GL_TEXTURE_1D, 0);


        ((UniformAccessor) offsetsUniform).invokeMarkDirty();
        effect.safeGetUniform("BubbleCount").set(this.bubbles.size());
    }

    public void createBuffers() {
        dataTexture = GlStateManager._genTexture();
        GL11.glBindTexture(GL11.GL_TEXTURE_1D, dataTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_1D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glBindTexture(GL11.GL_TEXTURE_1D, 0);
        bubbleData = MemoryUtil.memAllocFloat(1024 * 4);
    }
}
