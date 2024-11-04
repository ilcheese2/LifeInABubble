#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float GameTime2;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

float random (in vec2 st) {
    return fract(sin(dot(st.xy,
                         vec2(12.9898,78.233)))*
        43758.5453123);
}

vec2 random2( vec2 p ) {
    return fract(sin(vec2(dot(p,vec2(127.1,311.7)),dot(p,vec2(269.5,183.3))))*43758.5453);
}

float worley(vec2 uv, float numSquared) {
    // thanks you book of shaders https://thebookofshaders.com/12/
    uv *= numSquared;
    vec2 position_in_cell = fract(uv);
    vec2 edge_of_cell = floor(uv);

    float minimum_dist = 100.;
    for (int y=-1;y<=1;y++) {
        for (int x=-1;x<=1;x++) {
            vec2 point = random2(vec2(x, y) + edge_of_cell);
            point = 0.5 + 0.5*random2(vec2(GameTime2) * 0.1 + 6.28*point); // don't get this line
            minimum_dist = min(minimum_dist, distance(point + vec2(x,y), position_in_cell));
        }
    }
    return minimum_dist;
}

float luminance(vec3 color) {
    return dot(color, vec3(0.299, 0.587, 0.114));
}

vec3 hsv2rgb(vec3 c)
{
vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    float wor = pow(worley(texCoord0, 1.0), 2.2);
//    vec2 r = texture(Sampler0, texCoord0+offset).ra; // get trolled
//    float g = texture(Sampler0, texCoord0+2*offset).g;
//    float b = texture(Sampler0, texCoord0+3*offset).b;
//    vec4 color = vec4(r.r, g, b, r.g);
        //* vec3(52/255., 140/255., 235./255.)
    vec4 sampled = texture(Sampler0, texCoord0).rgba;
    vec3 patchColor = hsv2rgb(vec3(sin(0.15 * GameTime2), 1.0, luminance(sampled.rgb)));
    vec4 color = vec4(patchColor * wor + sampled.rgb, sampled.a);

    if (color.a < 0.1) {
        discard;
    }
    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
