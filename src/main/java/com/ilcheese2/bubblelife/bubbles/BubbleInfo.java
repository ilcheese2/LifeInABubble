package com.ilcheese2.bubblelife.bubbles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record BubbleInfo(int speed, int range, String shader, boolean changesTime, Map<String, List<Float>> uniforms, String postShader, Map<String, List<Float>> postUniforms) {

    private static final Codec<Map<String, List<Float>>> UNIFORM_CODEC = Codec.unboundedMap(Codec.STRING, Codec.list(Codec.FLOAT));
    public static final Codec<BubbleInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("speed").forGetter(BubbleInfo::speed),
            Codec.INT.fieldOf("range").forGetter(BubbleInfo::range),
            Codec.STRING.fieldOf("shader").forGetter(BubbleInfo::shader),
            Codec.BOOL.fieldOf("changes_time").forGetter(BubbleInfo::changesTime),
            UNIFORM_CODEC.fieldOf("uniforms").forGetter(BubbleInfo::uniforms),
            Codec.STRING.fieldOf("postShader").forGetter(BubbleInfo::postShader),
            UNIFORM_CODEC.fieldOf("postUniforms").forGetter(BubbleInfo::postUniforms)
    ).apply(instance, BubbleInfo::new));

    public static final StreamCodec<ByteBuf, BubbleInfo> STREAM_CODEC =  ByteBufCodecs.fromCodec(CODEC);

    public BubbleInfo() {
        this(50, 10, "bubblelife:red", true, new HashMap<>(), "bubblelife:blank", new HashMap<>());
    }


    public static class Builder {
        private int speed;
        private int range;
        private String shader;
        private boolean changesTime;
        private Map<String, List<Float>> uniforms = new HashMap<>();
        private String postShader;
        private Map<String, List<Float>> postUniforms = new HashMap<>();

        public Builder(BubbleInfo info) {
            this.speed = info.speed();
            this.range = info.range();
            this.shader = info.shader();
            this.changesTime = info.changesTime();
            this.uniforms = new HashMap<>(info.uniforms);
            this.postShader = info.postShader();
            this.postUniforms = new HashMap<>(info.postUniforms);
        }

        public Builder speed(int speed) {
            this.speed = speed;
            return this;
        };

        public Builder range(int range) {
            this.range = range;
            return this;
        };

        public Builder shader(String shader) {
            this.shader = shader;
            return this;
        }

        public Builder changesTime(boolean changesTime) {
            this.changesTime = changesTime;
            return this;
        }

        public Builder uniform(String name, List<Float> values) {
            uniforms.put(name, values);
            return this;
        }

        public Builder postShader(String postShader) {
            this.postShader = postShader;
            return this;
        }

        public Builder postUniform(String name, List<Float> values) {
            postUniforms.put(name, values);
            return this;
        }

        public Builder clearUniforms() {
            uniforms.clear();
            return this;
        }

        public Builder clearPostUniforms() {
            postUniforms.clear();
            return this;
        }

        public BubbleInfo build() {
            return new BubbleInfo(speed, range, shader, changesTime, uniforms, postShader, postUniforms);
        }
    }
}
