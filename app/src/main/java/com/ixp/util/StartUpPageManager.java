package com.ixp.util;

import android.content.Context;
import android.util.Log;

import com.ixp.devicemonitor.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by ixp on 12/12/16.
 */

public class StartUpPageManager {

    private static final String TAG = "StartUpPageManager";

    private static final String KEY_STRAT_PICUTRE = "startPage";
    private static final String KEY_START_UPDATE_TIME = "startUpdateTime";

    private static String mLocalPath = null;

    private static final int mDefaultImage = R.drawable.default_start;

    public static String getImagePath(Context context) {
        String filename = DataManager.getString(KEY_STRAT_PICUTRE);
        File file = new File(DirectoryManager.getStorageDirectory() + "/" + filename);

        downloadStartInfo();

        if (filename == null || !file.exists()) {
            return null;
        }
        return file.getAbsolutePath();
    }


    private static void downloadStartImage(String json) {

        try {
            JSONArray array = new JSONArray(json);

            if (array.length() > 0) {
                JSONObject obj = array.getJSONObject(0);
                String urlname = obj.getString("PIC_URL");
                String updateDate = obj.getString("UPDATE_DATE");
                String filename = Util.getFileName(urlname);
                String lastUpdateDate = DataManager.getString(KEY_START_UPDATE_TIME);

                mLocalPath = DirectoryManager.getStorageDirectory() + "/" + filename;
                File file = new File(mLocalPath);

                if (Util.compareTime(updateDate, lastUpdateDate) > 0 || !file.exists()) {
                    DataManager.storeString(KEY_START_UPDATE_TIME, updateDate);
                    DataManager.storeString(KEY_STRAT_PICUTRE, filename);

                    ImageDownloadUtil.get(Configs.HOST_URL + urlname, new HttpConnection.Callback() {
                        @Override
                        public void onSucess(byte[] data, int size) {
                            Util.saveImage(mLocalPath, data, size);
                        }

                        @Override
                        public void onFailed(int code) {
                            if (Configs.DEBUG) {
                                Log.e(TAG, "download image failed " + code);
                            }
                        }
                    });
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void downloadStartInfo() {
        ImageDownloadUtil.get(Configs.START_PAGE_URL + LocationUtil.getCity(), new HttpConnection.Callback() {
            @Override
            public void onSucess(byte[] data, int size) {
                downloadStartImage(new String(data, 0, size));
            }

            @Override
            public void onFailed(int code) {
                if (Configs.DEBUG) {
                    Log.e(TAG, "load image failed " + code);
                }
            }
        });
    }

    public static int getDefaultImage() {
        return mDefaultImage;
    }
}
