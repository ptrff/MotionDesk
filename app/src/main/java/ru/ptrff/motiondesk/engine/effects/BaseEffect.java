package ru.ptrff.motiondesk.engine.effects;

import android.util.Pair;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ptrff.motiondesk.R;
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
