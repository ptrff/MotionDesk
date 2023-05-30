package ru.ptrff.motiondesk.utils;

import android.util.Log;
import android.util.Pair;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.Array;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipMaster {
    private final Array<Pair<Texture, String>> textures;
    private JsonObject scene;

    public ZipMaster() {
        textures = new Array<>();
    }

    public void addTexture(Texture texture, String name) {
        textures.add(new Pair<>(texture, name));
    }

    public void setSceneJson(JsonObject sceneJson){
        scene = sceneJson;
    }

    public void archiveScene(String filePath) {
        try {
            File outputFile = new File(filePath);
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

            byte[] sceneData = scene.toString().getBytes(StandardCharsets.UTF_8);
            zipOutputStream.putNextEntry(new ZipEntry("scene.json"));
            zipOutputStream.write(sceneData);
            zipOutputStream.closeEntry();

            Pixmap pixmap;
            for (Pair<Texture, String> textureAndName : textures) {
                Texture texture = textureAndName.first;
                String textureName = textureAndName.second;

                TextureData textureData = texture.getTextureData();
                if (!textureData.isPrepared()) {
                    textureData.prepare();
                }
                pixmap = textureData.consumePixmap();

                FileHandle fileHandle = new FileHandle(filePath.replace("scene.zip", "")+textureName);
                PixmapIO.writePNG(fileHandle, pixmap);

                zipOutputStream.putNextEntry(new ZipEntry(textureName + ".png"));
                byte[] pngData = fileHandle.readBytes();
                zipOutputStream.write(pngData);
                zipOutputStream.closeEntry();

                //pixmap.dispose();
                fileHandle.delete();
            }

            zipOutputStream.finish();
            zipOutputStream.close();
        } catch (Exception e) {
            Log.e("ZipMaster", "Error archiving textures", e);
        }
    }
}
