package ru.ptrff.motiondesk.engine.effects;

import android.content.res.Resources;

import androidx.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;
import com.crashinvaders.vfx.gl.VfxGLUtils;

import java.util.List;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.models.ParameterField;

public class ParallaxEffect extends BaseEffect {
    private String name;
    private static final String U_TEXTURE = "u_texture";
    private static final String U_ACCELX = "u_accelX";
    private static final String U_ACCELY = "u_accelY";
    private static final String U_AMOUNTX = "u_amountX";
    private static final String U_AMOUNTY = "u_amountY";

    private float initialAccelerometerX;
    private float initialAccelerometerY;
    private LowPassFilter lowPassFilter;

    private int softeningCount = 3;
    private float softeningAlpha = 0.3f;
    private float amountX = 1;
    private float amountY = 1;

    public ParallaxEffect(Resources resources) {
        super(
                VfxGLUtils.compileShader(
                        Gdx.files.classpath("gdxvfx/shaders/screenspace.vert"),
                        Gdx.files.internal("shaders/ParallaxShader.frag")
                )
        );
        this.name = resources.getString(R.string.parallax);
        addParameters(resources);
        init();
        rebind();
    }

    public ParallaxEffect(String name, List<ParameterField> parameters) {
        super(
                VfxGLUtils.compileShader(
                        Gdx.files.classpath("gdxvfx/shaders/screenspace.vert"),
                        Gdx.files.internal("shaders/ParallaxShader.frag")
                )
        );
        this.name = name;
        for (ParameterField param:parameters){
            addParameter(param);
            onParameterChanged(param);
        }
        init();
        rebind();
    }

    private void addParameters(Resources resources) {
        ParameterField name = new ParameterField();
        name.setName(resources.getString(R.string.name));
        name.setValue(this.name);
        name.setType("string");
        name.setTypeName("name");
        name.setFieldType("text");
        addParameter(name);

        ParameterField softeningCount = new ParameterField();
        softeningCount.setName(resources.getString(R.string.softening_count));
        softeningCount.setValue(this.softeningCount);
        softeningCount.setType("int");
        softeningCount.setMax(10);
        softeningCount.setMin(2);
        softeningCount.setTypeName("softeningCount");
        softeningCount.setFieldType("text");
        addParameter(softeningCount);

        ParameterField softeningAlpha = new ParameterField();
        softeningAlpha.setName(resources.getString(R.string.softening_alpha));
        softeningAlpha.setValue(this.softeningAlpha);
        softeningAlpha.setType("float");
        softeningAlpha.setTypeName("softeningAlpha");
        softeningAlpha.setMax(1);
        softeningAlpha.setMin(0.01);
        softeningAlpha.setFieldType("text");
        addParameter(softeningAlpha);

        ParameterField amountX = new ParameterField();
        amountX.setName(resources.getString(R.string.amount_x));
        amountX.setValue(this.amountX);
        amountX.setType("float");
        amountX.setTypeName("amountX");
        amountX.setMax(100);
        amountX.setMin(-100);
        amountX.setFieldType("text");
        addParameter(amountX);

        ParameterField amountY = new ParameterField();
        amountY.setName(resources.getString(R.string.amount_Y));
        amountY.setValue(this.amountY);
        amountY.setType("float");
        amountY.setTypeName("amountY");
        amountY.setMax(100);
        amountY.setMin(-100);
        amountY.setFieldType("text");
        addParameter(amountY);
    }

    @Override
    public void onParameterChanged(ParameterField field) {
        if (field.getTypeName().equals("name")) {
            name = field.getValue().toString();
            return;
        }

        if (field.getTypeName().equals("softeningCount")) {
            softeningCount = (int) Float.parseFloat(field.getValue().toString());
            init();
        }

        if (field.getTypeName().equals("softeningAlpha")) {
            softeningAlpha = Float.parseFloat(field.getValue().toString());
            init();
        }

        if (field.getTypeName().equals("amountX"))
            amountX = Float.parseFloat(field.getValue().toString());

        if (field.getTypeName().equals("amountY"))
            amountY = Float.parseFloat(field.getValue().toString());

    }

    private void init() {
        initialAccelerometerX = Gdx.input.getAccelerometerX();
        initialAccelerometerY = Gdx.input.getAccelerometerY();
        lowPassFilter = new LowPassFilter(softeningCount, softeningAlpha);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void rebind() {
        super.rebind();

        float x = Gdx.input.getAccelerometerX() - initialAccelerometerX;
        float y = Gdx.input.getAccelerometerY() - initialAccelerometerY;
        float[] values = new float[]{x, y};
        float[] filteredValues = lowPassFilter.filter(values);

        program.bind();
        program.setUniformi(U_TEXTURE, TEXTURE_HANDLE0);

        program.setUniformf(U_ACCELX, filteredValues[0]);
        program.setUniformf(U_ACCELY, filteredValues[1]);

        program.setUniformf(U_AMOUNTX, amountX * 0.01f);
        program.setUniformf(U_AMOUNTY, amountY * 0.01f);
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