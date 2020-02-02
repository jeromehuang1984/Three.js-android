#version 300 es
precision mediump float;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
in vec4 vPosition;

void main() {
	gl_Position = projectionMatrix * modelViewMatrix * vPosition;
}