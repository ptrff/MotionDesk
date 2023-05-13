#ifdef GL_ES
	#define PRECISION mediump
	precision PRECISION float;
	precision PRECISION int;
#else
	#define PRECISION
#endif

uniform float u_time;
uniform sampler2D u_texture0;
uniform sampler2D u_texture1;
varying vec2 v_texCoord0;

void main()
{
	vec2 uv = v_texCoord0;
	vec2 warpUV = 2.0 * uv;

	float d = length(warpUV);
	vec2 st = 0.1 * warpUV + 0.2 * vec2(cos(0.071 * u_time * 2.0 + d), sin(0.073 * u_time * 2.0 - d));

	vec3 warpedCol = texture2D(u_texture0, st).rgb * 2.0;
	float w = max(warpedCol.r, 0.85);

	vec2 offset = 0.01 * cos(warpedCol.rg * 3.14159);
	vec3 col = texture2D(u_texture1, uv + offset).rgb * vec3(0.8, 0.8, 1.5);
	col *= w * 1.2;

	gl_FragColor = vec4(mix(col, texture2D(u_texture1, uv + offset).rgb, 0.5), 1.0);
}