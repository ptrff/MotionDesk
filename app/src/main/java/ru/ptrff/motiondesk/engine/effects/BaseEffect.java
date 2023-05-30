package ru.ptrff.motiondesk.engine.effects;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;
import java.util.ArrayList;
import java.util.List;
import ru.ptrff.motiondesk.models.ParameterField;

public abstract class BaseEffect extends ShaderVfxEffect implements ChainVfxEffect {

    private String name;
    private final List<ParameterField> parameters;

    public BaseEffect(ShaderProgram program) {
        super(program);
        parameters = new ArrayList<>();
    }

    public void onParameterChanged(ParameterField field){
    }
    public void addParameter(ParameterField parameter){
        parameters.add(parameter);
    }

    public List<ParameterField> getParameters(){
        return parameters;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
