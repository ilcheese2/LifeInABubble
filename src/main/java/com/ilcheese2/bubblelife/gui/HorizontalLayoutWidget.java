package com.ilcheese2.bubblelife.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HorizontalLayoutWidget extends AbstractWidget {

    private final List<AbstractWidget> children = new ArrayList<>();
    private final Font font;
    private final int labelWidth;

    public HorizontalLayoutWidget(int x, int y, int width, int height, Font font, Component message) {
        super(x, y, width, height, message);
        this.font = font;
        this.labelWidth = font.width(message);
    }

    public void addChild(AbstractWidget child) {
        children.add(child);
        recalculatePositions();
    }

    @Override
    public void visitWidgets(@NotNull Consumer<AbstractWidget> consumer) {
        children.forEach(consumer);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        recalculatePositions();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        recalculatePositions();
    }

    private void recalculatePositions() {
        int x = this.getX() + labelWidth;
        for (AbstractWidget child : children) {
            child.setX(x);
            child.setWidth((getWidth() - labelWidth) / children.size());
            x += child.getWidth() - 1;
            child.setY(this.getY());
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawString(font, getMessage(), getX(), getY() + this.height / 4, 16777215);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
