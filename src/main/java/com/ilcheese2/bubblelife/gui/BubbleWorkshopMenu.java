package com.ilcheese2.bubblelife.gui;

import com.ilcheese2.bubblelife.BubbleLife;
import com.ilcheese2.bubblelife.bubbles.workshop.BubbleWorkshopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class BubbleWorkshopMenu extends AbstractContainerMenu {

    public BubbleWorkshopBlockEntity blockEntity;
    private Consumer<ItemStack> listener = (stack) -> {
    };

    public BubbleWorkshopMenu(int containerId, Inventory playerInventory, Level level, BlockPos pos) {
        super(BubbleLife.BUBBLE_WORKSHOP_MENU.get(), containerId);
        blockEntity = (BubbleWorkshopBlockEntity) level.getBlockEntity(pos);
        this.addSlot(new Slot(blockEntity, 0, 15, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return blockEntity.canPlaceItem(0, stack);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                BubbleWorkshopMenu.this.listener.accept(this.getItem());
            }
        });
        this.createInventorySlots(playerInventory);


    }

    private void createInventorySlots(Inventory inventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        ItemStack quickMovedStack = ItemStack.EMPTY;
        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex);

        if (quickMovedSlot != null && quickMovedSlot.hasItem()) {
            ItemStack rawStack = quickMovedSlot.getItem();
            quickMovedStack = rawStack.copy();

            if (quickMovedSlotIndex == 0) {
                if (!this.moveItemStackTo(rawStack, 1, 37, true)) {
                    return ItemStack.EMPTY;

                }
            } else {
                if (!this.moveItemStackTo(rawStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (rawStack.isEmpty()) {
                quickMovedSlot.set(ItemStack.EMPTY);
            } else {
                quickMovedSlot.setChanged();
            }


            if (rawStack.getCount() == quickMovedStack.getCount()) {
                return ItemStack.EMPTY;
            }
            quickMovedSlot.onTake(player, rawStack);
        }

        return quickMovedStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity.stillValid(player);
    }

    public void addListener(Consumer<ItemStack> listener) {
        this.listener = listener;
    }
}
