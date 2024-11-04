#version 400


uniform sampler2D DiffuseSampler;
uniform vec4 ColorModulate;


in vec2 texCoord;
out vec4 fragColor;


void main() {
    fragColor =  0.4 * vec4(1,0,0,1) + texture(DiffuseSampler, texCoord) * ColorModulate;
}