package ru.ptrff.motiondesk.engine;

import static java.lang.Math.PI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;
import com.crashinvaders.vfx.gl.VfxGLUtils;

public class ShakeEffect extends BaseEffect {
    private String name;
    private Texture mask;
    private boolean maskEnabled = false;
    private static final String U_TEXTURE = "u_texture";
    private static final String U_MASK = "u_mask";
    private static final String U_ROTATION = "u_rotation";
    private static final String U_TIME = "u_time";

    private float time = 0f;

    public ShakeEffect(String name) {
        super(VfxGLUtils.compileShader(Gdx.files.classpath("gdxvfx/shaders/screenspace.vert"), Gdx.files.internal("shaders/ShakeShader.frag")));
        this.name = name;
        rebind();
    }

    public ShakeEffect(String name, Texture mask) {
        super(VfxGLUtils.compileShader(Gdx.files.classpath("gdxvfx/shaders/screenspace.vert"), Gdx.files.internal("shaders/ShakeShader.frag")));
        this.name = name;
        this.mask = mask;
        maskEnabled = true;
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
        program.setUniformi(U_MASK, TEXTURE_HANDLE1);
        if(time>=5) time=0;
        program.setUniformf(U_TIME, time);
        program.setUniformf(U_ROTATION, 1);
        program.setUniformf("u_amount", 1);
    }

    @Override
    public void render(VfxRenderContext context, VfxPingPongWrapper buffers) {
        render(context, buffers.getSrcBuffer(), buffers.getDstBuffer());
    }

    public void render(VfxRenderContext context, VfxFrameBuffer src, VfxFrameBuffer dst) {
        src.getTexture().bind(TEXTURE_HANDLE0);
        if(mask!=null && maskEnabled) mask.bind(TEXTURE_HANDLE1);
        renderShader(context, dst);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }


}
