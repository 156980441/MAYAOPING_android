package com.ixp.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;

import java.util.ArrayList;


public class DeviceDataManager {

    public interface OnDeviceListRefreshedListener {
        void onRefreshed(boolean succ, int code);
    }

    public interface OnDeviceInfoRefreshedListener {
        void onRefreshed(boolean succ, int code);
    }

    private static final long DELAY_TIME = 30 * 1000;

    private static Handler mHandler = null;

    private static Object mSyncObject = new Object();
    private static ArrayList<DeviceInfo> mDeviceList = new ArrayList<>();
    private static boolean mRefreshSucc = false;
    private static int mRefreshDeviecSeq = -1;
    private static String mRefreshDeviceId = null;
    private static OnDeviceInfoRefreshedListener mInfoListener = null;
    private static ArrayList<OnDeviceListRefreshedListener> mListListeners = new ArrayList<>();
    private static ListRefreshCallback mListHttpCallback = null;
    private static InfoRefreshedCallback mInfoHttpCallback = null;

    public static void init(Context context) {
        mHandler = new MyHandler();// 刷新数据

        mRefreshDeviceId = null;
        mRefreshDeviecSeq = -1;
        mInfoHttpCallback = null;
        mListHttpCallback = null;
        mDeviceList.clear();
        mRefreshSucc = false;
    }

    public static void cleanup() {
        mDeviceList.clear();
        mInfoHttpCallback = null;
        mListHttpCallback = null;
    }

    private static void generateTestData() {
        for (int i = 0; i < 5; i++) {
            DeviceInfo info = new DeviceInfo();
            info.name = "测试设备" + i;
            info.deviceId = "" + i;
            info.temperature = i + 0.5f;
            info.tds = i * 10;
            info.ph = i * .05f;
            info.state = false;

            mDeviceList.add(info);
        }
    }

    public static ArrayList<DeviceInfo> getDevices() {
        return  mDeviceList;
    }

    public static boolean isDeviceExist(String id) {
        for(DeviceInfo info : mDeviceList) {
            if (info.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static void addDevice(String name, String id) {
        DeviceInfo info = new DeviceInfo();
        info.name = name;
        info.deviceId = id;
        mDeviceList.add(info);
    }

    public static void setOnDeviceInfoRefreshedListener(OnDeviceInfoRefreshedListener listener) {
        mInfoListener = listener;
    }

    public static void addOnDeviceListRefreshedListener(OnDeviceListRefreshedListener listener) {
        mListListeners.add(listener);
    }

    public static void removeOnDeviceListRefreshedListener(OnDeviceListRefreshedListener listener) {
        mListListeners.remove(listener);
    }

    public static void refreshDeviceList(boolean forceRefresh) {
        if ((forceRefresh || !mRefreshSucc) && mListHttpCallback == null) {
            mListHttpCallback = new ListRefreshCallback();//网络监听
            HttpUtil.get(Configs.MACHINE_LIST_URL + Configs.userInfo.userNo, mListHttpCallback);
        }
        mHandler.sendEmptyMessageDelayed(0, DELAY_TIME);
    }

    public static void refreshDeviceInfo(String id) {
        synchronized (mSyncObject) {
            if (mInfoHttpCallback == null) {

            }
        }
    }

    public static void stopRefreshDeviceInfo() {
        synchronized (mSyncObject) {
            mInfoHttpCallback = null;
        }
    }

    private static void invokeListRefreshedCallback(boolean succ, int errCode) {
        mRefreshSucc = succ;
        mListHttpCallback = null;
        for (OnDeviceListRefreshedListener listener : mListListeners) {
            listener.onRefreshed(succ, errCode);
        }
    }

    private static void updateDeviceList(byte[] data, int size) {
        String string = new String(data, 0, size);
        mDeviceList.clear();
        try {
            JSONArray array = new JSONArray(string);
            for (int i = 0; i < array.length(); i++) {
                DeviceInfo info = DeviceInfo.createFromJson(array.getString(i));
                if (info != null) mDeviceList.add(info); // 防止 exception
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void invokeInfoRefreshedCallback(boolean succ, int errCode) {
        mInfoHttpCallback = null;
        if (mInfoListener != null) {
            mInfoListener.onRefreshed(succ, errCode);
        }
    }

    private static void updateDeviceInfo(byte[] data, int size) {

    }

    private static class ListRefreshCallback implements HttpConnection.Callback {

        @Override
        public void onSucess(byte[] data, int size) {
            updateDeviceList(data, size);
            invokeListRefreshedCallback(true, 0);
        }

        @Override
        public void onFailed(int code) {
            invokeListRefreshedCallback(false, code);
        }
    }

    private static class InfoRefreshedCallback implements HttpConnection.Callback {

        @Override
        public void onSucess(byte[] data, int size) {
            synchronized (mSyncObject) {
                if (mInfoHttpCallback == this) {
                    updateDeviceInfo(data, size);
                    invokeInfoRefreshedCallback(true, 0);
                }
            }
        }

        @Override
        public void onFailed(int code) {
            synchronized (mSyncObject) {
                if (mInfoHttpCallback == this) {
                    invokeInfoRefreshedCallback(false, code);
                }
            }
        }
    }

    private static class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
//            if (mRefreshDeviecSeq != -1 && mRefreshDeviceId != null) {
//                refreshDeviceInfo(mRefreshDeviceId);
//            }
            refreshDeviceList(true);
        }
    }
}
