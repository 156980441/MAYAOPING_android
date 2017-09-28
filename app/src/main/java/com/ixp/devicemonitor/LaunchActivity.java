package com.ixp.devicemonitor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.ixp.util.AdManager;
import com.ixp.util.DataManager;
import com.ixp.util.DeviceDataManager;
import com.ixp.util.DirectoryManager;
import com.ixp.util.LocationUtil;
import com.ixp.util.StartUpPageManager;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        getSupportActionBar().hide();

        init();

        showStartPage();

        findViewById(R.id.back).postDelayed(new Runnable() {
            @Override
            public void run() {
                showLogin();
                finish();
            }
        }, 2000);
    }

    private void showStartPage() {
        ImageView view = (ImageView)findViewById(R.id.back);

        String path = StartUpPageManager.getImagePath(this);

        view.setScaleType(ImageView.ScaleType.FIT_XY);
        if (path == null) {
            view.setBackgroundResource(StartUpPageManager.getDefaultImage());
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            view.setBackgroundDrawable(new BitmapDrawable(bitmap));
        }
    }

    private void init() {
        DirectoryManager.init(this.getApplicationContext());
        DataManager.init(this.getApplicationContext());
        LocationUtil.init(this.getApplicationContext());
        AdManager.init(this.getApplicationContext());
        DeviceDataManager.init(this.getApplicationContext());
    }

    private void showLogin() {
        Intent intent = new Intent();
        intent.setClass(LaunchActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
