package com.ilcheese2.bubblelife.gui;

import com.ilcheese2.bubblelife.datapacks.BubbleShader;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;

public class LabeledCycleButton<T> extends CycleButton<T> {

    private final Font font;
    private final int labelWidth;
    private final Component name;
    private final CycleButton.OnValueChange onValueChange;

    public LabeledCycleButton(int x, int y, int width, int height, Font font, Component message, List<T> values, Function<T, Component> stringifier, CycleButton.OnValueChange<T> onValueChange,
                              OptionInstance.TooltipSupplier<T> tooltipSupplier) {

        super(x, y, width, height, message, message, 0, values.getFirst(), ValueListSupplier.create(values), stringifier, CycleButton::createDefaultNarrationMessage, onValueChange, tooltipSupplier, true);
        labelWidth = font.width(message);
        this.font = font;
        name = message;
        this.onValueChange = onValueChange;
        setValue(values.getFirst());
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawString(font, this.name, this.getX(), this.getY() + this.height/4, 16777215);
        int oldX = this.getX();
        int oldWidth = this.getWidth();
        this.setX(oldX + labelWidth + 2);
        this.setWidth(oldWidth - labelWidth - 2 * 2);

        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        this.setX(oldX);
        this.setWidth(oldWidth);
    }

    @Override
    public void setValue(T value) {
        super.setValue(value);
        onValueChange.onValueChange(this, value);
    }
}