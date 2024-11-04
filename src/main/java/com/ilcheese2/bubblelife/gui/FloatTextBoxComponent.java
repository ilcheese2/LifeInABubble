package com.ilcheese2.bubblelife.gui;

import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Sizing;

public class FloatTextBoxComponent extends TextBoxComponent {
    protected FloatTextBoxComponent(Sizing horizontalSizing) {
        super(horizontalSizing);
    }

    @Override
    public void insertText(String textToWrite) {
        super.insertText(textToWrite.replaceAll("[^.0-9]", ""));
    }
}
