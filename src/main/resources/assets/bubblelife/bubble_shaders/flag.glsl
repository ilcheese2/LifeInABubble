#version 150

vec4 detached_times_flag(vec3 normal) { // namespace_name
    vec4 colors[] = vec4[](vec4(0.95,0.0,0.0,1.0), vec4(1.0,141.0/255.0,0.0,1.0),vec4(1.0,0.95,0.0,1.0),vec4(0.0,129.0/255.0,33.0/255.0,1.0),vec4(0.0,76.0/255.0,1.0,1.0),vec4(118.0/255.0, 1.0/255.0, 136.0/255.0, 1.0));

    float pos = 1 - normal.y;
    vec4 color = (1.- step(1./6., pos)) * colors[0];
    for (int i = 1; i < 6 - 1; i++) {
        color +=  step((i) / 6., pos) * (1.- step((i + 1)/6., pos)) * colors[i];
    }
    color += step(5./6., pos) * colors[5];
    return vec4(color.rgb, 1);
}