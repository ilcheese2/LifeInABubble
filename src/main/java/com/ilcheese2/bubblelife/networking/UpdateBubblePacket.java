package com.ilcheese2.bubblelife.networking;


import com.ilcheese2.bubblelife.DetachedTimes;
import com.ilcheese2.bubblelife.bubbles.BubbleInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateBubblePacket(BubbleInfo info) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateBubblePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DetachedTimes.MODID, "bubble_update"));

    public static final StreamCodec<ByteBuf, UpdateBubblePacket> STREAM_CODEC = StreamCodec.composite(
            BubbleInfo.STREAM_CODEC,
            UpdateBubblePacket::info,
            UpdateBubblePacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void onUpdateBubblePacket(final UpdateBubblePacket packet, final IPayloadContext context) {
        context.player().containerMenu.getSlot(0).getItem().set(DetachedTimes.BUBBLE_INFO, packet.info());
    }
}
