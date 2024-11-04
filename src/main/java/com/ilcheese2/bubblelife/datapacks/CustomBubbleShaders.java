package com.ilcheese2.bubblelife.datapacks;

import com.google.gson.*;
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
    private static final Map<String, Integer> SHADERS_MAP = new HashMap<>();


    public CustomBubbleShaders() {
        super(GSON, "bubble_shaders");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        SHADERS.clear();
        SHADERS_MAP.clear();
        object.forEach((location, element) -> {
            try {
                JsonObject json = element.getAsJsonObject();
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
                SHADERS.add(new BubbleShader(shaderName, stream, convertIntermediate(uniforms)));
                SHADERS_MAP.put(SHADERS.getLast().name, SHADERS.size() - 1);
                stream.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    // name, offset, (start in vec4, size)
    private Map<String, BubbleUniform> convertIntermediate(List<BubbleUniform.Intermediate> uniforms) { // require order
        Map<String, BubbleUniform> result = new HashMap<>();
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

    public static BubbleShader getShader(String name) {
        return SHADERS.get(getShaderIndex(name));
    }
}
