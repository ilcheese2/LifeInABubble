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
    private Consumer<ItemStack> listener = (stack) -> {};

    // Server menu constructor
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

    // Assume we have a data inventory of size 5
// The inventory has 4 inputs (index 1 - 4) which outputs to a result slot (index 0)
// We also have the 27 player inventory slots and the 9 hotbar slots
// As such, the actual slots are indexed like so:
//   - Data Inventory: Result (0), Inputs (1 - 4)
//   - Player Inventory (5 - 31)
//   - Player Hotbar (32 - 40)
    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        // The quick moved slot stack
        ItemStack quickMovedStack = ItemStack.EMPTY;
        // The quick moved slot
        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex);

        // If the slot is in the valid range and the slot is not empty
        if (quickMovedSlot != null && quickMovedSlot.hasItem()) {
            // Get the raw stack to move
            ItemStack rawStack = quickMovedSlot.getItem();
            // Set the slot stack to a copy of the raw stack
            quickMovedStack = rawStack.copy();

            // If the quick move was performed on the data inventory result slot
            if (quickMovedSlotIndex == 0) {
                // Try to move the result slot into the player inventory/hotbar
                if (!this.moveItemStackTo(rawStack, 1, 37, true)) {
                    // If cannot move, no longer quick move
                    return ItemStack.EMPTY;

                }

                // Perform logic on result slot quick move
                //slot.onQuickCraft(rawStack, quickMovedStack);
            }
            else {
                if (!this.moveItemStackTo(rawStack,0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Else if the quick move was performed on the player inventory or hotbar slot
//            else if (quickMovedSlotIndex >= 5 && quickMovedSlotIndex < 41) {
//                // Try to move the inventory/hotbar slot into the data inventory input slots
//                if (!this.moveItemStackTo(rawStack, 1, 5, false)) {
//                    // If cannot move and in player inventory slot, try to move to hotbar
//                    if (quickMovedSlotIndex < 32) {
//                        if (!this.moveItemStackTo(rawStack, 32, 41, false)) {
//                            // If cannot move, no longer quick move
//                            return ItemStack.EMPTY;
//                        }
//                    }
//                    // Else try to move hotbar into player inventory slot
//                    else if (!this.moveItemStackTo(rawStack, 5, 32, false)) {
//                        // If cannot move, no longer quick move
//                        return ItemStack.EMPTY;
//                    }
//                }
//            }
//            // Else if the quick move was performed on the data inventory input slots, try to move to player inventory/hotbar
//            else if (!this.moveItemStackTo(rawStack, 5, 41, false)) {
//                // If cannot move, no longer quick move
//                return ItemStack.EMPTY;
//            }

            if (rawStack.isEmpty()) {
                // If the raw stack has completely moved out of the slot, set the slot to the empty stack
                quickMovedSlot.set(ItemStack.EMPTY);
            } else {
                // Otherwise, notify the slot that that the stack count has changed
                quickMovedSlot.setChanged();
            }

    /*
    The following if statement and Slot#onTake call can be removed if the
    menu does not represent a container that can transform stacks (e.g.
    chests).
    */
            if (rawStack.getCount() == quickMovedStack.getCount()) {
                // If the raw stack was not able to be moved to another slot, no longer quick move
                return ItemStack.EMPTY;
            }
            // Execute logic on what to do post move with the remaining stack
            quickMovedSlot.onTake(player, rawStack);
        }

        return quickMovedStack; // Return the slot stack
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity.stillValid(player);
    }

    public void addListener(Consumer<ItemStack> listener) {
        this.listener = listener;
    }
}
