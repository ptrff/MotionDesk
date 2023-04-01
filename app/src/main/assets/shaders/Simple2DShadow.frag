#ifdef GL_ES
#define PRECISION mediump
precision PRECISION float;
precision PRECISION int;
#else
#define PRECISION
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_time;
uniform float u_zoomAmount;

const vec2 u_offset = vec2(0.5, 0.5);

void main() {
    // Get the alpha value of the texture
    float alpha = texture2D(u_texture, v_texCoords).a;

    // Calculate the distance from the center of the texture
    vec2 center = vec2(5.0, 5.0);
    float distance = length(v_texCoords - center);

    // Calculate the shadow value based on the distance and alpha
    float shadow = smoothstep(0.5, 0.55, distance) * alpha;

    // Output the final color with the shadow added
    vec4 texColor = texture2D(u_texture, v_texCoords);
    gl_FragColor = vec4(texColor.rgb, texColor.a + shadow);
}