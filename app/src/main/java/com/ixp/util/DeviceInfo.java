package com.ixp.util;

import org.json.JSONObject;

/**
 * Created by ixp on 9/25/16.
 */

public class DeviceInfo {
    public int seq;
    public String name;
    public String deviceId;
    public float temperature;
    public int tds;
    public float ph;
    public boolean state;
    public String updateTime;

    public static DeviceInfo createFromJson(String str) {

        try {
            DeviceInfo info = new DeviceInfo();
            JSONObject object = new JSONObject(str);
            info.seq = object.getInt("SEQ");
            info.name = object.getString("TITLE");
            info.deviceId = object.getString("ID");
            info.temperature = Float.valueOf(object.getString("TEMPERATURE"));//如果s为null，则抛出一个NullPointerException
            info.tds = (int)(float)Float.valueOf(object.getString("TDS"));
            info.ph = Float.valueOf(object.getString("PH"));
            info.state = "1".equals(object.getString("STATE"));
            info.updateTime = object.getString("UPDATE_DATE");
            return info;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
