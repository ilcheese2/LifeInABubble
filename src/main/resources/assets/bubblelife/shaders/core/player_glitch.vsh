#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float GameTime2;
uniform int FogShape;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out float vertexDistance;
out vec4 vertexColor;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;

vec3 random3(vec3 c) {
    //https://gist.github.com/patriciogonzalezvivo/670c22f3966e662d2f83
    float j = 4096.0*sin(dot(c,vec3(17.0, 59.4, 15.0)));
    vec3 r;
    r.z = fract(512.0*j);
    j *= .125;
    r.x = fract(512.0*j);
    j *= .125;
    r.y = fract(512.0*j);
    return r-0.5;
}


float worley(vec3 uv, float numSquared) {
    // thanks you book of shaders https://thebookofshaders.com/12/
    uv *= numSquared;
    vec3 position_in_cell = fract(uv);
    vec3 edge_of_cell = floor(uv);

    float minimum_dist = 100.;
    for (int z=-1;z<=1;z++) {
        for (int y = -1;y <= 1; y++) {
            for (int x = -1;x <= 1; x++) {
                vec3 point = random3(vec3(x, y, z) + edge_of_cell);
                point = 0.5 + 0.5 * sin(GameTime2 * 0.4 + 6.28 * point); // don't get this line
                minimum_dist = min(minimum_dist, distance(point + vec3(x, y, z), position_in_cell));
            }
        }
    }
    return minimum_dist;
}

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position + 0.2 * vec3(worley(Position, 3.0)), 1.0);

    vertexDistance = fog_distance(Position, FogShape);
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);
    lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);
    overlayColor = texelFetch(Sampler1, UV1, 0);
    texCoord0 = UV0;
}
