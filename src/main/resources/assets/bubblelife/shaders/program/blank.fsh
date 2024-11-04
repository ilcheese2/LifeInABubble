#version 400


uniform sampler2D DiffuseSampler;
uniform vec4 ColorModulate;
uniform vec4 Something;

in vec2 texCoord;
out vec4 fragColor;


void main() {
    fragColor =  texture(DiffuseSampler, texCoord) * ColorModulate;
}