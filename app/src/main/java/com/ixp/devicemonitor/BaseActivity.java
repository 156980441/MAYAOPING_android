package com.ixp.devicemonitor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ixp.util.AdItem;
import com.ixp.util.AdManager;
import com.ixp.util.Configs;
import com.ixp.util.LocationUtil;

import java.util.ArrayList;

public class BaseActivity extends AppCompatActivity {

    private AdView mTopAdView;
    private AdView mBottomAdView;
    protected ListView mListView;
    private RadioGroup mTabbar;
    private RadioButton mDeviceBt, mForumBt, mMineBt;

    private ArrayList<AdItem> mTopAdItems;
    private ArrayList<AdItem> mBottomAdItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // TODO: test code
        // DeviceDataManager.generateTestData();
        AdManager.refreshAds(LocationUtil.getCity());

        mTopAdItems = AdManager.getTopAdList();
        mBottomAdItems = AdManager.getBottomList();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();

    }

    private RadioGroup.OnCheckedChangeListener ChangeRadioGroup = new
            RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    Intent myintent = new Intent();
                    // TODO Auto-generated method stub
                    if (checkedId == mDeviceBt.getId()) {
                        myintent.setClass(BaseActivity.this, DevicesActivity.class);
                    } else if (checkedId == mForumBt.getId()) {
                        myintent.setClass(BaseActivity.this, ForumActivity.class);
                    } else if (checkedId == mMineBt.getId()) {
                        myintent.setClass(BaseActivity.this, MineActivity.class);
                    }
                    BaseActivity.this.startActivity(myintent);
                    BaseActivity.this.finish();
                }
            };

    private void initView() {

        mTopAdView = (AdView) findViewById(R.id.top_ad);
        mBottomAdView = (AdView) findViewById(R.id.botton_ad);
        mListView = (ListView) findViewById(R.id.list);
        mTabbar = (RadioGroup) findViewById(R.id.tabbar);
        mDeviceBt = (RadioButton) findViewById(R.id.devices);
        mForumBt = (RadioButton) findViewById(R.id.forum);
        mMineBt = (RadioButton) findViewById(R.id.mine);

        mTabbar.setOnCheckedChangeListener(ChangeRadioGroup);

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

                Intent intent = new Intent(BaseActivity.this, WebActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
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
