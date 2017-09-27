package com.ixp.devicemonitor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ixp.util.AdItem;
import com.ixp.util.AdManager;
import com.ixp.util.Configs;
import com.ixp.util.DeviceDataManager;
import com.ixp.util.HttpConnection;
import com.ixp.util.HttpUtil;
import com.ixp.util.LocationUtil;

import org.json.JSONObject;

import java.util.ArrayList;

public class DeviceActivity extends AppCompatActivity {

    private AdView mTopAdView;
    private AdView mBottomAdView;
    private ListView mListView;
    private BaseAdapter mListAdapter;
    private RadioGroup RG;
    private RadioButton RB1, RB2, RB3, RB4, RB5, RB6;

    private ArrayList<AdItem> mTopAdItems;
    private ArrayList<AdItem> mBottomAdItems;

    private DeviceDataManager.OnDeviceListRefreshedListener mOnDeviceListRefreshListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        // TODO: test code
        // DeviceDataManager.generateTestData();
        AdManager.refreshAds(LocationUtil.getCity());

        mTopAdItems = AdManager.getTopAdList();
        mBottomAdItems = AdManager.getBottomList();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
        refreshData();
    }

    private void refreshData() {
        mOnDeviceListRefreshListener = new DeviceDataManager.OnDeviceListRefreshedListener() {
            @Override
            public void onRefreshed(boolean succ, int code) {
                if (succ) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        };
        DeviceDataManager.addOnDeviceListRefreshedListener(mOnDeviceListRefreshListener);
        DeviceDataManager.refreshDeviceList(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DeviceDataManager.removeOnDeviceListRefreshedListener(mOnDeviceListRefreshListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mListAdapter != null) {
            mListAdapter.notifyDataSetChanged();
        }
    }

    private RadioGroup.OnCheckedChangeListener ChangeRadioGroup = new
            RadioGroup.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId)
                {

                    Intent myintent = new Intent();

                    // TODO Auto-generated method stub
                    if(checkedId==RB1.getId()){
                        Toast.makeText(DeviceActivity.this, RB1.getText()+"被选择", Toast.LENGTH_LONG).show();
                    }
                    else if(checkedId==RB1.getId()){
                        myintent.setClass(DeviceActivity.this, ForumActivity.class);
                    }
                    else if(checkedId==RB3.getId()){
                        myintent.setClass(DeviceActivity.this, MineActivity.class);
                    }
                    DeviceActivity.this.startActivity(myintent);
                    DeviceActivity.this.finish();
                }
            };

    private void initView() {
        mTopAdView = (AdView) findViewById(R.id.top_ad);
        mBottomAdView = (AdView) findViewById(R.id.botton_ad);
        mListView = (ListView) findViewById(R.id.list);

        RG = (RadioGroup) findViewById(R.id.tabbar);
        RB1 = (RadioButton) findViewById(R.id.devices);
        RB2 = (RadioButton) findViewById(R.id.forum);
        RB3 = (RadioButton) findViewById(R.id.mine);

        RG.setOnCheckedChangeListener(ChangeRadioGroup);


        mTopAdView.hideCloseButton();
        mTopAdView.setAdAdapter(new AdView.AdAdapter() {
            @Override
            public int getCount() {
                return mTopAdItems.size();
            }

            @Override
            public AdItem getItem(int index) {
                return mTopAdItems.get(index);
            }
        });

        mBottomAdView.setAdAdapter(new AdView.AdAdapter() {
            @Override
            public int getCount() {
                return mBottomAdItems.size();
            }

            @Override
            public AdItem getItem(int index) {
                return mBottomAdItems.get(index);
            }
        });

        mBottomAdView.setOnImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Configs.DEBUG) {
                    Log.e("webview", "load url is " + mBottomAdView.getCurrentUrl());
                }
                String url = mBottomAdView.getCurrentUrl();

                Intent intent = new Intent(DeviceActivity.this, WebActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

//        findViewById(R.id.add_device).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openAddDeviceActivity();
//            }
//        });

        mListAdapter = new MyListAdapter(getLayoutInflater());
        mListView.setAdapter(mListAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDeviceDetailInfo(position);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DeviceActivity.this);
                builder.setMessage("确认删除?").setTitle("提示")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteDeviceInfo(position);
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return true;
            }
        });

    }

    private void deleteDeviceInfo(final int position) {
        String machineId = DeviceDataManager.getDevices().get(position).deviceId;
        String deleteUrl = Configs.DELETE_MACHINE_URL + "/" + Configs.userInfo.userNo + "/" + machineId;

        HttpUtil.get(deleteUrl, new HttpConnection.Callback() {
            @Override
            public void onSucess(final byte[] data, final int size) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String string = new String(data, 0, size);
                        String status = null;
                        String message = null;
                        try {
                            JSONObject obj = new JSONObject(string);
                            status = obj.getString("statusCode");
                            message = obj.getString("message");

                            if (status.equals("200")) {
                                DeviceDataManager.getDevices().remove(position);
                                Toast.makeText(DeviceActivity.this, "删除成功", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(DeviceActivity.this, "删除错误:" + message, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {

                        }

                        Toast.makeText(DeviceActivity.this, "删除成功", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailed(int code) {
                Toast.makeText(DeviceActivity.this, "删除失败,错误码:" + code, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openAddDeviceActivity() {
        Intent intent = new Intent();
        intent.setClass(DeviceActivity.this, DeviceManagerActivity.class);
        startActivity(intent);
    }

    private void showDeviceDetailInfo(int position) {
        Intent intent = new Intent();
        intent.setClass(DeviceActivity.this, DeviceDetailActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    private class MyListAdapter extends BaseAdapter {

        private LayoutInflater mInflater = null;

        public MyListAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        @Override
        public int getCount() {
            return DeviceDataManager.getDevices().size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = mInflater.inflate(R.layout.list_item, null);
            TextView textView = (TextView) view.findViewById(R.id.item_text);

            textView.setText(DeviceDataManager.getDevices().get(position).name);

            return view;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }
    }

    private void showExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示").setMessage("确认是否退出?")
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showExit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        showExit();
    }
}
