/**
 * @author @oosmoxiecode
 *
 * Based on iq's "oldschool radialblur": https://www.shadertoy.com/view/4sfGRn
 */

THREE.RadialBlurShader = {

	uniforms: {

		"tDiffuse": { type: "t", value: null },
		"tDepth":   { type: "t", value: null },
		"steps":    { type: "f", value: 60.0 },
		"strength": { type: "f", value: 0.999 },
		"expo": 	{ type: "f", value: 1.1 },
		"center":   { type: "v2", value: new THREE.Vector2( 0.5, 0.5 ) },

	},

	vertexShader: [

		"varying vec2 vUv;",

		"void main() {",

			"vUv = uv;",
			"gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );",

		"}"

	].join("\n"),

	fragmentShader: [

		"uniform float steps;",
		"uniform float strength;",
		"uniform float expo;",
		"uniform sampler2D tDiffuse;",
		"uniform sampler2D tDepth;",
		"uniform vec2 center;",

		"varying vec2 vUv;",

		"void main() {",

		    "vec2 s = vUv;",

		    "vec3 total = vec3(0.0);",
		    "vec2 d = (center-vUv)/steps;",
		    "float w = 1.0;",

		    //"for( int i=0; i<int(steps); i++ ) {",
		    "for( int i=0; i<60; i++ ) {", // hardcode since the above fails in angle...
		        "vec3 res = texture2D( tDepth, s).xyz;",
		        "res = smoothstep(0.0,1.0,res);",
		        "total += w*res;",
		        "w *= strength;",
		        "s += d;",
		    "}",
		    "total /= steps;",

			//"gl_FragColor = vec4( total*expo, 1.0);",
			"vec3 dif = texture2D( tDiffuse, vUv).xyz;",
			"gl_FragColor = vec4( mix(total*expo, dif*2.0, 0.5), 1.0);",

		"}"

	].join("\n")

};
