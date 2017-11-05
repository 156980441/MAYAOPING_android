package com.ixp.devicemonitor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ixp.util.AdItem;
import com.ixp.util.AdManager;
import com.ixp.util.Configs;
import com.ixp.util.DeviceDataManager;
import com.ixp.util.HttpConnection;
import com.ixp.util.HttpUtil;

import org.json.JSONObject;

import java.util.ArrayList;

public class DeviceManagerActivity extends AppCompatActivity {

    private static final String TAG = "DeviceManagerActivity";

    private ArrayList<AdItem> mTopAdItems;
    private ArrayList<AdItem> mBottomItems;

    private EditText mDeviceNameView;
    private EditText mDeviceIdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manager);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制竖屏
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTopAdItems = AdManager.getTopAdList();
        mBottomItems = AdManager.getBottomList();

        initView();
    }

    private void initView() {
        AdView topView = (AdView) findViewById(R.id.top_ad);
        final AdView bottomView = (AdView) findViewById(R.id.botton_ad);

        mDeviceNameView = (EditText) findViewById(R.id.add_device_name);
        mDeviceIdView = (EditText) findViewById(R.id.add_device_id);

        topView.hideCloseButton();
        topView.setAdAdapter(new AdView.AdAdapter() {
            @Override
            public int getCount() {
                return mTopAdItems.size();
            }

            @Override
            public AdItem getItem(int index) {
                return mTopAdItems.get(index);
            }
        });

        bottomView.setAdAdapter(new AdView.AdAdapter() {
            @Override
            public int getCount() {
                return mBottomItems.size();
            }

            @Override
            public AdItem getItem(int index) {
                return mBottomItems.get(index);
            }
        });
        bottomView.setOnImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        bottomView.setOnImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = bottomView.getCurrentUrl();

                Intent intent = new Intent(DeviceManagerActivity.this, WebActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

        findViewById(R.id.add_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mDeviceNameView.getText().toString();
                String id = mDeviceIdView.getText().toString();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(id)) {
                    Toast.makeText(DeviceManagerActivity.this, "设备名称或ID为空", Toast.LENGTH_LONG).show();
                    return;
                }

                if (DeviceDataManager.isDeviceExist(id)) {
                    Toast.makeText(DeviceManagerActivity.this, "设备已存在", Toast.LENGTH_LONG).show();
                    return;
                }
                //DeviceDataManager.addDevice(name, id);
                addDevice(name, id);

            }
        });

    }

    private String buildParams(String name, String id) {
        // {"USER_NO":"5","MACHINE_ID":"111","MACHINE_TITLE":"设备"}
        JSONObject object = new JSONObject();
        try {
            object.put("USER_NO", Configs.userInfo.userNo);
            object.put("MACHINE_ID", id);
            object.put("MACHINE_TITLE", name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    private void addDevice(String name, String id) {
        // for test
        DeviceDataManager.addDevice(name, id);

        String postData = buildParams(name, id);
        if (Configs.DEBUG) {
            Log.d(TAG, "device data " + postData);
        }

        HttpUtil.post(Configs.ADD_MACHINE_URL, postData, new HttpConnection.Callback() {
            @Override
            public void onSucess(final byte[] data, final int size) {
                mDeviceNameView.post(new Runnable() {
                    @Override
                    public void run() {
                        int statusCode = 300;
                        String message = null;
                        String jsonStr = null;

                        if (data != null && size > 0) {
                            jsonStr = new String(data, 0, size);
                            try {
                                JSONObject object = new JSONObject(jsonStr);
                                String statusStr = object.getString("statusCode");
                                if (Configs.DEBUG) {
                                    Log.e(TAG, "statusCode is " + statusStr);
                                }
                                statusCode = Integer.parseInt(statusStr);
                                message = object.getString("message");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (statusCode == 200) {
                            Toast.makeText(DeviceManagerActivity.this, "添加成功", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(DeviceManagerActivity.this, "添加失败:" + statusCode + "," + message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onFailed(final int code) {
                mDeviceNameView.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DeviceManagerActivity.this, "网络连接失败:" + code, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
