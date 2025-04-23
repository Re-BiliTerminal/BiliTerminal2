package com.huanli233.biliterminal2.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.huanli233.biliterminal2.BiliTerminal;
import com.huanli233.biliterminal2.BiliTerminalKt;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;

public class FileUtil {
    public static void clearCache(Context context) {
        File cacheDir = context.getCacheDir();
        if (cacheDir.exists() && Objects.requireNonNull(cacheDir.listFiles()).length != 0)
            deleteFolder(cacheDir);
    }

    public static void deleteFolder(File folder) {
        if (!folder.exists()) return;

        if (folder.isFile()) {
            folder.delete();
            return;
        }

        File[] templist = folder.listFiles();
        assert templist != null;
        for (File file : templist) {
            if (file.isFile()) {
                file.delete();
            } else {
                if (Objects.requireNonNull(file.listFiles()).length != 0)
                    deleteFolder(file);
            }
        }
        folder.delete();
    }

    public static JSONObject readJson(File file) {
        if (file == null || !file.exists() || !file.canRead() || !file.isFile()) return null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            FileInputStream inputStream = new FileInputStream(file);
            FileChannel channel = inputStream.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1 << 13);
            int i;
            while ((i = channel.read(buffer)) != -1) {
                buffer.flip();
                outputStream.write(buffer.array(), 0, i);
                buffer.clear();
            }
            return new JSONObject(outputStream.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkStoragePermission() {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < 17) return true;
        return ContextCompat.checkSelfPermission(BiliTerminalKt.getContextNotNull(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(BiliTerminalKt.getContextNotNull(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void requestStoragePermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
    }

    public static File getDownloadPath() {
        File path = new File(Preferences.getString("save_path_video", Environment.getExternalStorageDirectory() + "/Android/media/" + BiliTerminalKt.getContextNotNull().getPackageName() + "/"));
        try {
            File nomedia = new File(path, ".nomedia");    //为了防止系统扫描
            if (!nomedia.exists()) nomedia.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    public static File getDownloadPath(String title) {
        return getDownloadPath(title, null);
    }

    public static File getDownloadPath(String title, String child) {
        File parentFolder = new File(getDownloadPath(), stringToFile(title));
        if (child == null || child.isEmpty()) return parentFolder;
        return new File(parentFolder, stringToFile(child));
    }

    public static File getDownloadPicturePath() {
        return new File(Preferences.getString("save_path_pictures", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/哔哩终端/"));
    }

    public static void requireTFCardPermission() {

    }

    public static String stringToFile(String str) {
        return str.substring(0, Math.min(85, str.length()))
                .replace("|", "｜")
                .replace(":", "：")
                .replace("*", "﹡")
                .replace("?", "？")
                .replace("\"", "”")
                .replace("<", "＜")
                .replace(">", "＞")
                .replace("/", "／")
                .replace("\\", "＼");
    }

    public static String getFileNameFromLink(String link) {
        int length = link.length();
        for (int i = length - 1; i > 0; i--) {
            if (link.charAt(i) == '/') {
                return link.substring(i + 1);
            }
        }
        return "fail";
    }

    public static String getFileFirstName(String file) {
        for (int i = 0; i < file.length(); i++) {
            if (file.charAt(i) == '.') {
                return file.substring(0, i);
            }
        }
        return "fail";
    }
}
