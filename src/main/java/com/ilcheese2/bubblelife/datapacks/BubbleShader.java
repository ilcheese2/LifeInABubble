package com.ilcheese2.bubblelife.datapacks;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class BubbleShader {

    public String name;
    public String shaderName;
    public Program compiledShader;
    public Map<String, BubbleUniform> uniforms;

    public BubbleShader(String name, InputStream shader, Map<String, BubbleUniform> uniforms) throws IOException {
        this.name = name;
        this.shaderName = name.replace(":", "_");
        GlslPreprocessor preprocessor = new GlslPreprocessor() {
            @Override
            public List<String> process(String shaderData) {
                var builder = new StringBuilder();
                shaderData = shaderData.replace("%s(vec3 normal)".formatted(shaderName), "%s(vec3 normal, int bubbleOffset)".formatted(shaderName));
                var builder2 = new StringBuilder(shaderData);

                if (BubbleShader.this.uniforms != null && !BubbleShader.this.uniforms.isEmpty()) {
                    BubbleShader.this.uniforms.forEach((name, uniform) -> {
                        builder.append("#define %s texelFetch(DataSampler, bubbleOffset + %d, 0).%s\n".formatted(name, uniform.offset + 2, uniform.getSwizzle()));
                    });
                }
                builder.append("uniform sampler1D DataSampler;\n");
                builder2.insert(shaderData.indexOf("\n", shaderData.indexOf("#version"))+1, builder);
                return super.process(builder2.toString());
            }

            @Override
            public @NotNull String applyImport(boolean useFullPath, @NotNull String directory) {
                return "";
            }
        };
        this.uniforms = uniforms;
        compiledShader = Program.compileShader(Program.Type.FRAGMENT, name, shader, name, preprocessor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BubbleShader shader) {
            return name.equals(shader.name);
        }
        return false;
    }
}
