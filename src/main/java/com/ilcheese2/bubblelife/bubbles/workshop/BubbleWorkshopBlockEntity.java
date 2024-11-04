package com.ilcheese2.bubblelife.bubbles.workshop;

import com.ilcheese2.bubblelife.BubbleLife;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BubbleWorkshopBlockEntity extends BlockEntity implements Container {

    private ItemStack item = ItemStack.EMPTY;

    public BubbleWorkshopBlockEntity(BlockPos pos, BlockState blockState) {
        super(BubbleLife.BUBBLE_WORKSHOP_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return item.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? item : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (amount > 1 || slot != 0) {
            return ItemStack.EMPTY;
        }
        var oldItem = item;
        item = ItemStack.EMPTY;
        this.setChanged();
        return oldItem;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        var oldItem = item;
        item = ItemStack.EMPTY;
        this.setChanged();
        return oldItem;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            item = stack;
        }
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        item = ItemStack.EMPTY;
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(BubbleLife.BUBBLE_ITEM.get());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Item")) {
            item = ItemStack.parse(registries, tag.getCompound("Item")).orElse(ItemStack.EMPTY);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        if (!item.isEmpty()) {
            CompoundTag tag1 = new CompoundTag();
            tag.put("Item", item.save(registries, tag1));
        }

        super.saveAdditional(tag, registries);
    }
}


