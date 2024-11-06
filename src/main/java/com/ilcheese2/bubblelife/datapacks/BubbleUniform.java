package com.ilcheese2.bubblelife.datapacks;

public class BubbleUniform {
    public String name;
    public int size;
    public int offset;
    public int innerOffset;
    public Float[] values;
    public Float[] defaultValues;

    public BubbleUniform(String name, int size, int offset, int innerOffset, Float[] defaultValues) {
        this.name = name;
        this.size = size;
        this.offset = offset;
        this.innerOffset = innerOffset;
        this.values = defaultValues;
        this.defaultValues = defaultValues;
    }

    public void setValues(Float[] values) {
        this.values = values;
    }

    public static class Intermediate {
        public String name;
        public int size;
        public Float[] defaultData;

        public Intermediate(String name, int size, Float[] defaultData) {
            this.name = name;
            this.size = size;
            this.defaultData = defaultData;
        }
    }

    public String getSwizzle() {
        String[] swizzle = new String[] { "x", "y", "z", "w" };
        StringBuilder builder = new StringBuilder();
        int i = this.innerOffset;

        while (i < size + innerOffset) {
            builder.append(swizzle[i]);
            i++;
        }
        return builder.toString();
    }
}
