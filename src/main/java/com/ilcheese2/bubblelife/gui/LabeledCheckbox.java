package com.ilcheese2.bubblelife.gui;

import com.ilcheese2.bubblelife.BubbleLife;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;



import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class LabeledCheckbox extends AbstractButton {
    private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = ResourceLocation.fromNamespaceAndPath(BubbleLife.MODID, "checkbox_selected_highlighted");
    private static final ResourceLocation CHECKBOX_SELECTED_SPRITE = ResourceLocation.fromNamespaceAndPath(BubbleLife.MODID, "checkbox_selected");
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE = ResourceLocation.fromNamespaceAndPath(BubbleLife.MODID, "checkbox_highlighted");
    private static final ResourceLocation CHECKBOX_SPRITE = ResourceLocation.fromNamespaceAndPath(BubbleLife.MODID, "checkbox");
    private boolean selected;
    private final Consumer<LabeledCheckbox> listener;
    private final Font font;
    private final int labelWidth;

    public LabeledCheckbox(int x, int y, Component message, Font font, boolean selected, Consumer<LabeledCheckbox> listener) {
        super(x, y, 0, 0, message);
        this.height = 10;
        this.listener = listener;
        this.selected = selected;
        this.font = font;
        this.labelWidth = font.width(message);
        this.width = labelWidth + 10 + 2;
    }

    @Override
    public void onPress() {
        this.selected = !this.selected;
        this.listener.accept(this);
    }

    public boolean selected() {
        return this.selected;
    }

    public void selected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableDepthTest();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        ResourceLocation resourcelocation;
        if (this.selected) {
            resourcelocation = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            resourcelocation = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }

        guiGraphics.drawString(font, this.getMessage(), this.getX(), this.getY(), 16777215);
        guiGraphics.blitSprite(resourcelocation, this.getX() + this.labelWidth + 2, this.getY(), 10, 10);
    }
}
