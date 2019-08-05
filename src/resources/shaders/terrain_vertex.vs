#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec3 vertexNormal;
layout (location=2) in vec4 colour;

flat out vec3 mvVertexNormal;
out vec3 mvVertexPos;
out vec3 worldVertexPos;
out vec4 calcColour;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 worldMatrix;

void main() {
    vec4 mvPos = modelViewMatrix * vec4(position, 1);
    gl_Position = projectionMatrix * mvPos;
    mvVertexNormal = normalize(modelViewMatrix * vec4(vertexNormal, 0.0)).xyz;
    vec4 worldPos = worldMatrix * vec4(position, 1);
    mvVertexPos = mvPos.xyz;
    worldVertexPos = worldPos.xyz;
    calcColour = colour;
}