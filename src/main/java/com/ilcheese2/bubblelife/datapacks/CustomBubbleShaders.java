package com.ilcheese2.bubblelife.datapacks;

import com.google.gson.*;
import com.ilcheese2.bubblelife.BubbleLife;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class CustomBubbleShaders extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();

    public static final List<BubbleShader> SHADERS = new ArrayList<>();
    private static final Map<String, Integer> SHADERS_MAP = new LinkedHashMap<>();
    public static final Map<String, Pair<PostShaderInfo, PostChain>> POST_SHADERS = new LinkedHashMap<>();


    public static class PostShaderInfo {
        public Map<String, Integer> uniforms = new LinkedHashMap<>();
    }

    public CustomBubbleShaders() {
        super(GSON, "bubble_shaders");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        SHADERS.clear();
        SHADERS_MAP.clear();
        POST_SHADERS.clear();
        object.forEach((location, element) -> {
            try {
                JsonObject json = element.getAsJsonObject();
                if (location.getPath().endsWith("post")) {
                    var postJson = json.getAsJsonArray("post");
                    for (JsonElement jsonElement : postJson) {
                        JsonObject object1 = jsonElement.getAsJsonObject();
                        ResourceLocation postLocation = ResourceLocation.fromNamespaceAndPath(BubbleLife.MODID, "shaders/post/" + object1.get("name").getAsString() + ".json");
                        Minecraft client = Minecraft.getInstance();
                        PostChain postChain = new PostChain(client.getTextureManager(), client.getResourceManager(), client.getMainRenderTarget(), postLocation);
                        postChain.resize(client.getWindow().getWidth(), client.getWindow().getHeight());
                        var info = new PostShaderInfo();
                        for (var uniform: object1.getAsJsonArray("uniforms")) {
                            JsonObject object2 = uniform.getAsJsonObject();
                            info.uniforms.put(object2.get("name").getAsString(), object2.get("count").getAsInt());
                        }
                        POST_SHADERS.put(location.getNamespace() + ":" + object1.get("name").getAsString(), new Pair<>(info, postChain));
                    }
                } else {
                    String shaderLoc = json.get("shader").getAsString();

                    List<BubbleUniform.Intermediate> uniforms = new ArrayList<>();

                    var uniformJson = json.get("uniforms");
                    if (uniformJson != null) {
                        uniformJson.getAsJsonObject().asMap().forEach((name, uniform) -> {
                            JsonArray defaultValue = uniform.getAsJsonArray();
                            uniforms.add(new BubbleUniform.Intermediate(name, defaultValue.size(), defaultValue.asList().stream().map(JsonElement::getAsFloat).toArray(Float[]::new)));
                        });
                    }

                    var shaderResource = resourceManager.listResources("bubble_shaders", loc -> {
                        return loc.getNamespace().equals(location.getNamespace()) && loc.getPath().endsWith(shaderLoc + ".glsl");
                    });

                    if (shaderResource.size() != 1) {
                        throw new FileNotFoundException("Could not find shader: " + location.getNamespace() + ":" + shaderLoc);
                    }

                    Resource resource = shaderResource.values().stream().findFirst().get();
                    String shaderName = location.getNamespace() + ":" + shaderLoc;
                    InputStream stream = resource.open();
                    SHADERS.add(new BubbleShader(shaderName, json.get("name").getAsString(), json.get("description").getAsString(), stream, convertIntermediate(uniforms)));
                    SHADERS_MAP.put(SHADERS.getLast().name, SHADERS.size() - 1);
                    stream.close();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    // name, offset, (start in vec4, size)
    private Map<String, BubbleUniform> convertIntermediate(List<BubbleUniform.Intermediate> uniforms) { // require order
        Map<String, BubbleUniform> result = new LinkedHashMap<>();
        int sizeCount = 0;
        for (var uniform : uniforms) {
            if (uniform.size > 4) {
                throw new UnsupportedOperationException("Size of " + uniform.name + " is too large. (Must be less than four floats)");
            } else {
                if (uniform.size + sizeCount > 4) {
                    sizeCount = uniform.size;
                    result.put(uniform.name, new BubbleUniform(uniform.name, uniform.size, result.size(), 0, uniform.defaultData));
                } else {
                    sizeCount = uniform.size + sizeCount;
                    result.put(uniform.name, new BubbleUniform(uniform.name, uniform.size, result.size(), sizeCount - uniform.size, uniform.defaultData));
                }
            }
        }
        return result;
    }

    private static final List<String> LOGGED = new ArrayList<>();

    public static int getShaderIndex(String name) {
        if (!SHADERS_MAP.containsKey(name)) {
            if (!LOGGED.contains(name)) {
                LOGGER.warn("Could not find bubble shader: " + name);
                LOGGED.add(name);
            }
            return 0;
        }
        return SHADERS_MAP.get(name);
    }

    public static PostShaderInfo getPostShaderInfo(String name) {
        if (!POST_SHADERS.containsKey(name)) {
            if (!LOGGED.contains(name)) {
                LOGGER.warn("Could not find post shader: " + name);
                LOGGED.add(name);
            }
            return POST_SHADERS.get("bubblelife:blank").getFirst();
        }
        return POST_SHADERS.get(name).getFirst();
    }

    public static PostChain getPostShader(String name) {
        if (!POST_SHADERS.containsKey(name)) {
            if (!LOGGED.contains(name)) {
                LOGGER.warn("Could not find post shader: " + name);
                LOGGED.add(name);
            }
            return POST_SHADERS.get("bubblelife:blank").getSecond();
        }
        return POST_SHADERS.get(name).getSecond();
    }

    public static BubbleShader getShader(String name) {
        return SHADERS.get(getShaderIndex(name));
    }
}
