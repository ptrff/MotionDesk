package ru.ptrff.motiondesk.engine;

import com.badlogic.gdx.graphics.Pixmap;
import com.crashinvaders.vfx.scene2d.VfxWidgetGroup;

public class ActorHandler extends VfxWidgetGroup {

    private boolean lockStatus = false;
    private boolean visibility = true;

    public ActorHandler(ImageActor actor) {
        super(Pixmap.Format.RGBA8888);
        setFillParent(false);
        addActor(actor);
    }

    @Override
    public String getName() {
        return getImageActor().getName();
    }

    public float getActorX() {
        return getImageActor().getX();
    }

    public float getActorY() {
        return getImageActor().getY();
    }

    public void setActorSize(float width, float height) {
        getImageActor().setSize(width, height);
    }

    public void setActorPosition(float x, float y) {
        getImageActor().setPosition(x, y);
    }

    public void setZoomAmount(float zoomAmount) {
        getImageActor().setZoomAmount(zoomAmount);
    }

    public void setActorWidth(float width) {
        getImageActor().setWidth(width);
    }

    public float getActorWidth() {
        return getImageActor().getWidth();
    }

    public float getActorHeight() {
        return getImageActor().getHeight();
    }

    public void addStroke(){
        getImageActor().addStroke();
    }

    public void removeStroke(){
        getImageActor().removeStroke();
    }

    public ImageActor getImageActor(){
        return (ImageActor) super.getChild(0);
    }

    public int size(){
        return super.getChildren().size;
    }

    public void setVisibility(boolean visibility){
        setVisible(visibility);
        this.visibility = visibility;
    }

    public boolean getVisibility(){
        return visibility;
    }

    public void setLockStatus(boolean lockStatus){
        this.lockStatus = lockStatus;
    }

    public boolean getLockStatus(){
        return lockStatus;
    }
}
