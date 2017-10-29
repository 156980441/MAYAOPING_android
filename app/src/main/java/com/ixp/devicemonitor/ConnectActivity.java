package com.ixp.devicemonitor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ixp.util.StringUtils;

import java.util.Timer;
import java.util.TimerTask;

public class ConnectActivity extends BaseActivity {

    private final String TAG = "ConnectActivity";
    ImageView ivResult;
    TextView tvInfo;
    Button btnAp;
    Button btnCheck;
    private String ssid, password;

    private boolean isAPConfig;

    private int secondleft = 0;

    private Timer timer;

    private boolean isReceiveLink = false;

    private enum handler_key {
        //the tick time
        TICK_TIME,
        // the result ok
        CONFIG_SUCCESS,
        // the result false
        CONFIG_FALSE,

        CONFIG_FINISH,
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler_key key = handler_key.values()[msg.what];
            switch (key) {
                case TICK_TIME:
                    secondleft++;
                    Log.i("bear", "secondleft " + secondleft + " isReceiveLink " + isReceiveLink);
                    if (isReceiveLink) {
                        secondleft = 601;
                    }
                    if (secondleft > 600) {
                        timer.cancel();
                        ivResult.setSelected(isReceiveLink);
                        intoConfiger(false);
                        if (isReceiveLink) {
                            handler.sendEmptyMessage(handler_key.CONFIG_SUCCESS.ordinal());
                        } else {
                            handler.sendEmptyMessage(handler_key.CONFIG_FALSE.ordinal());
                        }
                    } else {
                    }

                    break;
                case CONFIG_SUCCESS:
                    tvInfo.setText(getResources().getString(R.string.configure_ok));

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(handler_key.CONFIG_FINISH.ordinal());
                        }
                    }, 8000);

                    break;
                case CONFIG_FALSE:
                    Toast.makeText(ConnectActivity.this, "配网失败", Toast.LENGTH_SHORT).show();
                    tvInfo.setText(getResources().getString(R.string.configure_error));
                    btnCheck.setVisibility(View.VISIBLE);
//                    btnAp.setVisibility(View.VISIBLE);
                    break;

                case CONFIG_FINISH:
                    //finish 前面的程序
//                    for (Activity activity : JActivityManager.getActivityStack()) {
//                        activity.finish();
//                    }
//                    IntentUtils.getInstance().startActivity(ConnectActivity.this, MainActivity.class);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);


        ivResult = (ImageView)findViewById(R.id.iv_result);
        tvInfo = (TextView) findViewById(R.id.tv_info);
        btnAp = (Button)findViewById(R.id.btn_ap);
        btnCheck = (Button) findViewById(R.id.btn_check);

        // 启用ActionBar图标导航功能
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        intoConfiger(true);
        openTimer();
        initIntent();

    }

    private void initIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            ssid = bundle.getString("ssid");
            password = bundle.getString("password");
            isAPConfig = bundle.getBoolean("isAPConfig", false);
            Log.e(TAG, "[Bundle]" + ssid + "," + password + "," + isAPConfig);
        }

        if (!StringUtils.isEmpty(ssid) && !StringUtils.isEmpty(password)) {
            if (isAPConfig) {
                doAPStart();
            } else {
                doSmartStart();
            }
        }
    }

    private void doAPStart() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Log.i("bear", "[APStart] " + "ssid:" + ssid + ",password:" + password);
//                udpHelper.APLink(ssid, password, new Callback<JsonObject>() {
//                    @Override
//                    public void onCompleted(Exception e, JsonObject jsonObject) {
//                        Log.i("bear", "[ap]=" + jsonObject);
//                        if (jsonObject != null) {
//                            isReceiveLink = true;
//                        }
//                    }
//                });
            }
        }.start();
    }

    private void doSmartStart() {
//        wifiTouch.start(ssid, password, mContext, 60);
//        udpHelper.receiveSmartLink(new Callback<JsonObject>() {
//            @Override
//            public void onCompleted(Exception e, JsonObject jsonObject) {
//                Log.i(TAG, "Configer " + jsonObject);
//                //配网过程收到很多数据
//                if (jsonObject != null) {
//                    isReceiveLink = true;
//                }
//            }
//        });

    }

    //配网倒计时，100ms为一个刻度，配网时间为1min.
    private void openTimer() {
        secondleft = 0;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(handler_key.TICK_TIME.ordinal());
            }
        }, 1000, 100);
    }

    private void intoConfiger(boolean b) {
        if (b) {
            ivResult.setVisibility(View.GONE);
            tvInfo.setText(getResources().getString(R.string.configure_info));
            btnAp.setVisibility(View.GONE);
            btnCheck.setVisibility(View.GONE);
        } else {
            ivResult.setVisibility(View.VISIBLE);
        }
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
