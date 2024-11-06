#version 150

vec4 flag(vec3 normal) { // must be named same as file
    vec4 colors[8] = vec4[](vec4(color1,1.0), vec4(color2, 1.0),vec4(color3,1.0),vec4(color4,1.0),vec4(color5,1.0),vec4(color6, 1.0),vec4(color7, 1.0),vec4(color8, 1.0));

    float pos = 1 - normal.y;
    float numColors = float(int(colorCount));
    vec4 color = (1.- step(1./numColors, pos)) * colors[0];
    for (int i = 1; i < numColors - 1.; i++) {
        color +=  step(i / numColors, pos) * (1.- step((i + 1)/numColors, pos)) * colors[i];
    }
    color += step((numColors - 1.)/numColors, pos) * colors[int(numColors - 1)];
    return vec4(color.rgb, 1);
}