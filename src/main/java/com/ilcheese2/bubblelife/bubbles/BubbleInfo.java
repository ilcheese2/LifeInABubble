package com.ilcheese2.bubblelife.bubbles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record BubbleInfo(float speed, int range, String shader, boolean changesTime, Map<String, List<Float>> uniforms) {

    private static final Codec<Map<String, List<Float>>> UNIFORM_CODEC = Codec.unboundedMap(Codec.STRING, Codec.list(Codec.FLOAT));
    public static final Codec<BubbleInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("speed").forGetter(BubbleInfo::speed),
            Codec.INT.fieldOf("range").forGetter(BubbleInfo::range),
            Codec.STRING.fieldOf("shader").forGetter(BubbleInfo::shader),
            Codec.BOOL.fieldOf("changes_time").forGetter(BubbleInfo::changesTime),
            UNIFORM_CODEC.fieldOf("uniforms").forGetter(BubbleInfo::uniforms)
    ).apply(instance, BubbleInfo::new));

    public static final StreamCodec<ByteBuf, BubbleInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            BubbleInfo::speed,
            ByteBufCodecs.INT,
            BubbleInfo::range,
            ByteBufCodecs.STRING_UTF8,
            BubbleInfo::shader,
            ByteBufCodecs.BOOL,
            BubbleInfo::changesTime,
            ByteBufCodecs.fromCodec(UNIFORM_CODEC),
            BubbleInfo::uniforms,
            BubbleInfo::new
    );

    public BubbleInfo() {
        this(50, 10, "detached_times:red", true, new HashMap<>());
    }


    public static class Builder {
        private float speed;
        private int range;
        private String shader;
        private boolean changesTime;
        private Map<String, List<Float>> uniforms = new HashMap<>();

        public Builder(BubbleInfo info) {
            this.speed = info.speed();
            this.range = info.range();
            this.shader = info.shader();
            this.changesTime = info.changesTime();
        }

        public Builder speed(float speed) {
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

        public BubbleInfo build() {
            return new BubbleInfo(speed, range, shader, changesTime, uniforms);
        }
    }
}
