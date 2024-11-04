package com.ilcheese2.bubblelife;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class BubbleLifeAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, BubbleLife.MODID);

    public static final Supplier<AttachmentType<Boolean>> REWIND = ATTACHMENT_TYPES.register(
            "rewind", () -> AttachmentType.builder(() -> false).build()
    );

    public static final Supplier<AttachmentType<Long>> TICK_SPEED = ATTACHMENT_TYPES.register(
            "mspt", () -> AttachmentType.builder(() -> 0L).build()
    );

    public static final Supplier<AttachmentType<Float>> TICK_RESIDUAL = ATTACHMENT_TYPES.register(
            "dt", () -> AttachmentType.builder(() -> 0f).build()
    );

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
