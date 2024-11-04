package com.ilcheese2.bubblelife.gui;

import com.ilcheese2.bubblelife.DetachedTimes;
import com.ilcheese2.bubblelife.bubbles.BubbleInfo;
import com.ilcheese2.bubblelife.datapacks.BubbleShader;
import com.ilcheese2.bubblelife.datapacks.BubbleUniform;
import com.ilcheese2.bubblelife.datapacks.CustomBubbleShaders;
import com.ilcheese2.bubblelife.networking.UpdateBubblePacket;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.inject.GreedyInputComponent;
import io.wispforest.owo.ui.util.DisposableScreen;
import io.wispforest.owo.ui.util.UIErrorToast;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;


public class BubbleWorkshopScreen extends AbstractContainerScreen<BubbleWorkshopMenu> implements DisposableScreen {

    protected @NotNull OwoUIAdapter<ParentComponent> uiAdapter = null;
    protected boolean invalid = false;
    private final ResourceLocation background = ResourceLocation.fromNamespaceAndPath(DetachedTimes.MODID, "textures/gui/workshop.png");
    private Consumer<BubbleInfo> subscriber = (info) -> {};
    private final Consumer<ItemStack> listener = (itemStack) -> {
        if (itemStack.isEmpty()) {
            BubbleWorkshopScreen.this.subscriber.accept(null);
            return;
        }
        var info = itemStack.get(DetachedTimes.BUBBLE_INFO);
        if (info != null) {
            BubbleWorkshopScreen.this.subscriber.accept(info);
        }
    };

    public BubbleWorkshopScreen(BubbleWorkshopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        menu.addListener(listener);
        this.leftPos = 125;
        this.topPos = 37;
    }


    protected @NotNull OwoUIAdapter<ParentComponent> createAdapter() {
        return OwoUIAdapter.create(this, (sizing, sizing1) -> {
            return Containers.verticalFlow(sizing, sizing1);
        });
    }


    protected void build(ParentComponent root) {
        //root.surface(Surface.VANILLA_TRANSLUCENT);
        var vert = Containers.verticalFlow(Sizing.content(), Sizing.content());
        var scroll = Containers.verticalScroll(Sizing.fixed(120), Sizing.fixed(53), vert).scrollbar(ScrollContainer.Scrollbar.vanillaFlat()).positioning(Positioning.absolute(172, 52));
        ((FlowLayout) root).child(scroll);

        TextBoxComponent range = new NumberTextBoxComponent(Sizing.expand(), 10, 20);
        range.onChanged().subscribe((s) -> this.<String>onInputChange((value, builder) -> {
            if (value.isEmpty()) {
                return builder;
            }
            return builder.range(Integer.parseInt(value));
        }).accept(s));

        TextBoxComponent speed = new NumberTextBoxComponent(Sizing.expand(), 50, 500);
        speed.onChanged().subscribe((s) -> this.<String>onInputChange((value, builder) -> {
            if (value.isEmpty()) {
                return builder;
            }
            return builder.speed(Integer.parseInt(value));
        }).accept(s));

        CheckboxComponent changesTime = Components.checkbox(Component.literal(""));
        changesTime.onChanged(onInputChange((value, builder) -> builder.changesTime(value)));

        var shaderContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        var shaderScroll = Containers.horizontalScroll(Sizing.fixed(120), Sizing.fixed(24), shaderContainer).scrollbar(ScrollContainer.Scrollbar.vanillaFlat()).horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.BOTTOM);
        List<ButtonComponent> components = new ArrayList<>();
        var uniformContainer = Containers.verticalFlow(Sizing.expand(), Sizing.content());

        for (BubbleShader shader : CustomBubbleShaders.SHADERS) {
            var shaderButton = Components.button(Component.literal(shader.name), onInputChange((component, builder) -> {
                uniformContainer.clearChildren();
                component.active(false);
                for (ButtonComponent buttonComponent : components) {
                    if (buttonComponent != component) {
                        buttonComponent.active(true);
                    }
                }
                CustomBubbleShaders.getShader(shader.name).uniforms.forEach((name, uniform) -> {
                    var uniformRow = makeUniformInput(97, uniform, getBubbleInfo(), uniform.size);
                    uniformContainer.child(uniformRow);
                    if (!uniformContainer.children().isEmpty()) {
                        uniformContainer.child(0, Components.label(Component.translatable("menu.detached_times.workshop.uniforms")).color(Color.ofArgb(4210752)));
                    }
                });
                return builder.shader(shader.name);
            }));
            components.add(shaderButton);
            shaderContainer.child((io.wispforest.owo.ui.core.Component) shaderButton);
        }
        AtomicReference<String> prevShader = new AtomicReference<>("");
        subscriber = ((info) -> {
            if (info == null) {
                range.text("");
                speed.text("");
                changesTime.checked(false);
                components.forEach((button) -> button.active(true));
                uniformContainer.clearChildren();
                prevShader.set("");
                return;
            }
            range.text(Integer.toString(info.range()));
            speed.text(Integer.toString((int) info.speed()));
            changesTime.checked(info.changesTime());
            components.forEach((button) -> button.active(!button.getMessage().getString().equals(info.shader())));
            if (prevShader.get().equals(info.shader())) return;
            prevShader.set(info.shader());
            uniformContainer.clearChildren();
            CustomBubbleShaders.getShader(info.shader()).uniforms.forEach((name, uniform) -> {
                var uniformRow = makeUniformInput(97, uniform, info, uniform.size);
                uniformContainer.child(uniformRow);
            });
            if (!uniformContainer.children().isEmpty()) {
                uniformContainer.child(0, Components.label(Component.translatable("menu.detached_times.workshop.uniforms")).color(Color.ofArgb(4210752)));
            }
        });

