package ru.ptrff.motiondesk.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import ru.ptrff.motiondesk.engine.effects.GlitchEffect;
import ru.ptrff.motiondesk.engine.effects.ParallaxEffect;
import ru.ptrff.motiondesk.engine.effects.ShakeEffect;
import ru.ptrff.motiondesk.engine.scene.ActorHandler;
import ru.ptrff.motiondesk.engine.effects.BaseEffect;
import ru.ptrff.motiondesk.engine.effects.WindEffect;
import ru.ptrff.motiondesk.engine.scene.ImageActor;
import ru.ptrff.motiondesk.models.ParameterField;

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
            //actorElement.add("masked", toElement(actorHandler.haveMask()));
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
            actorElement.add("type_name", toElement(effect.getClass().getSimpleName()));
            JsonArray parameters = new JsonArray();
            for(ParameterField field:effect.getParameters())
                parameters.add(toElement(field));
            actorElement.add("parameters", parameters);
            array.add(actorElement);
        }
        return array;
    }

    private static JsonElement toElement(Object a){
        return gson.toJsonTree(a);
    }

    public static Array<ActorHandler> JsonArrayToActors(JsonArray jsonArray, Context context){
        Array<ActorHandler> array = new Array<>();
        for(JsonElement element:jsonArray){
            JsonObject object = element.getAsJsonObject();

            String name = object.get("name").getAsString();

            Bitmap bitmap = ProjectManager.getBitmapFromCurrentByName(context, name+".png");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            Pixmap pixmap = new Pixmap(byteArray, 0, byteArray.length);

            Texture texture = new Texture(pixmap);

            ImageActor imageActor = new ImageActor(texture, name);


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
            System.out.println(name+"  loaded");

            //if(object.get("masked").getAsBoolean())

            if(!object.get("effects").getAsJsonArray().isEmpty()){
                actor.addEffects(JsonArrayToEffects(object.get("effects").getAsJsonArray()));
            }
            array.add(actor);
        }
        return array;
    }

    private static List<BaseEffect> JsonArrayToEffects(JsonArray jsonArray){
        List<BaseEffect> array = new ArrayList<>();
        for(JsonElement element:jsonArray) {
            BaseEffect effect = null;
            JsonObject object = element.getAsJsonObject();

            String name = object.get("name").getAsString();

            String typeName = object.get("type_name").getAsString();

            JsonArray parameters = object.get("parameters").getAsJsonArray();
            List<ParameterField> parametersList = new ArrayList<>();
            for(JsonElement parameter:parameters){
                parametersList.add(gson.fromJson(parameter, ParameterField.class));
            }

            if(typeName.equals("ParallaxEffect")){
                effect = new ParallaxEffect(name, parametersList);
            }

            if(typeName.equals("GlitchEffect")){
                effect = new GlitchEffect(name, parametersList);
            }

            if(typeName.equals("ShakeEffect")){
                effect = new ShakeEffect(name, parametersList);
            }

            if(typeName.equals("WindEffect")){
                effect = new WindEffect(name, parametersList);
            }

            if(effect!=null){
                array.add(effect);
            }
        }
        return array;
    }

}
