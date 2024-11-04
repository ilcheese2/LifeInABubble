package com.ilcheese2.bubblelife.mixin;

import com.ilcheese2.bubblelife.DetachedTimesAttachments;
import com.ilcheese2.bubblelife.DetachedTimesConfig;
import com.ilcheese2.bubblelife.Utils;
import com.ilcheese2.bubblelife.networking.RewindPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Unique
    public final List<Tuple<Vec3, Long>> detachedTimes$movements = new ArrayList<>();

    @Inject(method = "tick", at = @At("HEAD"))
    void movePlayer(CallbackInfo ci) {
        if (((Entity) (Object) this).getData(DetachedTimesAttachments.REWIND)) {
            Entity entity = (Entity) (Object) this;

            if (detachedTimes$movements.isEmpty()) {
                PacketDistributor.sendToServer(new RewindPacket(false));
                entity.setData(DetachedTimesAttachments.REWIND, false);
                return;
            }

            var movement = detachedTimes$movements.removeLast();

            if (movement.getA().equals(Vec3.ZERO)) {
                return;
            }

            entity.xOld = entity.getX();
            entity.yOld = entity.getY();
            entity.zOld = entity.getZ();
            entity.setPos(movement.getA());
        }
        else {
            var player = (LocalPlayer) (Object) this;
            var newMovement = new Tuple<>(player.position(), Minecraft.getInstance().level.getGameTime());
            detachedTimes$movements.add(newMovement);
            Utils.filterOrderedList(detachedTimes$movements, (movement) -> {
                return newMovement.getB() - movement.getB() > DetachedTimesConfig.REWIND_TIME.get();
            });
        }
    }
}
