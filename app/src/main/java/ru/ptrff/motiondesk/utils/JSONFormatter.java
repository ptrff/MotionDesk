package ru.ptrff.motiondesk.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import ru.ptrff.motiondesk.engine.ActorHandler;
import ru.ptrff.motiondesk.engine.BaseEffect;
import ru.ptrff.motiondesk.engine.GlitchEffect;
import ru.ptrff.motiondesk.engine.ImageActor;

public class JSONFormatter {
    static Gson gson = new Gson();

    public static JsonArray actorsToJsonArray(Array<ActorHandler> actors){
        JsonArray array = new JsonArray();
        for (int i = 0; i<actors.size;i++) {
            ActorHandler actorHandler = actors.get(i);
            JsonObject actorElement = new JsonObject();
            actorElement.add("name", toElement(actorHandler.getName()));
            actorElement.add("x", toElement(actorHandler.getActorX()));
            actorElement.add("y", toElement(actorHandler.getActorY()));
            actorElement.add("rotation", toElement(actorHandler.getActorRotation()));
            actorElement.add("width", toElement(actorHandler.getActorWidth()));
            actorElement.add("height", toElement(actorHandler.getActorHeight()));
            actorElement.add("visibility", toElement(actorHandler.getVisibility()));
            actorElement.add("locked", toElement(actorHandler.getLockStatus()));
            actorElement.add("masked", toElement(actorHandler.haveMask()));
//            if(actorHandler.haveMask()){
//                //addMask
//            }
            actorElement.add("effects", effectsToJsonArray(actorHandler.getEffects()));
            array.add(actorElement);
        }
        return array;
    }

    private static JsonElement effectsToJsonArray(List<BaseEffect> effects){
        JsonArray array = new JsonArray();
        for (BaseEffect effect:effects){
            JsonObject actorElement = new JsonObject();
            actorElement.add("name", toElement(effect.getName()));
            actorElement.add("type", toElement(effect.getType()));
            if(effect.getType().equals("GlitchEffect")){
                GlitchEffect glitchEffect = (GlitchEffect) effect;
                actorElement.add("specificVars", toElement("specVars"));
            }
            array.add(actorElement);
        }
        return array;
    }

    private static JsonElement toElement(Object a){
        return gson.toJsonTree(a);
    }

    public static Array<ActorHandler> JsonArrayToActors(JsonArray jsonArray, List<Texture> textures){
        Array<ActorHandler> array = new Array<>();
        int position = 0;
        for(JsonElement element:jsonArray){
            JsonObject object = element.getAsJsonObject();
            Texture texture = textures.get(position);
            ImageActor imageActor = new ImageActor(texture, object.get("name").getAsString());
            ActorHandler actor = new ActorHandler(imageActor);
            actor.setActorPosition(
                    object.get("x").getAsFloat(),
                    object.get("y").getAsFloat()
            );
            actor.setActorRotation(
                    object.get("rotation").getAsFloat()
            );
            actor.setActorSize(
                    object.get("width").getAsFloat(),
                    object.get("height").getAsFloat()
            );
            actor.setVisibility(
                    object.get("visibility").getAsBoolean()
            );
            actor.setLockStatus(
                    object.get("locked").getAsBoolean()
            );
//            if(object.get("masked").getAsBoolean())
                //SetMask
            if(!object.get("effects").getAsJsonArray().isEmpty()){
                actor.addEffects(JsonArrayToEffects(object.get("effects").getAsJsonArray()));
            }
            array.add(actor);
            position++;
        }
        return array;
    }

    private static List<BaseEffect> JsonArrayToEffects(JsonArray jsonArray){
        List<BaseEffect> array = new ArrayList<>();
        for(JsonElement element:jsonArray) {
            JsonObject object = element.getAsJsonObject();
            if(object.get("type").getAsString().equals("GlitchEffect")){
                GlitchEffect effect = new GlitchEffect(object.get("name").getAsString());
                array.add(effect);
            }
        }
        return array;
    }

}
