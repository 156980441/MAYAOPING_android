package com.ixp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.ixp.devicemonitor.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;


public class AdManager {

    private static final String TAG = "AdManager";
    private static final String KEY_LINK_AD_UPDATE_DATE = "linkAdUpdateDate";
    private static final String KEY_BITMAP_AD_UPDATE_DATE = "bitmapAdUpdateDate";
    private static final String KEY_LINK_AD1 = "linkAd1";
    private static final String KEY_LINK_AD2 = "linkAd2";
    private static final String KEY_LINK_AD3 = "linkAd3";
    private static final String KEY_BITMAP_AD1 = "bitmapAd1";
    private static final String KEY_BITMAP_AD2 = "bitmapAd2";
    private static final String KEY_BITMAP_AD3 = "bitmapAd3";
    private static final String KEY_LINK_URL = "linkUrl";

    private static final String DEFAULT_LINK_URL = "http://www.baidu.com";

    private static String[] mLinkeAdKeys = new String[] {KEY_LINK_AD1, KEY_LINK_AD2, KEY_LINK_AD3};
    private static String[] mBitmapAdKeys = new String[] {KEY_BITMAP_AD1, KEY_BITMAP_AD2, KEY_BITMAP_AD3};

    public static int[] mLinkAdImageIds = new int[]{R.drawable.t1, R.drawable.t2, R.drawable.t3};
    public static int[] mBitmapAdImageIds = new int[]{R.drawable.t1, R.drawable.t2, R.drawable.t3};

    public static HttpConnection.Callback[] mLinkAdCallbacks = new HttpConnection.Callback[3];
    public static HttpConnection.Callback[] mBitmapAdCallbacks = new HttpConnection.Callback[3];

    private static ArrayList<AdItem> mTopAdList = new ArrayList<>();
    private static ArrayList<AdItem> mBottomAdList = new ArrayList<>();

    private static HttpConnection mHttpConnection = new HttpConnection();
    private static int mCityId = 0;     // default: beijing
    private static boolean mDownloadListFinished = true;
    private static Context mContext = null;

    public static void refreshAds(int cityId) {
        mCityId = cityId;

        if (downloadAdsFinished() && mDownloadListFinished) {
            mDownloadListFinished = false;
            mHttpConnection.get(Configs.AD_LIST_URL + cityId, new HttpConnection.Callback() {
                @Override
                public void onSucess(byte[] data, int size) {
                    if (Configs.DEBUG) {
                        Log.e(TAG, "download ad list succ");
                    }
                    String string = new String(data, 0, size);
                    downloadAdList(string);
                }

                @Override
                public void onFailed(int code) {
                    mDownloadListFinished = true;
                }
            });
        }
    }

    private static boolean downloadAdsFinished() {
        for (int i = 0; i < mLinkAdCallbacks.length; i++) {
            if (mLinkAdCallbacks[i] != null)
                return false;
        }
        for (int i = 0; i < mBitmapAdCallbacks.length; i++) {
            if (mBitmapAdCallbacks[i] != null)
                return false;
        }
        return true;
    }

