package ru.ptrff.motiondesk.engine;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;

public abstract class BaseEffect extends ShaderVfxEffect implements ChainVfxEffect {

    protected String name;
    private final String type;

    public BaseEffect(ShaderProgram program, String type) {
        super(program);
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
