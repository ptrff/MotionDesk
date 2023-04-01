#ifdef GL_ES
#define PRECISION mediump
precision PRECISION float;
precision PRECISION int;
#else
#define PRECISION
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_mask;
uniform int u_rotation;
uniform float u_amount;
uniform float u_time;


void main()
{
	vec2 uv = v_texCoords;

	// Translate to origin and apply rotation
	float angle = radians(float(u_rotation));
	mat2 rotationMatrix = mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
	uv = rotationMatrix * (uv - 0.5) + 0.5;

	// Apply linear wave-like animation
	float animationAmount = (1.0 - texture2D(u_mask, uv).r) * u_amount;
	uv.y += sin(uv.x * 10.0 + u_time * 3.0) / 10.0 * animationAmount;
	uv.x += cos(uv.y * 10.0 + u_time * 3.0) / 10.0 * animationAmount;
	uv.y -= sin(uv.x * 10.0 + u_time * 3.0) / 10.0 * animationAmount;

	// Translate back to original position and apply movement amount
	uv = (uv - 0.5) * (1.0 + u_amount) + 0.5;

	// Sample texture color
	vec4 textureColor = texture2D(u_texture, uv);

	// Apply mask as a multiplier to the texture color
	vec4 mask = texture2D(u_mask, uv);
	float grayscale = dot(mask.rgb, vec3(0.299, 0.587, 0.114));
	float maskAmount = smoothstep(0.0, 1.0, grayscale);
	vec4 maskedColor = textureColor * maskAmount;

	gl_FragColor = mix(textureColor, maskedColor, maskAmount);
}