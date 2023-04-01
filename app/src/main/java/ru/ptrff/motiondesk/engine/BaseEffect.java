package ru.ptrff.motiondesk.engine;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;

public abstract class BaseEffect extends ShaderVfxEffect implements ChainVfxEffect {

    protected String name;

    public BaseEffect(ShaderProgram program) {
        super(program);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
