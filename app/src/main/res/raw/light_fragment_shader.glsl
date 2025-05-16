#version 320 es
precision mediump float;

in vec3 Normal;

uniform vec3 objectColor;
uniform vec3 lightColor;

out vec4 FragColor;

void main() {
    float ambientStrength = 0.5;
    vec3 ambient = ambientStrength * lightColor;
    vec3 result = ambient * objectColor;
    FragColor = vec4(result, 1.0);
}
