#ifdef GL_ES
#define PRECISION mediump
precision PRECISION float;
precision PRECISION int;
#else
#define PRECISION
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_accelX;
uniform float u_accelY;
uniform float u_amountX;
uniform float u_amountY;

void main()
{
    vec2 uv = v_texCoords;

    // Calculate the amount of movement based on the accelerometer readings and parameters
    float moveX = u_accelX * u_amountX;
    float moveY = u_accelY * u_amountY;

    // Apply the movement to the texture coordinates
    uv.x += moveX;
    uv.y += moveY;

    // Sample the texture using the adjusted texture coordinates
    gl_FragColor = texture2D(u_texture, uv);
}