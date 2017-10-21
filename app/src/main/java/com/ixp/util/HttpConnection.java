package com.ixp.util;

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ixp on 11/6/16.
 */

public class HttpConnection {

    private static final String TAG = "HttpConnection";
    private static final int TIMEOUT = 5000;
    private LinkedList<RequestData> mRequestQueue = new LinkedList<>();
    private ExecutorService mService = Executors.newFixedThreadPool(2);

    // 网络回到成功或者失败的回调
    public interface Callback{

        void onSucess(byte[] data, int size);

        void onFailed(int code);
    }

    private static class RequestData {
        String url;
        String method;
        byte[] postData;
        Callback callback;

        public RequestData(String url, Callback callback) {
            this.url = url;
            this.callback = callback;
            this.method = "GET";
        }

        public RequestData(String url, Callback callback, byte[] postData) {
            this.url = url;
            this.callback = callback;
            this.method = "POST";
            this.postData = postData;
        }

        @Override
        public String toString() {
            return "RequestData{" +
                    "url='" + url + '\'' +
                    ", method='" + method + '\'' +
                    ", postData=" + (postData != null ? new String(postData) : "null") +
                    ", callback=" + callback +
                    '}';
        }
    }

    /**
     * 自适应大小的buffer
     */
    private static class ByteArray {
        byte[] mData;
        int mSize;

        public ByteArray() {
            this(16);
        }

        public ByteArray(int size) {
            mData = new byte[size];
            mSize = 0;
        }

        public void put(byte[] arr, int size) {
            if (mSize + size > mData.length) {
                byte[] oldData = mData;
                mData = new byte[newSize(mSize + size)];
                System.arraycopy(oldData, 0, mData, 0, mSize);
            }

            System.arraycopy(arr, 0, mData, mSize, size);
            mSize += size;
        }

        public byte[] getData() {
            return mData;
        }

        public int getSize() {
            return mSize;
        }

        private static int newSize(int oldSize) {
            return oldSize + 1024;
        }

    }

    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            RequestData data = null;

            synchronized (mRequestQueue) {
                if (!mRequestQueue.isEmpty()) {
                    data = mRequestQueue.poll();
                }
            }

            if (data == null) {
                return;
            }

            if (Configs.DEBUG) {
                Log.d("[MyRunnable]", "executor " + data);
            }

            try {
                HttpURLConnection connection = (HttpURLConnection)new URL(data.url).openConnection();
                boolean isPost = data.method.equals("POST");

                connection.setConnectTimeout(TIMEOUT);
                connection.setReadTimeout(TIMEOUT);

                connection.setDoInput(true);

                if (isPost) {
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-type", "application/json");
                }

                connection.connect();

                if (isPost) {
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(data.postData);
                    outputStream.close();
                }

                int code = connection.getResponseCode();

                if (code == 200) {
                    ByteArray byteArray = new ByteArray();
                    byte[] buffer = new byte[1024];
                    InputStream inputStream = connection.getInputStream();
                    int readSize;

                    while ((readSize = inputStream.read(buffer)) > 0) {
                        byteArray.put(buffer, readSize);
                    }
                    inputStream.close();
                    if (Configs.DEBUG) {
                        Log.d(TAG, "response " + new String(byteArray.getData(), 0, byteArray.getSize()));
                    }
                    data.callback.onSucess(byteArray.getData(), byteArray.getSize());

                } else {
                    data.callback.onFailed(code);
                }
            } catch (Exception e) {
                e.printStackTrace();
                data.callback.onFailed(0);
            }

            executeNext();
        }
    }

    public void get(String url, Callback callback) {
        RequestData data = new RequestData(url, callback);
        if (Configs.DEBUG) {
            Log.d(TAG, "[get] data is " + data);
        }
        synchronized (mRequestQueue) {
            mRequestQueue.add(data);
        }
        executeNext();
    }

    public void post(String url, String postData, Callback callback) {
        RequestData data = new RequestData(url, callback, postData.getBytes());
        if (Configs.DEBUG) {
            Log.d(TAG, "[post] data is " + data);
        }
        synchronized (mRequestQueue) {
            mRequestQueue.add(data);
        }
        executeNext();
    }

    public static void init() {

    }

    public void cleanup() {
        synchronized (mRequestQueue) {
            mRequestQueue.clear();
        }
        mService.shutdown();
    }

    private void executeNext() {
        if (!mRequestQueue.isEmpty()) {
            mService.submit(new MyRunnable());
        }
    }
}
