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

public class ShakeEffect extends BaseEffect {
    private String name;
    private static final String U_TEXTURE = "u_texture";
    private static final String U_ROTATION = "u_rotation";
    private static final String U_AMOUNT = "u_amount";
    private static final String U_SPEED = "u_speed";
    private static final String U_TIME = "u_time";

    private float rotation = 0f;
    private float speed = 1f;
    private float amount = 1f;
    private float time = 0f;

    public ShakeEffect(Resources resources) {
        super(
                VfxGLUtils.compileShader(
                        Gdx.files.classpath("gdxvfx/shaders/screenspace.vert"),
                        Gdx.files.internal("shaders/ShakeShader.frag")
                )
        );
        this.name = resources.getString(R.string.shake);
        addParameters(resources);
        rebind();
    }

    public ShakeEffect(String name, List<ParameterField> parameters) {
        super(
                VfxGLUtils.compileShader(
                        Gdx.files.classpath("gdxvfx/shaders/screenspace.vert"),
                        Gdx.files.internal("shaders/ShakeShader.frag")
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

        ParameterField rotation = new ParameterField();
        rotation.setName(resources.getString(R.string.rotation));
        rotation.setValue(this.rotation);
        rotation.setType("float");
        rotation.setTypeName("rotation");
        rotation.setMax(360);
        rotation.setMin(0);
        rotation.setFieldType("text");
        addParameter(rotation);

        ParameterField speed = new ParameterField();
        speed.setName(resources.getString(R.string.speed));
        speed.setValue(this.speed);
        speed.setType("float");
        speed.setMax(1000);
        speed.setMin(0);
        speed.setTypeName("speed");
        speed.setFieldType("text");
        addParameter(speed);

        ParameterField amount = new ParameterField();
        amount.setName(resources.getString(R.string.amount));
        amount.setValue(this.amount);
        amount.setType("float");
        amount.setMax(10);
        amount.setMin(0);
        amount.setTypeName("amount");
        amount.setFieldType("text");
        addParameter(amount);
    }

    @Override
    public void onParameterChanged(ParameterField field) {
        if(field.getTypeName().equals("name")) {
            name = field.getValue().toString();
            return;
        }

        if(field.getTypeName().equals("rotation"))
            rotation = Float.parseFloat(field.getValue().toString());

        if(field.getTypeName().equals("speed"))
            speed = Float.parseFloat(field.getValue().toString());

        if(field.getTypeName().equals("amount"))
            amount = Float.parseFloat(field.getValue().toString());

    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if(time*1.4*speed>=10*PI) setTime(0);
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
        setUniform(U_TEXTURE, TEXTURE_HANDLE0);
        if(time>=5) time=0;
        setUniform(U_TIME, time);
        setUniform(U_ROTATION, rotation);
        setUniform(U_SPEED, speed*0.1f);
        setUniform(U_AMOUNT, amount*0.1f);
    }

    @Override
    public void render(VfxRenderContext context, VfxPingPongWrapper buffers) {
        render(context, buffers.getSrcBuffer(), buffers.getDstBuffer());
    }

    public void render(VfxRenderContext context, VfxFrameBuffer src, VfxFrameBuffer dst) {
        rebind();
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
