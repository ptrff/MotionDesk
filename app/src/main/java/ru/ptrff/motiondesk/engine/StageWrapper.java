package ru.ptrff.motiondesk.engine;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Arrays;
import java.util.List;

public class StageWrapper extends Stage {
    private int actorsCount;

    public StageWrapper(ScreenViewport screenViewport, Batch batch) {
        super(screenViewport, batch);
    }

    public int indexOf(ActorHandler actor){
        return getObjects().indexOf(actor, true);
    }

    public ActorHandler get(int id){
        return (ActorHandler) getObjects().get(id);
    }

    public int size(){
        return getActors().size;
    }

    public void setActorPosition(int id, float x, float y){
        get(id).setActorPosition(x, y);
    }

    public void removeStroke(int id){
        get(id).removeStroke();
    }

    public void add(ActorHandler actor){
        addActor(actor);
        actorsCount++;
    }

    public float getActorY(int id){
        return get(id).getActorY();
    }

    public float getActorX(int id){
        return get(id).getActorX();
    }

    public void setActorZoomAmount(int id, float zoomAmount){
        get(id).setActorZoomAmount(zoomAmount);
    }

    public Array<ActorHandler> getObjects(){
        return (Array<ActorHandler>) (Array<?>) getActors();
    }

    public void setActorSize(int id, float width, float height){
        get(id).setActorSize(width, height);
    }

    public float getActorHeight(int id) {
        return get(id).getHeight();
    }

    public float getActorWidth(int id){
        return get(id).getActorWidth();
    }

}
