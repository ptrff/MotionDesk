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

void main()
{
	vec2 uv = v_texCoords;

	//uv.y = -1.0 - uv.y;

    uv.x += cos(uv.y*5.2+u_time*1.4)/100.0;
    uv.y += sin(uv.x*5.1+u_time*1.4)/100.0;
	uv.x -= cos(uv.y*5.2+u_time*1.4)/100.0;
	uv.x -= cos(uv.x*5.2+u_time*1.4)/100.0;

    gl_FragColor = texture2D(u_texture, uv);
}
