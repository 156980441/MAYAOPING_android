package com.ixp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.shapes.Shape;

/**
 * Created by ixp on 12/11/16.
 */

public class DataManager {

    private static final String NAME = "deviceManager";

    private static SharedPreferences mSharedPreference = null;

    public static void init(Context context) {
        mSharedPreference = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static void cleanup() {

    }

    public static void storeString(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void storeInt(String key, int value) {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static String getString(String key) {
        return mSharedPreference.getString(key, null);
    }

    public static int getInt(String key) {
        return mSharedPreference.getInt(key, 0);
    }
}
