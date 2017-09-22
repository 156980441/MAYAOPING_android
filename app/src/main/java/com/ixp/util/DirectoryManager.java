package com.ixp.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by ixp on 12/11/16.
 */

public class DirectoryManager {

    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
    }

    public static String getStorageDirectory() {
        File file = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (!file.exists()) {
            file.mkdirs();
        }

        if (file.canRead() && file.canWrite()) {
            return file.getAbsolutePath();
        }

        file = Environment.getExternalStorageDirectory();
        String path = file.getAbsolutePath() + "/device_monitor";
        File newFile = new File(path);
        if (!newFile.exists())
            newFile.mkdirs();
        return path;
    }
}
