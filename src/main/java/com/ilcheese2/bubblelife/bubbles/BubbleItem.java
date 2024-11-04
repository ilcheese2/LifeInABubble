package com.ilcheese2.bubblelife.bubbles;

import com.ilcheese2.bubblelife.BubbleLifeConfig;
import com.ilcheese2.bubblelife.BubbleLife;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class BubbleItem extends Item {

    public BubbleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal(stack.get(BubbleLife.BUBBLE_INFO.get()).toString()));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide) {
            Bubble bubble = BubbleControllerServer.instance().ownsBubble(player);
            if (bubble == null) {
                if (!player.getCooldowns().isOnCooldown(this) && BubbleControllerServer.instance().inBubblePosition(player.position()) == null) {
                    var newBubble = new Bubble(level, player.getItemInHand(usedHand).get(BubbleLife.BUBBLE_INFO), player);
                    newBubble.setPos(player.position());
                    level.addFreshEntity(newBubble);
                    player.getCooldowns().addCooldown(this, BubbleLifeConfig.BUBBLE_COOLDOWN.get());
                }
            } else {
                bubble.discard();
                player.getCooldowns().removeCooldown(this);
            }
            return InteractionResultHolder.success(player.getItemInHand(usedHand));

        }
        return InteractionResultHolder.pass(player.getItemInHand(usedHand));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        //stack.
    }
}
