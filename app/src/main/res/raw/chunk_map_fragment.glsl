
#ifdef USE_MAP

	vec2 aUv = vUv;
	#ifdef FLIP_Y
		aUv = vec2(vUv.x, 1.0 - vUv.y);
	#endif

	vec4 texelColor = texture2D( map, aUv );

	texelColor = mapTexelToLinear( texelColor );
	diffuseColor *= texelColor;

#endif

