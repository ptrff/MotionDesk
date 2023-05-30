package ru.ptrff.motiondesk.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.ptrff.motiondesk.data.local.WallpaperItemRepository;
import ru.ptrff.motiondesk.models.WallpaperItem;

public class ProjectManager {
    private static final String ROOT_DIR_NAME = "MotionDesk";
    private static final String TAG = "ProjectManager";
    private static final String PROJECTS_DIR_NAME = "Projects";
    private static final String CURRENT_DIR_NAME = "Current";
    private static final String TEMP_DIR_NAME = "Temp";

    public static File getRootDirectory(Context context) {
        File appDataDir = context.getApplicationContext().getFilesDir();

        File rootDir = new File(appDataDir, ROOT_DIR_NAME);
        if (!rootDir.exists()) {
            rootDir.mkdir();
        }

        return rootDir;
    }

    public static File getProjectsDirectory(Context context) {
        File rootDir = getRootDirectory(context);

        File projectsDir = new File(rootDir, PROJECTS_DIR_NAME);
        if (!projectsDir.exists()) {
            projectsDir.mkdir();
        }

        return projectsDir;
    }

    public static File getCurrentDirectory(Context context) {
        File rootDir = getRootDirectory(context);

        File projectsDir = new File(rootDir, CURRENT_DIR_NAME);
        if (!projectsDir.exists()) {
            projectsDir.mkdir();
        }

        return projectsDir;
    }

    @SuppressLint("CheckResult")
    public static void createProject(Context context, WallpaperItem project, ZipMaster master) throws IOException {
        File projectsDir = getProjectsDirectory(context);

        String projectName = project.getId();
        File projectDir = new File(projectsDir, projectName);
        if (!projectDir.exists()) {
            projectDir.mkdir();
        }


        if(project.hasPreviewImage() && project.getImage()!=null){
            File previewFile = new File(projectDir, "preview.jpg");
            OutputStream previewStream = Files.newOutputStream(previewFile.toPath());
            project.getImage().compress(Bitmap.CompressFormat.JPEG, 90, previewStream);
            previewStream.close();
        }

        WallpaperItemRepository repo = new WallpaperItemRepository(context);
        repo.insertWallpaperItem(project)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {},
                        (error) -> Log.e(TAG, "Error adding WallpaperItem to database", error)
                );

        String zipFilePath = new File(projectDir, "scene.zip").getAbsolutePath();
        master.archiveScene(zipFilePath);

    }

    @SuppressLint("CheckResult")
    public static boolean removeProject(Context context, String id) {
        WallpaperItemRepository repo = new WallpaperItemRepository(context);
        repo.removeWallpaperItemById(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {},
                        (error) -> Log.e(TAG, "Error removing WallpaperItem from database", error)
                );

        File projectFiles = new File(getProjectsDirectory(context)+"/"+id);
        if (projectFiles.isDirectory()) {
            File[] files = projectFiles.listFiles();
            if (files != null) {
                for (File file : files) {
                    boolean success = file.delete();
                    if (!success) {
                        return false;
                    }
                }
            }
            return projectFiles.delete();
        }
        return false;
    }

    public static List<File> getProjectFiles(Context context) {
        File projectsDir = getProjectsDirectory(context);

        File[] projectDirs = projectsDir.listFiles(File::isDirectory);

        Log.i(TAG, "projects:");
        for (File project : projectDirs) {
            WallpaperItem wallpaperItem = convertProjectFileToWallpaperItem(project);
            if (wallpaperItem != null) {
                IDGenerator.usedIDs.add(String.valueOf(wallpaperItem.getId()));
                Log.i(TAG, wallpaperItem.getId() + " " + wallpaperItem.getName());
            } else {
                Log.e(TAG, project.getName() + " reading error");
            }
        }

        return new ArrayList<>(Arrays.asList(projectDirs));
    }

    public static WallpaperItem convertProjectFileToWallpaperItem(File project) {
        try {
            return new Gson().fromJson(getWallpaperItemJsonString(project), WallpaperItem.class);
        } catch (IOException e) {
            return null;
        }
    }

    @SuppressLint("DefaultLocale")
    public static String getProjectSize(Context context, String id){
        File file = new File(getProjectsDirectory(context)+"/"+id);
        long fileSizeInBytes = file.length();

        String hrSize;
        double m = fileSizeInBytes/1024.0;
        DecimalFormat dec = new DecimalFormat("0");

        if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else {
            hrSize = dec.format(fileSizeInBytes).concat(" KB");
        }

        return hrSize;
    }

    public static void unpackProjectToFolder(Context context, String id, String folderName) {
        File targetDir = new File(getRootDirectory(context), folderName);
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }else {
            File[] files = targetDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    boolean success = file.delete();
                    if (!success) {
                        Log.e(TAG, "Error deleting file "+file);
                    }
                }
            }
        }

        unpackProject(context, id, targetDir);
    }

    private static void unpackProject(Context context, String id, File targetDir) {
        File project = getProjectDirById(context, id);

        Log.i(TAG, "Unpacking "+id+" to "+targetDir);

        try {
            ZipFile zipFile = new ZipFile(new File(project, "scene.zip"));
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryFile = new File(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    InputStream entryStream = zipFile.getInputStream(entry);
                    FileOutputStream entryOutStream = new FileOutputStream(entryFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = entryStream.read(buffer)) != -1) {
                        entryOutStream.write(buffer, 0, bytesRead);
                    }
                    entryOutStream.close();
                    entryStream.close();
                }
            }
            zipFile.close();
        } catch (IOException e) {
            Log.e(TAG, "Error unpacking for "+id, e);
        }

        try {
            File previewFile = new File(project, "preview.jpg");
            if (previewFile.exists()) {
                File newPreviewFile = new File(targetDir, "preview.jpg");
                Files.copy(previewFile.toPath(), newPreviewFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            File wallpaperFile = new File(project, "wallpaper.json");
            if (wallpaperFile.exists()) {
                File newWallpaperFile = new File(targetDir, "wallpaper.json");
                Files.copy(wallpaperFile.toPath(), newWallpaperFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error copying preview.jpg and wallpaper.json", e);
        }
    }

    public static File getProjectDirById(Context context, String id) {
        return new File(getProjectsDirectory(context), id);
    }

    public static boolean projectExists(Context context, String id) {
        return (new File(getProjectsDirectory(context), id)).exists();
    }

    public static JsonObject getSceneJsonFromFolder(Context context, String folderName) {
        File sceneFile = new File(new File(getRootDirectory(context), folderName), "scene.json");
        try {
            FileReader fileReader = new FileReader(sceneFile);
            Gson gson = new Gson();
            JsonObject object = gson.fromJson(fileReader, JsonObject.class);
            fileReader.close();
            return object;
        } catch (IOException e){
            Log.e(TAG, "scene.json reading error for "+sceneFile.getAbsolutePath());
            return null;
        }
    }

    public static String getWallpaperItemJsonString(File projectDir) throws IOException {
        File wallpaperFile = new File(projectDir, "wallpaper.json");
        try (FileInputStream inputStream = new FileInputStream(wallpaperFile)) {
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }
    }

    public static Bitmap getBitmapFromFolderByName(Context context, String name, String folderName){
        File file = new File(new File(getRootDirectory(context), folderName), name);
        if (!file.exists()) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        return bitmap;
    }

    public static File getPreviewById(Context context, String id){
        return new File(getProjectDirById(context, id), "preview.jpg");
    }
}