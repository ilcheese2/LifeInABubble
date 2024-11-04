package com.ilcheese2.bubblelife.gui;

import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Sizing;

public class NumberTextBoxComponent extends TextBoxComponent {

    private final int min;
    private final int max;

    protected NumberTextBoxComponent(Sizing horizontalSizing, int min, int max) {
        super(horizontalSizing);
        this.min = min;
        this.max = max;
    }

    @Override
    public void insertText(String textToWrite) {
        super.insertText(textToWrite.replaceAll("[^0-9]", ""));
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            String text = getValue();
            if (text.isEmpty()) {
                return;
            }
            int number = Integer.parseInt(text);
            if (number < min) {
                setValue(String.valueOf(min));
            } else if (number > max) {
                setValue(String.valueOf(max));
            }
        }
    }
}
