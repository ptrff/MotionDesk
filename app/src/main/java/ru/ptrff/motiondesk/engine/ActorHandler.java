package ru.ptrff.motiondesk.engine;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.crashinvaders.vfx.scene2d.VfxWidgetGroup;

import java.util.ArrayList;
import java.util.List;

public class ActorHandler extends VfxWidgetGroup {

    private final List<BaseEffect> effects;
    private boolean mask = false;
    private boolean lockStatus = false;
    private boolean visibility = true;

    public ActorHandler(Actor actor) {
        super(Pixmap.Format.RGBA8888);
        effects = new ArrayList<>();
        setFillParent(false);
        addActor(actor);
    }

    public void addEffect(BaseEffect effect){
        effects.add(effect);
        getVfxManager().addEffect(effect, effects.size());
    }

    public void addEffects(List<BaseEffect> effects){
        for(BaseEffect effect:effects)
            addEffect(effect);
    }

    public List<BaseEffect> getEffects() {
        return effects;
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
        if (mask) getMask().setSize(width, height);
    }

    public void setActorPosition(float x, float y) {
        getImageActor().setPosition(x, y);
        if(mask) getMask().setPosition(x, y);
    }

    public void setActorZoomAmount(float zoomAmount) {
        getImageActor().setZoomAmount(zoomAmount);
    }

    public void setActorWidth(float width) {
        getImageActor().setWidth(width);
    }

    public void setActorHeight(float height) {
        getImageActor().setHeight(height);
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

    public void addMask(){
        if(!mask) addActor(new MaskActor((int) getActorWidth(), (int) getActorHeight(), (int) getActorX(), (int) getActorY()));

        mask = true;
    }

    public float getZoomAmount(){
        return getImageActor().getZoomAmount();
    }

    public void removeMask(){
        if(mask) removeActor(getChild(1));
        mask = false;
    }

    public MaskActor getMask(){
        if(mask){
            return (MaskActor) getChild(1);
        }else {
            return null;
        }
    }

    public boolean haveMask(){
        return mask;
    }

    public boolean getLockStatus(){
        return lockStatus;
    }

    public void drawMask(float x, float y, float deltaX, float deltaY) {
        if(getMask()!=null)
            getMask().pan(x, y, deltaX, deltaY);
    }

    public void setActorRotation(float rotation){
        getImageActor().setRotation(rotation);
    }

    public float getActorRotation() {
        return getImageActor().getRotation();
    }
}
