package com.ixp.devicemonitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
                } else if (position == 1) {
                    intent.setClass(MineActivity.this, MachineSettingActivity.class);
                } else if (position == 2) {
                    intent.setClass(MineActivity.this, PasswordSettingActivity.class);
                } else if (position == 3) {

                } else if (position == 4) {

                } else if (position == 5) {

                }
                startActivity(intent);
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
