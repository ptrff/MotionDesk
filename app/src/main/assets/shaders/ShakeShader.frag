#ifdef GL_ES
#define PRECISION mediump
precision PRECISION float;
#else
#define PRECISION
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_rotation;
uniform float u_speed;
uniform float u_amount;
uniform float u_time;

void main()
{
	vec2 uv = v_texCoords;

	// Convert rotation angle to radians
	float rad = radians(u_rotation);

	// Calculate sin and cos of the angle
	float sinAngle = sin(rad);
	float cosAngle = cos(rad);

	// Calculate the amount of shaking based on time and the u_amount uniform
	float shaking = u_amount * sin(u_speed * u_time * 100.0);

	// Calculate the UV coordinates for the shaken texture
	vec2 shakenUV = vec2(uv.x + shaking * cosAngle, uv.y + shaking * sinAngle);

	// Get the texture color at the modified UV coordinates and set it as the output color
	gl_FragColor = texture2D(u_texture, shakenUV);
}
