package com.ixp.devicemonitor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ixp.util.Configs;
import com.ixp.util.DeviceDataManager;
import com.ixp.util.HttpConnection;
import com.ixp.util.HttpUtil;

import org.json.JSONObject;

// 设备列表界面

public class DevicesActivity extends BaseActivity {

    // 设置 list
    private BaseAdapter mListAdapter;
    // Android 中处理点击事件的 OnClickListener 接口也是被定义在类 View 中的,为接口添加了个命名空间，明确了接口的所属，且免去了到处定义一堆.java文件
    private DeviceDataManager.OnDeviceListRefreshedListener mOnDeviceListRefreshListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListAdapter = new MyListAdapter(getLayoutInflater());
        this.mListView.setAdapter(mListAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDeviceDetailInfo(position);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DevicesActivity.this);
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

        refreshData();
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

    private void refreshData() {
        mOnDeviceListRefreshListener = new DeviceDataManager.OnDeviceListRefreshedListener() {
            @Override
            public void onRefreshed(boolean succ, int code) {
                if (succ) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 当我们需要 ListView 进行刷新的时候，我们需要调用 Adapter.notifyDataSetChanged() 来让界面刷新。
                            mListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        };
        // 监听数据变化，通知给 list，之后 list 更新数据
        DeviceDataManager.addOnDeviceListRefreshedListener(mOnDeviceListRefreshListener);
        DeviceDataManager.refreshDeviceList(false);
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

    private void showDeviceDetailInfo(int position) {
        Intent intent = new Intent();
        intent.setClass(DevicesActivity.this, DeviceDetailActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
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
                                Toast.makeText(DevicesActivity.this, "删除成功", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(DevicesActivity.this, "删除错误:" + message, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {

                        }

                        Toast.makeText(DevicesActivity.this, "删除成功", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailed(int code) {
                Toast.makeText(DevicesActivity.this, "删除失败,错误码:" + code, Toast.LENGTH_LONG).show();
            }
        });
    }
}
