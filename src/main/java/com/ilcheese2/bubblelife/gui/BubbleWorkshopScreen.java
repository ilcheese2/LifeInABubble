package com.ilcheese2.bubblelife.gui;

import com.ilcheese2.bubblelife.BubbleLife;
import com.ilcheese2.bubblelife.bubbles.BubbleInfo;
import com.ilcheese2.bubblelife.datapacks.CustomBubbleShaders;
import com.ilcheese2.bubblelife.networking.UpdateBubblePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class BubbleWorkshopScreen extends AbstractContainerScreen<BubbleWorkshopMenu> {

    private final ResourceLocation background = ResourceLocation.fromNamespaceAndPath(BubbleLife.MODID, "textures/gui/workshop.png");
    private final List<AbstractWidget> widgets = new ArrayList<>();
    private final List<Renderable> renderables2 = new ArrayList<>();
    private final List<AbstractWidget> removableUniformWidgets = new ArrayList<>();
    private final List<Consumer<BubbleInfo>> listeners = new ArrayList<>();

    private final int INNER_TOP = 52;
    private final int INNER_LEFT = 172;
    private final int INNER_HEIGHT = 53;
    private final int INNER_WIDTH = 120;
    private float scrollOffs;
    private boolean scrolling;
    private float contentHeight;

    public BubbleWorkshopScreen(BubbleWorkshopMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.leftPos = 125;
        this.topPos = 37;
        menu.addListener((stack) -> {
            BubbleInfo info = stack.get(BubbleLife.BUBBLE_INFO);
            listeners.forEach(listener -> listener.accept(info));
        });
    }

    @Override
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        renderables2.add(widget);
        return addWidget(widget);
    }

    @Override
    protected void removeWidget(GuiEventListener listener) {
        renderables2.remove(listener);
        super.removeWidget(listener);
    }

    @Override
    protected void init() {
        super.init();
        widgets.clear();
        listeners.clear();
        renderables2.clear();
        scrollOffs = 0;
        addWidget(this.createInput(Component.translatable("menu.bubblelife.workshop.speed"), 25, 500, true,
                (value, builder) -> builder.speed(value)),
                (widget, info) -> {
                    if (info == null) {
                        widget.setValue("");
                        return;
                    }
                    widget.setValue(String.valueOf(info.speed()));
                });
        addWidget(this.createInput(Component.translatable("menu.bubblelife.workshop.range"), 5, 20, true,
                (value, builder) -> builder.range(value)),
                (widget, info) -> {
                    if (info == null) {
                        widget.setValue("");
                        return;
                    }
                    widget.setValue(String.valueOf(info.range()));
                });
        addWidget(new LabeledCheckbox(INNER_LEFT, INNER_TOP, Component.translatable("menu.bubblelife.workshop.changes_time"), font, true,
                onInputChange((value, builder) -> builder.changesTime(value.selected()))),
                (widget, info) -> {
                    if (info == null) {
                        widget.selected(false);
                        return;
                    }
                    widget.selected(info.changesTime());
                });
        final String[] lastShader = {""};
        widgets.add(null); // something about race conditions
        setWidget(widgets.size() - 1, new LabeledCycleButton<>(INNER_LEFT, INNER_TOP, INNER_WIDTH - 1, 15, font, Component.translatable("menu.bubblelife.workshop.shaders"), CustomBubbleShaders.SHADERS,
                (shader) -> Component.translatable(shader.nameKey),
                (button, shader) -> {
                        lastShader[0] = shader.name;
                        removableUniformWidgets.forEach(widgets::remove);
                        removableUniformWidgets.forEach(this::removeWidget);
                        removableUniformWidgets.clear();
                        onInputChange((value, builder) -> builder.shader(shader.name)).accept(shader);
                        for (var uniform : shader.uniforms.entrySet()) {
                            var layout = new HorizontalLayoutWidget(INNER_LEFT, INNER_TOP, INNER_WIDTH - 1, 15, font, Component.literal(uniform.getKey()));
                            var info = menu.getSlot(0).getItem().get(BubbleLife.BUBBLE_INFO);
                            Float[] values;
                            if (info == null || !info.uniforms().containsKey(uniform.getKey())) {
                                values = uniform.getValue().defaultValues.clone();
                            } else {
                                values = info.uniforms().get(uniform.getKey()).toArray(Float[]::new);
                            }
                            for (int i = 0; i < uniform.getValue().size; i++) {
                                int finalI = i;
                                var input = createInput(Component.empty(), Double.MIN_VALUE, Double.MAX_VALUE, false, (value) -> {
                                    values[finalI] = (float) Double.parseDouble(value);
                                    onInputChange((v, builder) -> builder.uniform(uniform.getKey(), List.of(values))).accept(values);
                                });
                                input.setValue(String.valueOf(values[i]));
                                layout.addChild(input);
                                removableUniformWidgets.add(input);;
                            }
                            this.widgets.add(layout);
                            removableUniformWidgets.add(layout);
                            layout.visitWidgets(removableUniformWidgets::add);
                            layout.visitWidgets(this::addWidget);

                        }
                        recalculatePositions();
                },
                (shader) -> Tooltip.create(Component.translatable(shader.descriptionKey))),
                (widget, info) -> {
                    if (info == null) {
                        widget.setMessage(Component.empty());
                        lastShader[0] = "";
                        removableUniformWidgets.forEach(widgets::remove);
                        removableUniformWidgets.forEach(this::removeWidget);
                        removableUniformWidgets.clear();
                        return;
                    }
                    if (!Objects.equals(lastShader[0], info.shader())) {
                        widget.setValue(CustomBubbleShaders.getShader(info.shader()));
                        lastShader[0] = info.shader();
                    }
                });


        widgets.forEach(this::addRenderableWidget);
        recalculatePositions();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        guiGraphics.blit(this.background, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.fill(INNER_LEFT+ INNER_WIDTH - 2, INNER_TOP, INNER_LEFT + INNER_WIDTH, INNER_TOP + INNER_HEIGHT, 0xFF404040);
        float barSize =  (INNER_HEIGHT *  INNER_HEIGHT / contentHeight);
        guiGraphics.enableScissor(INNER_LEFT, INNER_TOP, INNER_WIDTH + INNER_LEFT, INNER_HEIGHT + INNER_TOP);
        guiGraphics.fill(INNER_LEFT+ INNER_WIDTH - 2, (int) (INNER_TOP + scrollOffs * (contentHeight - INNER_HEIGHT)), INNER_LEFT + INNER_WIDTH, (int) (INNER_TOP + barSize + scrollOffs * (contentHeight - INNER_HEIGHT)), 0xfff0f0f0);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 0);
        for (Renderable renderable : renderables2) {
            if (renderable == null) {
                continue;
            }
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        for (Renderable renderable : removableUniformWidgets) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void renderTransparentBackground(GuiGraphics guiGraphics) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        if (INNER_LEFT + INNER_WIDTH - 2 < mouseX && mouseX < INNER_LEFT + INNER_WIDTH) {
            if (INNER_TOP < mouseY && mouseY < INNER_TOP + INNER_HEIGHT) {
                this.scrolling = true;
                return true;
            }
        }
        var focused = this.getFocused();
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        if (this.getFocused() == focused && focused != null) {
            if (!focused.getRectangle().containsPoint((int) mouseX, (int) mouseY)) {
                focused.setFocused(false);
            }
        }
        return result;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling) {
            int j = INNER_TOP;
            int k = j + INNER_HEIGHT;
            float barSize =  (INNER_HEIGHT *  INNER_HEIGHT / contentHeight);
            this.scrollOffs = ((float) mouseY - (float) j - barSize/2) / ((float) (k - j) - barSize);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.recalculatePositions();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        float f = (float)scrollY / contentHeight;
        this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
        this.recalculatePositions();
        return true;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {

    }

    private void recalculatePositions() {
        int y = INNER_TOP;
        var oldHeight = contentHeight;
        for (AbstractWidget widget : widgets) {
            if (widget == null) {
                continue;
            }
            widget.setY((int) (y - scrollOffs * (contentHeight - INNER_HEIGHT)));
            y += Math.min(widget.getHeight(), 15) + 2;
        }
        contentHeight = y - INNER_TOP;
        scrollOffs = (oldHeight - INNER_HEIGHT)/ (contentHeight-INNER_HEIGHT) * scrollOffs;
        scrollOffs = Mth.clamp(scrollOffs, 0.0F, 1.0F);
        y = INNER_TOP;
        for (AbstractWidget widget : widgets) {
            if (widget == null) {
                continue;
            }
            widget.setY((int) (y - scrollOffs * (contentHeight - INNER_HEIGHT)));
            y += Math.min(widget.getHeight(), 15) + 2;
        }
    }

    private <T> Consumer<T> onInputChange(BiFunction<T, BubbleInfo.Builder, BubbleInfo.Builder> builder) {
        return (value) -> {
            ItemStack item = menu.blockEntity.getItem(0);
            var info = item.get(BubbleLife.BUBBLE_INFO);
            if (info != null) {
                item.set(BubbleLife.BUBBLE_INFO, builder.apply(value, new BubbleInfo.Builder(info)).build());
                this.minecraft.player.connection.send(new UpdateBubblePacket(item.get(BubbleLife.BUBBLE_INFO)));
            }
        };
    }

    private <T extends AbstractWidget> void addWidget(T widget, BiConsumer<T, BubbleInfo> listener) {
        this.widgets.add(widget);
        this.listeners.add((info) -> listener.accept(widget, info));
    }

    private <T extends AbstractWidget> void setWidget(int index, T widget, BiConsumer<T, BubbleInfo> listener) {
        this.widgets.set(index, widget);
        this.listeners.add((info) -> listener.accept(widget, info));
    }

    private <T> LabeledEditBox createInput(Component message, T min, T max, boolean isInteger, Consumer<String> consumer) {
        return new LabeledEditBox(font, INNER_LEFT, INNER_TOP, INNER_WIDTH - 1, 15, message,
                (value) -> isInteger ? value.matches("\\d*") : value.matches("\\d*\\.?\\d*"),
                (value) -> {
                    try {
                        if (isInteger) {
                            var number = Integer.parseInt(value);
                            return number >= (Integer) min && number <= (Integer) max;
                        } else {
                            var number = Double.parseDouble(value);
                            return number >= (Double) min && number <= (Double) max;
                        }
                    } catch (NumberFormatException nfe) {
                        return false;
                    }
                }, consumer);
    }

    private <T> LabeledEditBox createInput(Component message, T min, T max, boolean isInteger, BiFunction<T, BubbleInfo.Builder, BubbleInfo.Builder> consumer) {
        return createInput(message, min, max, isInteger, onInputChange((v, builder) -> {
            if (isInteger) {
                return consumer.apply((T) (Integer) Integer.parseInt(v), builder);
            } else {
                return consumer.apply((T) (Double) Double.parseDouble(v), builder);
            }
        }));
    }
}
