package ru.ptrff.motiondesk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

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
import ru.ptrff.motiondesk.models.SceneParameters;

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
            actorElement.add("effects", effectsToJsonArray(actorHandler.getEffects()));
            array.add(actorElement);
        }
        return array;
    }

    public static SceneParameters getSceneParametersFromJson(JsonObject jsonObject){
        return gson.fromJson(jsonObject, SceneParameters.class);
    }

    private static JsonElement effectsToJsonArray(List<BaseEffect> effects){
        JsonArray array = new JsonArray();
        for (BaseEffect effect:effects){
            JsonObject actorElement = new JsonObject();
            actorElement.add("name", toElement(effect.getName()));
            actorElement.add("type_name", toElement(effect.getClass().getSimpleName()));
            actorElement.add("disabled", toElement(effect.isDisabled()));
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

    public static Array<Pair<Pixmap, JsonObject>> JsonArrayToPairs(JsonArray jsonArray, Context context, String folderName){
        Array<Pair<Pixmap, JsonObject>> pairs = new Array<>();
        for(JsonElement element:jsonArray){

            JsonObject object = element.getAsJsonObject();

            String name = object.get("name").getAsString();
            Log.i("JSONFormatter", "getting "+name+" texture");
            Bitmap bitmap = ProjectManager.getBitmapFromFolderByName(context, name+".png", folderName);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            Pair<Pixmap, JsonObject> pair = new Pair<>(
                    new Pixmap(byteArray, 0, byteArray.length),
                    object
            );

            pairs.add(pair);

        }
        return pairs;
    }

    public static List<BaseEffect> JsonArrayToEffects(JsonArray jsonArray){
        List<BaseEffect> array = new ArrayList<>();
        for(JsonElement element:jsonArray) {
            BaseEffect effect = null;
            JsonObject object = element.getAsJsonObject();

            String name = object.get("name").getAsString();

            String typeName = object.get("type_name").getAsString();

            boolean disabled = object.get("disabled").getAsBoolean();

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
                effect.setDisabled(disabled);
                array.add(effect);
            }
        }
        return array;
    }

}
