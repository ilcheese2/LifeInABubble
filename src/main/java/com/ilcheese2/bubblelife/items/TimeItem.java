package com.ilcheese2.bubblelife.items;

import com.ilcheese2.bubblelife.bubbles.BubbleControllerServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TimeItem extends Item {
    public TimeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide) {
            BubbleControllerServer.instance().startRewinding(player);
        }
        return super.use(level, player, usedHand);
    }
}
