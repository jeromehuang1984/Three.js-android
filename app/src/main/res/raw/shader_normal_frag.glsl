precision mediump float;

vec3 packNormalToRGB( const in vec3 normal ) {
	return normalize( normal ) * 0.5 + 0.5;
}

varying vec3 vNormal;

void main() {
	gl_FragColor = vec4( packNormalToRGB( vNormal ), 1.0 );
}