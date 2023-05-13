package ru.ptrff.motiondesk.engine.effects;

import static java.lang.Math.PI;

import android.content.res.Resources;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;
import com.crashinvaders.vfx.gl.VfxGLUtils;

import java.util.List;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.models.ParameterField;

public class WindEffect extends BaseEffect {
    private String name;
    private static final String U_TEXTURE = "u_texture";
    private static final String U_TIME = "u_time";

    private float time = 0f;

    public WindEffect(Resources resources) {
        super(
                VfxGLUtils.compileShader(
                        Gdx.files.classpath("gdxvfx/shaders/screenspace.vert"),
                        Gdx.files.internal("shaders/WindEffect.frag")
                )
        );
        this.name = resources.getString(R.string.windy_swings);
        addParameters(resources);
        rebind();
    }

    public WindEffect(String name, List<ParameterField> parameters) {
        super(
                VfxGLUtils.compileShader(
                        Gdx.files.classpath("gdxvfx/shaders/screenspace.vert"),
                        Gdx.files.internal("shaders/WindEffect.frag")
                )
        );
        this.name = name;
        for (ParameterField param:parameters){
            addParameter(param);
            onParameterChanged(param);
        }
        rebind();
    }

    private void addParameters(Resources resources){
        ParameterField name = new ParameterField();
        name.setName(resources.getString(R.string.name));
        name.setValue(this.name);
        name.setType("string");
        name.setTypeName("name");
        name.setFieldType("text");
        addParameter(name);
    }

    @Override
    public void onParameterChanged(ParameterField field) {
        if(field.getTypeName().equals("name"))
            name = field.getValue().toString();
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

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }


}
