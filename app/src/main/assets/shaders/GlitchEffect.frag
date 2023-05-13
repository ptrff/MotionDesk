#ifdef GL_ES
	#define PRECISION mediump
	precision PRECISION float;
	precision PRECISION int;
#else
	#define PRECISION
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_speed;
uniform float u_amount;
uniform float u_time;

vec3 mod289(vec3 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec2 mod289(vec2 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec3 permute(vec3 x) {
    return mod289(((x*34.0)+1.0)*x);
}

float snoise(vec2 v) {
    const vec4 C = vec4(0.211324865405187, 0.366025403784439, -0.577350269189626, 0.024390243902439);
    vec2 i = floor(v + dot(v, C.yy) );
    vec2 x0 = v - i + dot(i, C.xx);
    vec2 i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;
    i = mod289(i);
    vec3 p = permute(permute(i.y + vec3(0.0, i1.y, 1.0)) + i.x + vec3(0.0, i1.x, 1.0));
    vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
    m = m*m;
    m = m*m;
    vec3 x = 2.0 * fract(p * C.www) - 1.0;
    vec3 h = abs(x) - 0.5;
    vec3 ox = floor(x + 0.5);
    vec3 a0 = x - ox;
    m *= 1.79284291400159 - 0.85373472095314 * (a0*a0 + h*h);
    vec3 g;
    g.x = a0.x * x0.x + h.x * x0.y;
    g.yz = a0.yz * x12.xz + h.yz * x12.yw;
    return 130.0 * dot(m, g);
}

float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 uv = v_texCoords;
    float time = u_time * 2.0 * u_speed;
    float noise = max(0.0, snoise(vec2(time, uv.y * 0.3)) - 0.3) * (1.0 / 0.7) * u_amount;
    noise = noise + (snoise(vec2(time*10.0, uv.y * 2.4)) - 0.5) * 0.15 * u_amount;
    float xpos = uv.x - noise * noise * 0.25 * u_amount;
    vec4 fragColor = texture2D(u_texture, vec2(xpos, uv.y));
    fragColor.rgb = mix(fragColor.rgb, vec3(rand(vec2(uv.y * time))), noise * 0.3).rgb;
    if (floor(mod(gl_FragCoord.y * 0.25, 2.0)) == 0.0) {
        fragColor.rgb *= 1.0 - (0.15 * noise);
    }
    fragColor.g = mix(fragColor.r, texture2D(u_texture, vec2(xpos + noise * 0.05, uv.y)).g, 0.25);
    fragColor.b = mix(fragColor.r, texture2D(u_texture, vec2(xpos - noise * 0.05, uv.y)).b, 0.25);
    gl_FragColor = fragColor;
}