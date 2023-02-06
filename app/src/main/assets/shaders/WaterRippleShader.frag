#ifdef GL_ES
	#define PRECISION mediump
	precision PRECISION float;
	precision PRECISION int;
#else
	#define PRECISION
#endif

uniform vec3      iResolution;           // viewport resolution (in pixels)
uniform float     iTime;                 // shader playback time (in seconds)
uniform float     iTimeDelta;            // render time (in seconds)
uniform float     iFrameRate;            // shader frame rate
uniform int       iFrame;                // shader playback frame
uniform float     iChannelTime[4];       // channel playback time (in seconds)
uniform vec3      iChannelResolution[4]; // channel resolution (in pixels)
uniform vec4      iMouse;                // mouse pixel coords. xy: current (if MLB down), zw: click
uniform samplerXX iChannel0..3;          // input channel. XX = 2D/Cube
uniform vec4      iDate;                 // (year, month, day, time in seconds)

vec4    A =      vec4(0,11,33,0);
#define R(x)     mat2(cos(x+A))                                // rotation
#define Q(v)     v * v
#define T(c,p) ( Q( texture(c, p.xy) ) + Q( texture(c, p.xz) ) +Q ( texture(c, p.yz) ) ) / 3.
#define W        length

void mainImage( out vec4 O, vec2 u ) {
	vec3  R = iResolution,
          r = vec3( u+u, 0 ) - R,    o,p,P,s,a,q;
    r.z = R.y - dot(r,r)/3. /R.y;
    r /= W(r);
    float h = .5, l = 0.,L,d,f, i=l, j=i, n=9., m,w,I=1., // Windows want init
          t = o.z  = iTime * 4.;
    o.xy =  - sin(t * h + A.xz );                              // origin o, direction r
    r.xy *= R(sin(t * h/2.) * 1.57);
    r.xz *= R(-o.x * .78);
    r.yz *= R(-o.y * .78);

    for ( ; j++ < 32.; ) {                                     // --- trace
        P = p = o + .6*r*l;
        P.xy += sin(P.z*h +A.xz );                             // map - sdf to scene
        m = 1e3;
        s = 1./vec3(2,8,8);
        q = a = vec3( mod( atan(P.x,P.y)/.39 + P.z/2. , 4.) - 2.,
                      W(P.xy) - 3.,
                      fract(P.z) - .5
                    );
        for ( int k=0 ; k++ < 4; s = s.yzx * .75 ) {
            q = abs(q) - .3,
            q.xy *= R(.4), // pi/8
            P = abs(q) - s,
            w = min(max(P.x,max(P.y,P.z)),0.) + W(max(P,0.));  // sdBox
         // w < m ?  m = w, I = k < 4 ? 4. : 1. : I;           // Windows get it wrong
         // w < m ?  I = k < 4 ? 4. : 1. , m = w: I;           // differently wrong
            if ( w < m )  m = w, I = k < 4 ? 4. : 1.;
          }

        l += d = max(m, -1.-a.y);
      }

    for ( O*=0.; ++i < n; ) {
        for (j = L = 0.; j++ < 5.;  L += 5. - W(P) )           // --- trace2
            P = o + r * L,
            P.z -= floor( f = o.z + 4.*i/n );
        P.xz *= R( 6.28 * i/n );
        f = fract(f);
        O += T(iChannel1, .5*P) * f*(1.-f); // fract(f)*fract(-f); // bug on Windows
    }
    l = 1. + l * l * .0036;  // fog
    O += ( T(iChannel0, p) / I / ( l + l* d * 1e2 ) - O ) /l;
	O = sqrt( dot(O, vec4(.6,1.2,.2,0)) +O.zyxw /vec4(1,2,.67,1) );
}