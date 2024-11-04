package com.ilcheese2.bubblelife.mixin;

import com.ilcheese2.bubblelife.datapacks.CustomBubbleShaders;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;

@Mixin(Program.class)
public class ProgramMixin {

    @Unique
    private static final String TEMPLATE = """
            
            vec4 sphereTexture(vec3 normal, int bubbleOffset, int color) {
                switch (color) {
            """;

    @Unique
    private static final String TEMPLATE_END = """
                }
                return vec4(0.0, 0.0, 0.0, 1.0);
            }
            """;

    @Unique
    private static final String PLACEHOLDER = "vec4 sphereTexture(vec3 normal, int bubbleOffset, int color);";

    //@ModifyArg(method = "compileShaderInternal", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/preprocessor/GlslPreprocessor;process(Ljava/lang/String;)Ljava/util/List;"))
    @Inject(method = "compileShaderInternal", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;glCreateShader(I)I"))
    private static void addSwitchFunction(Program.Type type, String name, InputStream shaderData, String sourceName, GlslPreprocessor preprocessor, CallbackInfoReturnable<Integer> cir, @Local(ordinal = 2) LocalRef<String> shaderText) {
        if (type != Program.Type.FRAGMENT || !name.contains("bubble")) {
           return;
        }

        StringBuilder switchFunction = new StringBuilder();

        for (var shader: CustomBubbleShaders.SHADERS) {
            switchFunction.append("vec4 %s(vec3 normal, int bubbleOffset);\n".formatted(shader.shaderName));
        }

        switchFunction.append(TEMPLATE);
        int i = 0;
        for (var shader: CustomBubbleShaders.SHADERS) {
            switchFunction.append("       case %d:\n".formatted(i));
            switchFunction.append("            return %s(normal, bubbleOffset);\n".formatted(shader.shaderName));
            i++;
        }
        switchFunction.append(TEMPLATE_END);

        shaderText.set(shaderText.get().replace(PLACEHOLDER, switchFunction.toString()));
        return;
    }
}