        subscriber.accept(getBubbleInfo());

        vert.child(makeInputRow(97, Component.translatable("menu.detached_times.workshop.speed"), speed))
                .child(makeInputRow(97, Component.translatable("menu.detached_times.workshop.range"), range))
                .child(makeInputRow(97, Component.translatable("menu.detached_times.workshop.changes_time"), changesTime))
                .child(Components.label(Component.translatable("menu.detached_times.workshop.shaders")).color(Color.ofArgb(4210752)))
                .child(Containers.horizontalFlow(Sizing.expand(96), Sizing.content()).child(shaderScroll))
                .child(uniformContainer);
    }

    private BubbleInfo getBubbleInfo() {
        return menu.getSlot(0).getItem().get(DetachedTimes.BUBBLE_INFO);
    }

    private <T> Consumer<T> onInputChange(BiFunction<T, BubbleInfo.Builder, BubbleInfo.Builder> builder) {
        return (value) -> {
            ItemStack item = menu.blockEntity.getItem(0);
            var info = item.get(DetachedTimes.BUBBLE_INFO);
            if (info != null) {item.set(DetachedTimes.BUBBLE_INFO, builder.apply(value, new BubbleInfo.Builder(info)).build());
                minecraft.player.connection.send(new UpdateBubblePacket(item.get(DetachedTimes.BUBBLE_INFO)));
            }
        };
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    private <T> FlowLayout makeInputRow(int width, Component label, T child) {
        return (FlowLayout) Containers.horizontalFlow(Sizing.expand(width), Sizing.content())
                .child(Components.label(label).color(Color.ofArgb(4210752)))
                .child((io.wispforest.owo.ui.core.Component) child)
                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
    }

    private FlowLayout makeUniformInput(int width, BubbleUniform uniform, BubbleInfo info, int size) {
        Float[] values;
        if (info == null || !info.uniforms().containsKey(uniform.name)) {
            values = uniform.defaultValues.clone();
        } else {
            values = info.uniforms().get(uniform.name).toArray(Float[]::new);
        }
        var layout = (FlowLayout) Containers.horizontalFlow(Sizing.expand(width), Sizing.content())
                .child(Components.label(Component.literal(uniform.name)).color(Color.ofArgb(4210752)))
                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        for (int i = 0; i < size; i++) {
            int finalI = i;
            var input = new FloatTextBoxComponent(Sizing.expand(100/size));
            input.onChanged().subscribe((s) -> this.<String>onInputChange((value, builder) -> {
                try {
                    values[finalI] = Float.parseFloat(value);
                    return builder.uniform(uniform.name,  List.of(values));
                } catch (NumberFormatException e) {
                    return builder;
                }
            }).accept(s));
            input.text(values[i].toString());
            layout.child((io.wispforest.owo.ui.core.Component) input);
        }
        return layout;
    }

    @Override // copied from BaseOwoScreen as per documentation
    protected void init() {
        if (this.invalid) return;

        // Check whether this screen was already initialized
        if (this.uiAdapter != null) {
            // If it was, only resize the adapter instead of recreating it - this preserves UI state
            this.uiAdapter.moveAndResize(0, 0, this.width, this.height);
            // Re-add it as a child to circumvent vanilla clearing them
            this.addRenderableWidget(this.uiAdapter);
        } else {
            try {
                this.uiAdapter = this.createAdapter();
                this.build(this.uiAdapter.rootComponent);

                this.uiAdapter.inflateAndMount();
            } catch (Exception error) {
                Owo.LOGGER.warn("Could not initialize owo screen", error);
                UIErrorToast.report(error);
                this.invalid = true;
            }
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!this.invalid) {
            super.render(context, mouseX, mouseY, delta);
            this.renderTooltip(context, mouseX, mouseY);
        } else {
            this.onClose();
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(this.background, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.uiAdapter == null) return false;

        if ((modifiers & GLFW.GLFW_MOD_CONTROL) == 0
                && this.uiAdapter.rootComponent.focusHandler().focused() instanceof GreedyInputComponent inputComponent
                && inputComponent.onKeyPress(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.uiAdapter == null) return false;

        if (!this.uiAdapter.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return false;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.uiAdapter;
    }

    @Override
    public void removed() {
        if (this.uiAdapter != null) {
            this.uiAdapter.cursorAdapter.applyStyle(CursorStyle.NONE);
        }
    }

    @Override
    public void dispose() {
        if (this.uiAdapter != null) this.uiAdapter.dispose();
    }
}
