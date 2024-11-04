#version 400

#define MAX_BUBBLES 20

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform sampler1D DataSampler;

uniform vec4 ColorModulate;

uniform mat4 ProjInvMat;
uniform mat4 ModelViewInvMat;
uniform vec3 CamPos;

uniform vec4 Bubbles[MAX_BUBBLES];
uniform int BubbleOffsets[MAX_BUBBLES];
uniform int BubbleCount;

in vec2 texCoord;

out vec4 fragColor;

float sphere(vec3 p, float s) {
    return length(p) - s;
}

vec3 projectAndDivide(mat4 projectionMatrix, vec3 coord) { // https://shaderlabs.org/mw/images/5/5a/Space_conversion_cheat_sheet.png
                                                           vec4 homogenousCoord = projectionMatrix * vec4(coord.xyz, 1.0);
                                                           return homogenousCoord.xyz / homogenousCoord.w;
}

vec3 toWorldSpace(vec3 screenPos) {
    vec3 ndcPos = screenPos * 2.0 - 1.0;
    vec3 viewPos = projectAndDivide(ProjInvMat, ndcPos);
    vec3 playerPos = mat3(ModelViewInvMat) * viewPos;
    return playerPos + CamPos;
}

float sphereIntersection(vec3 origin, vec3 direction, vec3 sphere, float radius, float previousMin, out vec3 normal) {
    vec3 oc = sphere - origin; // https://raytracing.github.io/ my beloved
    float a = dot(direction, direction);
    float h = dot(direction, oc);
    float c = dot(oc, oc) - radius * radius;

    float discriminant = h * h - a * c;
    if (discriminant < 0) return 0.;

    float sqrtd = sqrt(discriminant);

    float root = (h - sqrtd) / a;
    float mult = 1;
    if (c < 0) {//(0.001 > root && root > previousMin) {
                root = (h + sqrtd) / a;
                mult = 1;
                //        if (0.001 > root && root > previousMin) {
                //            return 0.;
                //        }
    }


    vec3 outward_normal = (origin + root * direction - sphere) / radius;
    normal = outward_normal * mult;
    return root;
}

vec4 sphereTexture(vec3 normal, int bubbleOffset, int color);


void main() {

    float depth = texture(DepthSampler, texCoord).r;
    vec3 worldPos = toWorldSpace(vec3(texCoord, depth));

    vec3 sphereCenter = vec3(-67, 65, 261);
    vec3 sphereCenter2 = vec3(-62, 75, 253);

    vec3[] sphereCenters = vec3[](sphereCenter, sphereCenter2);
    vec4[] sphereColors = vec4[](vec4(1, 0, 0, 1), vec4(0, 1, 0, 1));

    float terrainDistance = distance(CamPos, worldPos);

    vec4 a = vec4(0);
    float minDist = 10000.0;
    fragColor = a;

    for (int i = 0; i < BubbleCount; i++) {
        vec3 normal;
        vec4 bubble = texelFetch(DataSampler, BubbleOffsets[i], 0);
        float dist = sphereIntersection(CamPos, normalize(worldPos - CamPos), bubble.xyz, bubble.w, minDist, normal);

        if (dist < minDist && dist > 0 && dist < terrainDistance) {
            minDist = dist;
            vec4 color = texelFetch(DataSampler, BubbleOffsets[i] + 1, 0);
            a = sphereTexture(normal, BubbleOffsets[i], int(color.x)); // waste three bytes smh
        }
    }



    fragColor =  0.4 * a + texture(DiffuseSampler, texCoord) * ColorModulate;
}