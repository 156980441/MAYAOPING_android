package com.ixp.devicemonitor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ixp.util.AdItem;
import com.ixp.util.AdManager;
import com.ixp.util.Configs;
import com.ixp.util.DeviceDataManager;
import com.ixp.util.DeviceInfo;
import com.ixp.util.HttpConnection;
import com.ixp.util.HttpUtil;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class DeviceDetailActivity extends AppCompatActivity {

    private static final String TAG = "DeviceDetailActivity";

    private ArrayList<AdItem> mTopAdItems;
    private ArrayList<AdItem> mBottomItems;

    private Button mModifyButton = null;

    private int mPosition = 0;
    private DeviceDataManager.OnDeviceListRefreshedListener mOnDeviceListRefreshListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTopAdItems = AdManager.getTopAdList();
        mBottomItems = AdManager.getBottomList();

        initView();

        mOnDeviceListRefreshListener = new DeviceDataManager.OnDeviceListRefreshedListener() {
            @Override
            public void onRefreshed(boolean succ, int code) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showDeviceInfo(mPosition);
                    }
                });
            }
        };
        DeviceDataManager.addOnDeviceListRefreshedListener(mOnDeviceListRefreshListener);
    }

    private void initView() {
        AdView topView = (AdView) findViewById(R.id.top_ad);
        final AdView bottomView = (AdView) findViewById(R.id.botton_ad);

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
                String url = bottomView.getCurrentUrl();

                Intent intent = new Intent(DeviceDetailActivity.this, WebActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

        mModifyButton = (Button) findViewById(R.id.modify_name);
        mModifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(DeviceDetailActivity.this);

                new AlertDialog.Builder(DeviceDetailActivity.this)
                        .setView(editText)
                        .setTitle("输入名称")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String text = editText.getText().toString();
                                final DeviceInfo info = DeviceDataManager.getDevices().get(mPosition);

                                if (!TextUtils.isEmpty(text)) {
                                    String encodeText = URLEncoder.encode(text);
                                    if (Configs.DEBUG) {
                                        Log.d(TAG, "text: " + text + ", encodeText: " + encodeText);
                                    }
                                    HttpUtil.get(Configs.SET_MACHINE_NAME + info.deviceId + "/" + encodeText, new HttpConnection.Callback() {
                                        @Override
                                        public void onSucess(byte[] data, int size) {
                                            final String string = new String(data, 0, size);

                                            try {
                                                JSONObject object = new JSONObject(string);
                                                final String status = object.getString("code");

                                                mModifyButton.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if ("200".equals(status)) {
                                                            Toast.makeText(DeviceDetailActivity.this, "更改成功", Toast.LENGTH_LONG).show();
                                                            info.name = text;
                                                            showDeviceInfo(mPosition);
                                                        } else {
                                                            Toast.makeText(DeviceDetailActivity.this, "更改失败,错误码:" + status, Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailed(int code) {
                                            mModifyButton.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(DeviceDetailActivity.this, "网络错误", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (intent != null) {
            mPosition = intent.getIntExtra("position", 0);
            showDeviceInfo(mPosition);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DeviceDataManager.removeOnDeviceListRefreshedListener(mOnDeviceListRefreshListener);
    }

    private void showDeviceInfo(final int position) {
        final DeviceInfo info = DeviceDataManager.getDevices().get(position);

        ((TextView) findViewById(R.id.detail_device_name)).setText(" " + info.name);
        ((TextView) findViewById(R.id.detail_device_id)).setText(" " + info.deviceId);
        ((TextView) findViewById(R.id.detail_device_temperature)).setText(" " + info.temperature);
        ((TextView) findViewById(R.id.detail_device_tds)).setText(" " + info.tds);
        ((TextView) findViewById(R.id.detail_device_ph)).setText(" " + info.ph);
        ((TextView) findViewById(R.id.detail_update_date)).setText(" " + info.updateTime);

        Switch sw = (Switch) findViewById(R.id.detail_device_state);
        sw.setOnCheckedChangeListener(null);
        sw.setChecked(info.state);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (Configs.DEBUG) {
                    Log.d("switch", "switch state " + info.state);
                }
                HttpUtil.get(Configs.SET_MACHINE_STATE + info.deviceId + "/" + (isChecked ? 1 : 0), new HttpConnection.Callback() {
                    @Override
                    public void onSucess(byte[] data, int size) {
                        final String string = new String(data, 0, size);

                        try {
                            JSONObject object = new JSONObject(string);
                            final String status = object.getString("code");

                            mModifyButton.post(new Runnable() {
                                @Override
                                public void run() {
                                    if ("200".equals(status)) {
                                        info.state = isChecked;
                                        Toast.makeText(DeviceDetailActivity.this, "更改状态成功", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(DeviceDetailActivity.this, "更改状态失败,错误码:" + status, Toast.LENGTH_LONG).show();
                                    }
                                    showDeviceInfo(position);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        mModifyButton.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DeviceDetailActivity.this, "网络连接失败", Toast.LENGTH_LONG).show();
                            }
                        });
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
