package ru.ptrff.motiondesk.engine.scene;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import ru.ptrff.motiondesk.engine.scene.ActorHandler;

public class StageWrapper extends Stage {

    public StageWrapper(ScreenViewport screenViewport, Batch batch) {
        super(screenViewport, batch);
    }

    public int indexOf(ActorHandler actor){
        return getObjects().indexOf(actor, true);
    }

    public ActorHandler get(int id){
        return getObjects().get(id);
    }

    public int size(){
        return getActors().size;
    }

    public void add(ActorHandler actor){
        addActor(actor);
    }


    public Array<ActorHandler> getObjects(){
        return (Array<ActorHandler>) (Array<?>) getActors();
    }


}
