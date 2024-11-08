package com.ilcheese2.bubblelife.client;

import com.ilcheese2.bubblelife.BubbleLife;
import com.ilcheese2.bubblelife.bubbles.Bubble;
import com.ilcheese2.bubblelife.client.render.BubbleRenderer;
import com.ilcheese2.bubblelife.datapacks.CustomBubbleShaders;
import com.ilcheese2.bubblelife.gui.BubbleWorkshopMenu;
import com.ilcheese2.bubblelife.gui.BubbleWorkshopScreen;
import com.ilcheese2.bubblelife.mixin.PostChainAccessor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.*;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

@Mod(value = BubbleLife.MODID, dist = Dist.CLIENT)
public class BubbleLifeClient {

    public static ShaderInstance playerGlitch;

    public static PostChain bubbleShader;

    public static final Function<ResourceLocation, RenderType> PLAYER_GLITCH = Util.memoize(
            (texture) -> {
                RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> playerGlitch))
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .createCompositeState(true);
                return RenderType.create("player_glitch", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, rendertype$compositestate);
            }
    );


    @EventBusSubscriber(modid = BubbleLife.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ClientGameEvents {
        @SubscribeEvent
        private static void onRenderLevel(RenderLevelStageEvent event) {
            // https://github.com/LodestarMC/Lodestone/blob/main/src/main/java/team/lodestar/lodestone/systems/postprocess/PostProcessHandler.java
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
                if (bubbleShader == null) {
                    createBubbleShader();
                }

                updateUniforms(event.getModelViewMatrix());
                if ( bubbleShader != null) {
                    EffectInstance effect = ((PostChainAccessor) bubbleShader).getPasses().getFirst().getEffect();
                    effect.setSampler("DataSampler", () -> BubbleControllerClient.instance().dataTexture);
                    if (!Minecraft.getInstance().isPaused()) {
                        bubbleShader.process(event.getPartialTick().getGameTimeDeltaTicks());
                    }
                }
                Bubble bubble = BubbleControllerClient.instance().inBubblePosition(Minecraft.getInstance().player.position());
                if (bubble != null) {
                    PostChain chain = CustomBubbleShaders.getPostShader(bubble.info.postShader());
                    for (PostPass postpass : ((PostChainAccessor) chain).getPasses()) {
                        for (var uniform : bubble.info.postUniforms().entrySet()) {
                            if (uniform.getValue().stream().allMatch(Objects::nonNull)) {
                                postpass.getEffect().safeGetUniform(uniform.getKey()).set(ArrayUtils.toPrimitive(uniform.getValue().toArray(Float[]::new)));
                            }
                        }
                    }

                    chain.process(event.getPartialTick().getGameTimeDeltaTicks());
                }

                Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            }
        }

        @SubscribeEvent
        public static void onCommandsRegister(RegisterClientCommandsEvent event) {
//            event.getDispatcher().register(Commands.literal("reloads").executes((context) -> {
//                createBubbleShader();
//                return 1;
//            }));
        }

        @SubscribeEvent
        public static void onJoinLevel(LevelEvent.Load event) {
            BubbleControllerClient.instance().clearBubbles();
        }
    }

    @EventBusSubscriber(modid = BubbleLife.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            new BubbleControllerClient();
        }

        @SubscribeEvent
        public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
            event.registerShader(new ShaderInstance(
                    event.getResourceProvider(),
                    ResourceLocation.fromNamespaceAndPath(BubbleLife.MODID, "player_glitch"),
                    DefaultVertexFormat.NEW_ENTITY), (shader) -> playerGlitch = shader);
            BubbleControllerClient.instance().createBuffers();
        }

        @SubscribeEvent
        private static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(BubbleLife.BUBBLE.get(), BubbleRenderer::new);
        }

        @SubscribeEvent
        private static void onRegisterScreens(RegisterMenuScreensEvent event) {
            MenuScreens.ScreenConstructor<BubbleWorkshopMenu, BubbleWorkshopScreen> constructor = BubbleWorkshopScreen::new;
            event.register(BubbleLife.BUBBLE_WORKSHOP_MENU.get(), constructor);
        }

        @SubscribeEvent
        public static void buildContents(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                event.accept(BubbleLife.BUBBLE_WORKSHOP_BLOCK.get());
                event.accept(BubbleLife.BUBBLE_ITEM.get());
            }
        }
    }

    private static void createBubbleShader() {
        if (bubbleShader != null) {
            bubbleShader.close();
        }

        if (CustomBubbleShaders.SHADERS.isEmpty()) {
            return;
        }
        try {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(BubbleLife.MODID, "shaders/post/bubble.json");
            Minecraft client = Minecraft.getInstance();
            bubbleShader = new PostChain(client.getTextureManager(), client.getResourceManager(), client.getMainRenderTarget(), location) {
                @Override
                public PostPass addPass(String name, RenderTarget inTarget, RenderTarget outTarget, boolean useLinearFilter) throws IOException {
                    return super.addPass(name, inTarget, outTarget, useLinearFilter);
                }
            };


            bubbleShader.resize(client.getWindow().getWidth(), client.getWindow().getHeight());
            BubbleControllerClient.instance().updateShader();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateUniforms(Matrix4f matrix) {
        if (bubbleShader == null) return;

        EffectInstance effect = ((PostChainAccessor) bubbleShader).getPasses().getFirst().getEffect();
        effect.safeGetUniform("CamPos").set(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f());
        effect.safeGetUniform("ProjInvMat").set(new Matrix4f(RenderSystem.getProjectionMatrix()).invert());
        effect.safeGetUniform("ModelViewInvMat").set(new Matrix4f(matrix).invert());
        effect.setSampler("DepthSampler", () -> Minecraft.getInstance().getMainRenderTarget().getDepthTextureId());

    }

    public BubbleLifeClient(IEventBus bus) {
        bus.addListener((RegisterClientReloadListenersEvent event) -> {
            event.registerReloadListener( new CustomBubbleShaders());
        });
    }
}