    private static void loadFromCache() {
        String linkUrl = DataManager.getString(KEY_LINK_URL);
        String linkAd1 = DataManager.getString(KEY_LINK_AD1);
        String linkAd2 = DataManager.getString(KEY_LINK_AD2);
        String linkAd3 = DataManager.getString(KEY_LINK_AD3);

        File file1 = null, file2 = null, file3 = null;

        if (linkAd1 != null && linkUrl != null) {
            file1 = new File(DirectoryManager.getStorageDirectory() + "/" + linkAd1);
            file2 = new File(DirectoryManager.getStorageDirectory() + "/" + linkAd2);
            file3 = new File(DirectoryManager.getStorageDirectory() + "/" + linkAd3);
        }

        if (file1 != null && (file1.exists() || file2.exists() || file3.exists())) {
            Bitmap bitmap = null;
            AdItem adItem;

            if (file1.exists()) {
                bitmap = BitmapFactory.decodeFile(file1.getAbsolutePath());
            }

            adItem = new AdItem(bitmap, linkUrl, linkAd1);
            mBottomAdList.add(adItem);

            if (file2.exists()) {
                bitmap = BitmapFactory.decodeFile(file2.getAbsolutePath());
            }
            adItem = new AdItem(bitmap, linkUrl, linkAd2);
            mBottomAdList.add(adItem);

            if (file3.exists()) {
                bitmap = BitmapFactory.decodeFile(file3.getAbsolutePath());
            }
            adItem = new AdItem(bitmap, linkUrl, linkAd3);
            mBottomAdList.add(adItem);
        } else {
            for (int i = 0; i < mLinkAdImageIds.length; i++) {
                AdItem item = new AdItem(BitmapFactory.decodeResource(mContext.getResources(), mLinkAdImageIds[i], null), DEFAULT_LINK_URL, null);
                mBottomAdList.add(item);
            }
        }


        linkAd1 = DataManager.getString(KEY_BITMAP_AD1);
        linkAd2 = DataManager.getString(KEY_BITMAP_AD2);
        linkAd3 = DataManager.getString(KEY_BITMAP_AD3);

        if (linkAd1 != null && linkUrl != null) {
            file1 = new File(DirectoryManager.getStorageDirectory() + "/" + linkAd1);
            file2 = new File(DirectoryManager.getStorageDirectory() + "/" + linkAd2);
            file3 = new File(DirectoryManager.getStorageDirectory() + "/" + linkAd3);
        }

        if (file1 != null && (file1.exists() || file2.exists() || file3.exists())) {
            Bitmap bitmap = null;
            AdItem adItem;

            if (file1.exists()) {
                bitmap = BitmapFactory.decodeFile(file1.getAbsolutePath());
            }

            adItem = new AdItem(bitmap, linkUrl, linkAd1);
            mTopAdList.add(adItem);

            if (file2.exists()) {
                bitmap = BitmapFactory.decodeFile(file2.getAbsolutePath());
            }
            adItem = new AdItem(bitmap, linkUrl, linkAd2);
            mTopAdList.add(adItem);

            if (file3.exists()) {
                bitmap = BitmapFactory.decodeFile(file3.getAbsolutePath());
            }
            adItem = new AdItem(bitmap, linkUrl, linkAd3);
            mTopAdList.add(adItem);
        } else {
            for (int i = 0; i < mBitmapAdImageIds.length; i++) {
                AdItem item = new AdItem(BitmapFactory.decodeResource(mContext.getResources(), mBitmapAdImageIds[i], null), null, null);
                mTopAdList.add(item);
            }
        }
    }

    public static void init(Context context) {
        mContext = context;
        loadFromCache();
    }

    public static ArrayList<AdItem> getTopAdList() {
        return mTopAdList;
    }

    public static ArrayList<AdItem> getBottomList() {
        return mBottomAdList;
    }

    private static int findIndex(HttpConnection.Callback[] callbacks, HttpConnection.Callback callback) {
        for (int i = 0; i < callbacks.length; i++) {
            if (callback == callbacks[i]) {
                return i;
            }
        }
        return -1;
    }

    private static String formatUrl(String url) {
        if (!url.startsWith("http")) {
            return "http://" + url;
        }
        return url;
    }
    private static void downloadLinkAd(int i, String ad, String linkUrl) {
        AdItem item = mBottomAdList.get(i);
        item.url = linkUrl;
        item.fileName = Util.getFileName(ad);

        DataManager.storeString(mLinkeAdKeys[i], item.fileName);
        DataManager.storeString(KEY_LINK_URL, linkUrl);

        mLinkAdCallbacks[i] = new HttpConnection.Callback() {

            @Override
            public void onSucess(byte[] data, int size) {
                int index = findIndex(mLinkAdCallbacks, this);
                if (index == -1)
                    return;
                mLinkAdCallbacks[index] = null;
                AdItem item1 = mBottomAdList.get(index);
                String filename = item1.fileName;
                Util.saveImage(DirectoryManager.getStorageDirectory() + "/" + filename, data, size);
                BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inJustDecodeBounds = true;
                item1.image = BitmapFactory.decodeByteArray(data, 0, size, options);
                if (Configs.DEBUG) {
                    Log.e(TAG, "download link ad succ " + filename);
                }
            }

            @Override
            public void onFailed(int code) {
                int index = findIndex(mLinkAdCallbacks, this);
                if (index == -1)
                    return;
                mLinkAdCallbacks[index] = null;

                if (Configs.DEBUG) {
                    Log.e(TAG, "download link ad failed, code is " + code);
                }
            }
        };
        mHttpConnection.get(Configs.HOST_URL + ad, mLinkAdCallbacks[i]);
    }

