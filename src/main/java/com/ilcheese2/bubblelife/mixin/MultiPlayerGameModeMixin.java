package com.ilcheese2.bubblelife.mixin;

import com.ilcheese2.bubblelife.DetachedTimes;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @WrapOperation(method = "lambda$useItem$5", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;isOnCooldown(Lnet/minecraft/world/item/Item;)Z"))
    boolean changeCooldownCheck(ItemCooldowns instance, Item item, Operation<Boolean> original) {
        if (item == DetachedTimes.BUBBLE_ITEM.asItem()) {
            return false;
        }
        return original.call(instance, item);
    }
}
