package com.ilcheese2.bubblelife.networking;

import com.ilcheese2.bubblelife.BubbleLife;
import com.ilcheese2.bubblelife.BubbleLifeAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RewindPacket(boolean rewinding) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RewindPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(BubbleLife.MODID, "rewind"));

    public static final StreamCodec<ByteBuf, RewindPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            RewindPacket::rewinding,
            RewindPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void onRewindPacket(final RewindPacket packet, final IPayloadContext context) {
        context.player().setData(BubbleLifeAttachments.REWIND, packet.rewinding());

        if (packet.rewinding) {
            context.player().setDeltaMovement(Vec3.ZERO); // gravity hack
        }
    }
}