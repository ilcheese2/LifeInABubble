package com.ilcheese2.bubblelife.bubbles;

import com.ilcheese2.bubblelife.BubbleLifeAttachments;
import com.ilcheese2.bubblelife.networking.RewindPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class BubbleControllerServer extends BubbleController {
    private static BubbleControllerServer instance;
    private MinecraftServer server;

    public BubbleControllerServer(MinecraftServer server) {
        super();
        this.server = server;
        instance = this;
    }

    public static BubbleControllerServer instance() {
        return instance;
    }

    @Override
    protected void runOnMainThread(Runnable runnable) {
        server.execute(runnable);
    }

    @Override
    protected boolean isPaused() {
        if (server == null) {
            return false;
        }
        return server.isPaused();
    }

    public void startRewinding(Player player) {
        ServerPlayer player2 = ((ServerPlayer) player);

        runOnMainThread(() -> {
            PacketDistributor.sendToPlayer(player2, new RewindPacket(true));
            player2.setData(BubbleLifeAttachments.REWIND, true);
            player2.setDeltaMovement(Vec3.ZERO);
        });
    }

    public void updateEntities() {
        bubbles.forEach(Bubble::updateEntities);
    }
}
