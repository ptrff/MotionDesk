package ru.ptrff.motiondesk;

import static java.lang.Math.PI;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;
import com.crashinvaders.vfx.gl.VfxGLUtils;

public class GlitchEffect extends ShaderVfxEffect implements ChainVfxEffect {
    private static final String U_TEXTURE = "u_texture";
    private static final String U_TIME = "u_time";

    private float time = 0f;

    public GlitchEffect() {
        super(VfxGLUtils.compileShader(Gdx.files.classpath("gdxvfx/shaders/screenspace.vert"), Gdx.files.internal("shaders/test.frag")));
        rebind();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if(time*1.4>=2*PI) setTime(0);
        else setTime(time + delta);
    }

    public float getTime() {
        return time;
    }

    public void setTime(float newTime) {
        time = newTime;
        setUniform(U_TIME, time);
    }

    @Override
    public void rebind() {
        super.rebind();
        program.bind();
        program.setUniformi(U_TEXTURE, TEXTURE_HANDLE0);
        if(time>=5) time=0;
        program.setUniformf(U_TIME, time);
    }

    @Override
    public void render(VfxRenderContext context, VfxPingPongWrapper buffers) {
        render(context, buffers.getSrcBuffer(), buffers.getDstBuffer());
    }

    public void render(VfxRenderContext context, VfxFrameBuffer src, VfxFrameBuffer dst) {
        src.getTexture().bind(TEXTURE_HANDLE0);
        renderShader(context, dst);
    }
}
