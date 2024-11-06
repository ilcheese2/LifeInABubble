package com.ilcheese2.bubblelife.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class LabeledEditBox extends EditBox {

    private final Font font;
    private final int labelWidth;

    public LabeledEditBox(Font font, int x, int y, int width, int height, Component message, Predicate<String> filter, Predicate<String> validator, Consumer<String> responder) {
        super(font, x, y, width, height, message);
        this.font = font;
        labelWidth = font.width(message);
        this.setFilter(filter);
        this.setResponder((s) -> {
            if (validator.test(s)) {
                responder.accept(s);
            }
        });
    }

    @Override
    public void setMessage(Component message) {}

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawString(font, getMessage(), this.getX(), this.getY() + this.height/4, 16777215);
        int oldX = this.getX();
        int oldWidth = this.getWidth();
        this.setX(oldX + labelWidth + 2);
        this.setWidth(oldWidth - labelWidth - 2 * (getMessage().getString().isEmpty() ? 1 : 2));

        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        this.setX(oldX);
        this.setWidth(oldWidth);
    }
}
