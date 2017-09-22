package com.ixp.util;

import android.content.Context;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.ixp.service.LocationService;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDLocation;


public class LocationUtil {

    private static final String TAG = "LocationUtil";
    private static final String KEY_PROVINCE = "currentProvince";
    private static LocationService locationService = null;
    private static int mProvinceCode = -1;
    private static String mProvinceName = null;
    private static String[] mProvinceList = new String[] {"北京", "天津", "河北", "山西", "内蒙古", "辽宁", "吉林", "黑龙江", "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南", "广东", "广西", "海南", "重庆", "四川", "贵州", "云南", "西藏", "陕西", "甘肃", "青海", "宁夏", "新疆", "香港", "澳门", "台湾"};

    public static void init(Context context) {
        mProvinceName = DataManager.getString(KEY_PROVINCE);
        locationService = new LocationService(context.getApplicationContext());
        SDKInitializer.initialize(context.getApplicationContext());
        locationService.registerListener(mListener);
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        locationService.start();
    }

    public static int getCity() {
        if (mProvinceCode == -1) {
            mProvinceCode = findCityCode(mProvinceName);
        }
        return mProvinceCode != -1 ? mProvinceCode : 0;
    }

    public static void cleanup() {
        stop();
    }

    private static int findCityCode(String provinceName) {
        if (provinceName == null)
            return -1;
        for (int i = 0; i < mProvinceList.length; i++) {
            if (provinceName.startsWith(mProvinceList[i])) {
                return i;
            }
        }
        return -1;
    }

    private static void stop() {
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
    }

    private static BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                mProvinceName = location.getProvince();
                if (Configs.DEBUG) {
                    Log.d(TAG, "province name is " + mProvinceName);
                }
                DataManager.storeString(KEY_PROVINCE, mProvinceName);
                stop();
            }
        }
    };
}
