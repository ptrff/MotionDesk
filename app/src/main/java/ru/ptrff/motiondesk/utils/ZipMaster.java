package ru.ptrff.motiondesk.utils;

import android.util.Log;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.StreamUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipMaster {
    private final Array<Texture> textures;
    private JsonObject scene;

    public ZipMaster() {
        textures = new Array<>();
    }

    public void addTexture(Texture texture) {
        textures.add(texture);
    }

    public void setSceneJson(JsonObject sceneJson){
        scene = sceneJson;
    }

    public File archiveScene(String filePath) {
        try {
            File outputFile = new File(filePath);
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

            byte[] sceneData = scene.toString().getBytes(StandardCharsets.UTF_8);
            zipOutputStream.putNextEntry(new ZipEntry("scene.json"));
            zipOutputStream.write(sceneData);
            zipOutputStream.closeEntry();

            Pixmap pixmap;
            for (Texture texture : textures) {
                String entryName = String.valueOf(textures.indexOf(texture, false));

                TextureData textureData = texture.getTextureData();
                if (!textureData.isPrepared()) {
                    textureData.prepare();
                }
                pixmap = textureData.consumePixmap();

                FileHandle fileHandle = new FileHandle(filePath.replace("scene.zip", "")+entryName);
                PixmapIO.writePNG(fileHandle, pixmap);

                zipOutputStream.putNextEntry(new ZipEntry(entryName + ".png"));
                byte[] pngData = fileHandle.readBytes();
                zipOutputStream.write(pngData);
                zipOutputStream.closeEntry();

                pixmap.dispose();
                fileHandle.delete();
            }

            zipOutputStream.finish();
            zipOutputStream.close();
            return outputFile;
        } catch (Exception e) {
            Log.e("ZipMaster", "Error archiving textures", e);
            return null;
        }
    }

    public void unarchiveScene(String filePath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.equals("scene.json")) {
                    byte[] sceneData = readEntryData(zipInputStream, (int) entry.getSize());
                    String sceneJsonString = new String(sceneData, StandardCharsets.UTF_8);
                    JsonElement element = JsonParser.parseReader(new StringReader(sceneJsonString));
                    scene = element.getAsJsonObject();
                } else if (entryName.endsWith(".png")) {
                    byte[] textureData = readEntryData(zipInputStream, (int) entry.getSize());
                    Pixmap texturePixmap = new Pixmap(textureData, 0, textureData.length);
                    Texture texture = new Texture(texturePixmap);
                    textures.add(texture);
                    texturePixmap.dispose();
                }
                zipInputStream.closeEntry();
            }

            zipInputStream.close();
        } catch (Exception e) {
            Log.e("ZipMaster", "Error unarchiving scene", e);
        }
    }

    private byte[] readEntryData(ZipInputStream inputStream, int size) throws Exception {
        byte[] data = new byte[size];
        int bytesRead = 0;
        while (bytesRead < size) {
            int count = inputStream.read(data, bytesRead, size - bytesRead);
            if (count == -1) {
                Log.e("ZipMaster", "Unexpected end of entry data");
            }
            bytesRead += count;
        }
        return data;
    }
}
