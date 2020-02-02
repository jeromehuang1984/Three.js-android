precision mediump float;
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform mat3 normalMatrix;
attribute vec3 normal;
attribute vec4 vPosition;
varying vec3 vNormal;

void main() {
	vec4 mvPosition = modelViewMatrix * vec4( vPosition);
	gl_Position = projectionMatrix * mvPosition;

	vec3 objectNormal = vec3( normal );
	vec3 transformedNormal = normalMatrix * objectNormal;
	vNormal = normalize( transformedNormal );
}