    private static void downloadBitmapAd(int i, String ad) {
        AdItem item = mTopAdList.get(i);
        item.url = null;
        item.fileName = Util.getFileName(ad);

        DataManager.storeString(mBitmapAdKeys[i], item.fileName);

        mBitmapAdCallbacks[i] = new HttpConnection.Callback() {

            @Override
            public void onSucess(byte[] data, int size) {
                int index = findIndex(mBitmapAdCallbacks, this);
                if (index == -1)
                    return;
                mBitmapAdCallbacks[index] = null;
                AdItem item1 = mTopAdList.get(index);
                String filename = item1.fileName;
                Util.saveImage(DirectoryManager.getStorageDirectory() + "/" + filename, data, size);
                BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inJustDecodeBounds = true;
                item1.image = BitmapFactory.decodeByteArray(data, 0, size, options);
                if (Configs.DEBUG) {
                    Log.e(TAG, "download bitmap ad succ " + filename);
                }
            }

            @Override
            public void onFailed(int code) {
                int index = findIndex(mBitmapAdCallbacks, this);
                if (index == -1)
                    return;
                mBitmapAdCallbacks[index] = null;
                if (Configs.DEBUG) {
                    Log.e(TAG, "download bitmap ad failed, code is " + code);
                }
            }
        };
        mHttpConnection.get(Configs.HOST_URL + ad, mBitmapAdCallbacks[i]);
    }
    private static void downloadLinkAds(String[] adList, String url) {
        for (int i = 0; i < adList.length; i++) {
            downloadLinkAd(i, adList[i], url);
        }
    }

    private static void downloadBitmapAds(String[] adList) {
        for (int i = 0; i < adList.length; i++) {
            downloadBitmapAd(i, adList[i]);
        }
    }

    private static void downloadAdList(String adList) {
        try {
            JSONArray array = new JSONArray(adList);
            JSONObject linkAdObject = array.getJSONObject(0);
            JSONObject bitmapAdObject = array.getJSONObject(1);

            String linkAdUpdateDate = linkAdObject.getString("UPDATE_DATE");
            String bitmapAdUpdateDate = bitmapAdObject.getString("UPDATE_DATE");

            String lastLinkUpdateDate = DataManager.getString(KEY_LINK_AD_UPDATE_DATE);
            String lastBitmapUpdateDate = DataManager.getString(KEY_BITMAP_AD_UPDATE_DATE);

            String[] linkAdList = new String[3];
            String[] bitmapAdList = new String[3];

            String linkUrl = formatUrl(linkAdObject.getString("ADV_URL"));

            linkAdList[0] = linkAdObject.getString("PIC_URL1");
            linkAdList[1] = linkAdObject.getString("PIC_URL2");
            linkAdList[2] = linkAdObject.getString("PIC_URL3");

            bitmapAdList[0] = bitmapAdObject.getString("PIC_URL1");
            bitmapAdList[1] = bitmapAdObject.getString("PIC_URL2");
            bitmapAdList[2] = bitmapAdObject.getString("PIC_URL3");

            if (Util.compareTime(linkAdUpdateDate, lastLinkUpdateDate) > 0) {
                DataManager.storeString(KEY_LINK_AD_UPDATE_DATE, linkAdUpdateDate);
                downloadLinkAds(linkAdList, linkUrl);
            } else {
                for (int i = 0; i < linkAdList.length; i++) {
                    String filename = Util.getFileName(linkAdList[i]);
                    File file = new File(DirectoryManager.getStorageDirectory() + "/" + filename);
                    if (Configs.DEBUG) {
                        Log.d(TAG, "link ad file " + linkAdList[i] + " exist " + file.exists());
                    }
                    if (!file.exists()) {
                        downloadLinkAd(i, linkAdList[i], linkUrl);
                    }
                }
            }

            if (Util.compareTime(bitmapAdUpdateDate, lastBitmapUpdateDate) > 0) {
                DataManager.storeString(KEY_BITMAP_AD_UPDATE_DATE, bitmapAdUpdateDate);
                downloadBitmapAds(bitmapAdList);
            } else {
                for (int i = 0; i < bitmapAdList.length; i++) {
                    File file = new File(DirectoryManager.getStorageDirectory() + "/" + Util.getFileName(bitmapAdList[i]));
                    if (Configs.DEBUG) {
                        Log.d(TAG, "bitmap ad file " + bitmapAdList[i] + " exist " + file.exists());
                    }
                    if (!file.exists()) {
                        downloadBitmapAd(i, bitmapAdList[i]);
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
