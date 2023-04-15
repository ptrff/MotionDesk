package ru.ptrff.motiondesk.engine;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cursor;

public class UriFileHandleResolver implements FileHandleResolver {

    @Override
    public FileHandle resolve(String fileName) {
        Uri uri = Uri.parse(fileName);
        String path = uri.getPath();
        return Gdx.files.absolute(path);
    }
}
