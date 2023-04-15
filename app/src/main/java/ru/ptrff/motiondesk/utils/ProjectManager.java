package ru.ptrff.motiondesk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.ptrff.motiondesk.data.WallpaperItem;

public class ProjectManager {
    private static final String ROOT_DIR_NAME = "MotionDesk";
    private static final String PROJECTS_DIR_NAME = "Projects";

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

    public static void createProject(Context context, Bitmap preview, WallpaperItem project, ZipMaster master) throws IOException {
        File projectsDir = getProjectsDirectory(context);

        String projectName = project.getName();
        File projectDir = new File(projectsDir, projectName);
        if (!projectDir.exists()) {
            projectDir.mkdir();
        }

        File previewFile = new File(projectDir, "preview.jpg");
        OutputStream previewStream = new FileOutputStream(previewFile);
        preview.compress(Bitmap.CompressFormat.JPEG, 90, previewStream);
        previewStream.close();

        project.setImage(Uri.fromFile(previewFile).toString());

        Gson gson = new Gson();
        File wallpaperFile = new File(projectDir, "wallpaper.json");
        OutputStream wallpaperStream = new FileOutputStream(wallpaperFile);
        wallpaperStream.write(gson.toJson(project).getBytes());
        wallpaperStream.close();

        String zipFilePath = new File(projectDir, "scene.zip").getAbsolutePath();
        master.archiveScene(zipFilePath);

    }

    public static boolean removeProject(File projectDir) {
        if (projectDir.isDirectory()) {
            File[] files = projectDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    boolean success = file.delete();
                    if (!success) {
                        return false;
                    }
                }
            }
            return projectDir.delete();
        }
        return false;
    }

    public static List<File> getProjectFiles(Context context) {
        File projectsDir = getProjectsDirectory(context);

        File[] projectDirs = projectsDir.listFiles(File::isDirectory);

        Log.i("ProjectManager", "projects:");
        for(File project:projectDirs){
            WallpaperItem wallpaperItem = convertProjectFileToWallpaperItem(project);
            if(wallpaperItem!=null) {
                IDGenerator.usedIDs.add(String.valueOf(wallpaperItem.getId()));
                Log.i("ProjectManager", wallpaperItem.getId() + " " + wallpaperItem.getName());
            }else {
                Log.e("ProjectManager", project.getName()+" reading error");
            }
        }

        assert projectDirs != null;
        return new ArrayList<>(Arrays.asList(projectDirs));
    }

    public static WallpaperItem convertProjectFileToWallpaperItem(File project) {
        try {
            return new Gson().fromJson(getWallpaperItemJsonString(project), WallpaperItem.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static File getSceneFile(File projectDir) {
        return new File(projectDir, "scene.zip");
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

    public static Uri getPreviewUri(File projectDir) {
        File previewFile = new File(projectDir, "preview.jpg");
        return Uri.fromFile(previewFile);
    }
}
