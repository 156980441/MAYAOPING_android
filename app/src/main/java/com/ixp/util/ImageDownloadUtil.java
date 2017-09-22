package com.ixp.util;

/**
 * Created by ixp on 12/12/16.
 */

public class ImageDownloadUtil {

    private static HttpConnection mHttpConnection = null;

    private static void init() {
        if (mHttpConnection == null) {
            mHttpConnection = new HttpConnection();
        }
    }

    public static void cleanup() {
        mHttpConnection = null;
    }

    public static void get(String url, HttpConnection.Callback callback) {
        init();
        mHttpConnection.get(url, callback);
    }

    public static void post(String url, String postData, HttpConnection.Callback callback) {
        init();
        mHttpConnection.post(url, postData, callback);
    }
}
