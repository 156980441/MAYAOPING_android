package com.ixp.devicemonitor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ixp.util.NetworkUtils;

public class WifiSetting extends AppCompatActivity {

    /**
     * Context.
     */
    protected Context mContext;
    private final String TAG = "WifiSetting";

    private boolean isAPConfig = true;
    private boolean prompt = false;
    private boolean checkUpResult = true;

    TextView tvStatus;
    EditText wifiSsid;
    EditText wifiPwd;
    Button btnNext;

    CheckBox cbLaws;

    private String ssid;

    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制竖屏
        mContext = getApplication();
        tvStatus = (TextView) findViewById(R.id.tv_status);
        wifiSsid = (EditText) findViewById(R.id.wifi_ssid);
        wifiPwd = (EditText) findViewById(R.id.wifi_pwd);
        cbLaws = (CheckBox) findViewById(R.id.cbLaws);
        btnNext = (Button) findViewById(R.id.btn_next);
        initConfigure();
        initView();
    }

    private void initView() {

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prompt = true;
                if (checkUpResult == false) {
                    checkUpResult = true;
                }
                doConfigure();
            }
        });

        wifiPwd.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        //密码可视
        cbLaws.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String psw = wifiPwd.getText().toString();

                if (isChecked) {
                    wifiPwd.setInputType(0x90);
                } else {
                    wifiPwd.setInputType(0x81);
                }
                wifiPwd.setSelection(psw.length());
            }
        });
        if (!NetworkUtils.whetherNetWorkIsWifi(mContext)) {
            Toast.makeText(this, "WIFI尚未打开", Toast.LENGTH_SHORT).show();
            tvStatus.setText("WIFI尚未打开");
            initDialog();
        }
    }

    //初始化配网操作，如果SSID包含（10.10.3),则为AP配网，wifi账号可编辑。否则为SmartLink,获取当前ssid，wifi账号不可编辑
    private void initConfigure() {

        String strSSID = NetworkUtils.getCurentWifiSSID(mContext);
        String strIp = NetworkUtils.getCurentWifiIp(mContext);
        Log.i(TAG, "strSSID" + strSSID + ",strIp" + strIp);
        if (strIp.contains("10.10.3")) {
            isAPConfig = true;
            tvStatus.setText("AP配网");
            Toast.makeText(mContext, "AP模式", Toast.LENGTH_LONG).show();
            wifiSsid.setFocusableInTouchMode(true);
        } else {
            isAPConfig = false;
            tvStatus.setText("智能配网");
            wifiSsid.setText(strSSID);//设置当前的wifi
            wifiSsid.setFocusableInTouchMode(false);
        }


    }

    private void initDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new
                        Intent(Settings.ACTION_WIFI_SETTINGS)); //直接进入手机中的wifi网络设置界面
            }
        };
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("WIFI 尚未打开");
        dialogBuilder.setPositiveButton("去连接WiFi", dialogClickListener);
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void doConfigure() {
        initConfigure();
        ssid = wifiSsid.getText().toString().trim();
        password = wifiPwd.getText().toString().trim();

        if (!NetworkUtils.whetherNetWorkIsWifi(mContext)&& prompt) {
            Toast.makeText(this, "WIFI尚未打开", Toast.LENGTH_SHORT).show();
            checkUpResult = false;
            prompt = false;
        }
        if (ssid.equals("") && prompt) {
            Toast.makeText(this, "Wifi账号不能为空", Toast.LENGTH_SHORT).show();
            checkUpResult = false;
            prompt = false;
        }
        if (password.equals("") && prompt) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            checkUpResult = false;
            prompt = false;
        }

        if (checkUpResult) {
            //进入配网过程
            Intent intent = new Intent(WifiSetting.this, ConnectActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("ssid", ssid);
            bundle.putString("password", password);
            bundle.putBoolean("isAPConfig", isAPConfig);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }

    }

    // 返回上一页
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
