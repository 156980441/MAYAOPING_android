package com.ixp.devicemonitor;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ixp.util.Configs;

public class MineActivity extends BaseActivity {

    private static final String[] strs = new String[]{
            "添加设备", "Wifi 配置", "修改密码", "注销", "关于"
    };

    private BaseAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListAdapter = new MineListAdapter(getLayoutInflater());
        this.mListView.setAdapter(mListAdapter);
        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent();
                if (position == 0) {
                    intent.setClass(MineActivity.this, DeviceManagerActivity.class);
                    startActivity(intent);
                } else if (position == 1) {
//                    intent.setClass(MineActivity.this, MachineSettingActivity.class);
                    intent.setClass(MineActivity.this, WifiSetting.class);
                    startActivity(intent);
                } else if (position == 2) {
                    intent.setClass(MineActivity.this, PasswordSettingActivity.class);
                    startActivity(intent);
                } else if (position == 3) {

                    final AlertDialog.Builder normalDialog = new AlertDialog.Builder(MineActivity.this);
                    normalDialog.setTitle("用户注销");
                    normalDialog.setMessage("你确定要注销用户吗？");
                    normalDialog.setPositiveButton("注销",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Configs.userInfo.logout = 1;
                                    Intent myIntent = new Intent();
                                    myIntent.setClass(MineActivity.this, LoginActivity.class);
                                    MineActivity.this.startActivity(myIntent);
                                    MineActivity.this.finish();
                                }
                            });
                    normalDialog.setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //...To-do
                                }
                            });
                    normalDialog.show();

                } else if (position == 4) {

                    final AlertDialog.Builder normalDialog = new AlertDialog.Builder(MineActivity.this);
                    normalDialog.setTitle("版本");
                    normalDialog.setMessage("1.0");
                    normalDialog.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //...To-do
                                }
                            });
                    normalDialog.show();

                }
            }
        });
    }

    private class MineListAdapter extends BaseAdapter {

        private LayoutInflater mInflater = null;

        public MineListAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        @Override
        public int getCount() {
            return strs.length;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = mInflater.inflate(R.layout.list_item, null);
            TextView textView = (TextView) view.findViewById(R.id.item_text);

            textView.setText(strs[position]);

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
}
