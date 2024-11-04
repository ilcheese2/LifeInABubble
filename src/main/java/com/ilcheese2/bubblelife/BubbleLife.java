package com.ilcheese2.bubblelife;

import com.ilcheese2.bubblelife.bubbles.workshop.BubbleWorkshopBlockEntity;
import com.ilcheese2.bubblelife.bubbles.workshop.BubbleWorkshopBlock;
import com.ilcheese2.bubblelife.bubbles.BubbleController;
import com.ilcheese2.bubblelife.bubbles.BubbleInfo;
import com.ilcheese2.bubblelife.bubbles.Bubble;
import com.ilcheese2.bubblelife.gui.BubbleWorkshopMenu;
import com.ilcheese2.bubblelife.bubbles.BubbleItem;
import com.ilcheese2.bubblelife.networking.RewindPacket;
import com.ilcheese2.bubblelife.bubbles.BubbleControllerServer;
import com.ilcheese2.bubblelife.items.TimeItem;
import com.ilcheese2.bubblelife.networking.UpdateBubblePacket;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(DetachedTimes.MODID)
public class DetachedTimes {
    public static final String MODID = "detached_times";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredBlock<Block> BUBBLE_WORKSHOP_BLOCK = BLOCKS.registerBlock("bubble_workshop", BubbleWorkshopBlock::new, BlockBehaviour.Properties.of());

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, DetachedTimes.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BubbleInfo>> BUBBLE_INFO = DATA_COMPONENTS.registerComponentType(
            "bubble_info",
            builder -> builder
                    .persistent(BubbleInfo.CODEC)
                    .networkSynchronized(BubbleInfo.STREAM_CODEC)
    );

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerItem("example_item", TimeItem::new, new Item.Properties().food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()));
    public static final DeferredItem<Item> BUBBLE_ITEM = ITEMS.registerItem("bubble", (properties) -> new BubbleItem(
            new Item.Properties().stacksTo(1).component(BUBBLE_INFO::value, new BubbleInfo())
    ));
    public static final DeferredItem<BlockItem> BUBBLE_WORKSHOP_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", BUBBLE_WORKSHOP_BLOCK);

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<Bubble>> BUBBLE = ENTITIES.register("bubble",
            () -> EntityType.Builder.<Bubble>of(
                            (type, world) -> new Bubble(world),
                            MobCategory.MISC
                    )
                    .build(MODID + ":bubble"));

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DetachedTimes.MODID);
    public static final Supplier<BlockEntityType<BubbleWorkshopBlockEntity>> BUBBLE_WORKSHOP_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "bubble_workshop",
            () -> BlockEntityType.Builder.of(
                            BubbleWorkshopBlockEntity::new,
                            DetachedTimes.BUBBLE_WORKSHOP_BLOCK.get()
                    )
                    .build(null)
    );

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, DetachedTimes.MODID);
    public static final Supplier<MenuType<BubbleWorkshopMenu>> BUBBLE_WORKSHOP_MENU = MENU_TYPES.register("bubble_workshop", () -> IMenuTypeExtension.create((containerId, playerInventory, byteBuf) -> {
        Level level = playerInventory.player.level();
        BlockPos pos = byteBuf.readBlockPos();
        return new BubbleWorkshopMenu(containerId, playerInventory, level, pos);
    }));

    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, DetachedTimes.MODID);
    public static final EntityDataSerializer<BubbleInfo> BUBBLE_INFO_SERIALIZER = EntityDataSerializer.forValueType(BubbleInfo.STREAM_CODEC);

    static {
        ENTITY_DATA_SERIALIZERS.register("bubble_info", () -> BUBBLE_INFO_SERIALIZER);
    }


    private static BubbleControllerServer controller;

    public DetachedTimes(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        ENTITY_DATA_SERIALIZERS.register(modEventBus);

        DetachedTimesAttachments.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::register);

        modContainer.registerConfig(ModConfig.Type.COMMON, DetachedTimesConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        controller = new BubbleControllerServer(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        controller.shutdownNow();
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Pre event) {
        controller.updateEntities();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityTick(EntityTickEvent.Pre event) {
        if (BubbleController.inBubble(event.getEntity(), event.getEntity().level().isClientSide)) {
            event.setCanceled(true);
        }
    }

    public void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(RewindPacket.TYPE, RewindPacket.STREAM_CODEC, RewindPacket::onRewindPacket);
        registrar.playToServer(UpdateBubblePacket.TYPE, UpdateBubblePacket.STREAM_CODEC, UpdateBubblePacket::onUpdateBubblePacket);
    }



}
