THREE.LensflareShader = {

	uniforms: {

		"tDiffuse": { type: "t", value: null },
		"pos":   { type: "v2", value: new THREE.Vector2(0.25,0.5) },
		"res":   { type: "v2", value: new THREE.Vector2(512,512) },
		"alpha":   { type: "f", value: 1.0 },

	},

	vertexShader: [

		"varying vec2 vUv;",

		"void main() {",

			"vUv = uv;",
			"gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );",

		"}"

	].join("\n"),

	fragmentShader: [

		"uniform sampler2D tDiffuse;",
		"uniform vec2 pos;",
		"uniform vec2 res;",
		"uniform float alpha;",

		"varying vec2 vUv;",

		"float noise(float t) {",
			"return texture2D(tDiffuse,vec2(t,.0)/res.xy).x;",
		"}",

		"float noise2(vec2 t) {",
			"return texture2D(tDiffuse,t/res.xy).x;",
		"}",

		"vec3 lensflare(vec2 uv,vec2 pos) {",
			"vec2 main = uv-pos;",
			"vec2 uvd = uv*(length(uv));",
			
			"float ang = atan(main.x,main.y);",
			"float dist=length(main); dist = pow(dist,.1);",
			"float n = noise2(vec2(ang*16.0,dist*32.0));",
			
			"float f0 = 1.0/(length(uv-pos)*16.0+1.0);",
			
			"f0 = f0+f0*(sin(noise((pos.x+pos.y)*2.2+ang*4.0+5.954)*16.0)*.1+dist*.1+.8)*0.1;",
			
			"float f1 = max(0.01-pow(length(uv+1.2*pos),1.9),.0)*7.0;",

			"float f2 = max(1.0/(1.0+32.0*pow(length(uvd+0.8*pos),2.0)),.0)*00.25;",
			"float f22 = max(1.0/(1.0+32.0*pow(length(uvd+0.85*pos),2.0)),.0)*00.23;",
			"float f23 = max(1.0/(1.0+32.0*pow(length(uvd+0.9*pos),2.0)),.0)*00.21;",
			
			"vec2 uvx = mix(uv,uvd,-0.5);",
			
			"float f4 = max(0.01-pow(length(uvx+0.4*pos),2.4),.0)*6.0;",
			"float f42 = max(0.01-pow(length(uvx+0.45*pos),2.4),.0)*5.0;",
			"float f43 = max(0.01-pow(length(uvx+0.5*pos),2.4),.0)*3.0;",
			
			"uvx = mix(uv,uvd,-.4);",
			
			"float f5 = max(0.01-pow(length(uvx+0.2*pos),5.5),.0)*2.0;",
			"float f52 = max(0.01-pow(length(uvx+0.4*pos),5.5),.0)*2.0;",
			"float f53 = max(0.01-pow(length(uvx+0.6*pos),5.5),.0)*2.0;",
			
			"uvx = mix(uv,uvd,-0.5);",
			
			"float f6 = max(0.01-pow(length(uvx-0.3*pos),1.6),.0)*6.0;",
			"float f62 = max(0.01-pow(length(uvx-0.325*pos),1.6),.0)*3.0;",
			"float f63 = max(0.01-pow(length(uvx-0.35*pos),1.6),.0)*5.0;",
			
			"vec3 c = vec3(.0);",
			
			"c.r+=f2+f4+f5+f6; c.g+=f22+f42+f52+f62; c.b+=f23+f43+f53+f63;",
			"c = c*2.5 - vec3(length(uvd)*.05);",
			//"c+=vec3(f0*0.5);",
			
			"return c;",
		"}",

		"void main() {",

			"vec4 texel = texture2D( tDiffuse, vUv );",
			"gl_FragColor = texel;",
			"vec2 uvv = gl_FragCoord.xy / res.xy - 0.5;",
			"uvv.x *= res.x/res.y;", // aspect ratio
			"gl_FragColor.xyz += lensflare(uvv, vec2(pos.x, pos.y*-1.0))*alpha;",


		"}"

	].join("\n")

};
