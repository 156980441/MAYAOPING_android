package com.ixp.devicemonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ixp.util.Configs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MachineSettingActivity extends AppCompatActivity {

    private EditText mNetworkSSID = null;
    private EditText mNetworkPassword = null;
    private EditText mServerAddress = null;
    private EditText mServerPort = null;
    private EditText mMachineId = null;
    private EditText mPhCofA = null;
    private EditText mPhCofB = null;
    private EditText mTdsCofA = null;
    private EditText mTdsCofB = null;
    private Button mSetButton = null;
    private Button mLoadButton = null;

    private Socket mSocket = null;
    private InputStream mInputStream = null;
    private OutputStream mOutputStream = null;

    private static final String TAG = "MachineSettingActivity";
    private static final String IP = "192.168.4.1";
    private static final int PORT = 8086;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_setting);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initSocket();
        initView();
    }

    private void initView() {
        mNetworkSSID = (EditText)findViewById(R.id.network_ssid);
        mNetworkPassword = (EditText)findViewById(R.id.network_password);
        mServerAddress = (EditText)findViewById(R.id.server_address);
        mServerPort = (EditText)findViewById(R.id.server_port);
        mMachineId = (EditText)findViewById(R.id.machine_id);
        mPhCofA = (EditText)findViewById(R.id.ph_cofa);
        mPhCofB = (EditText)findViewById(R.id.ph_cofb);
        mTdsCofA = (EditText)findViewById(R.id.tds_cofa);
        mTdsCofB = (EditText)findViewById(R.id.tds_cofb);

        mSetButton = (Button)findViewById(R.id.set_params);
        mLoadButton = (Button)findViewById(R.id.read_params);

        mSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSettings();
            }
        });

        mLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //updateSettings("&S,44,55,,,x,3,4,5,6!");
                loadSettings();
            }
        });
    }

    private void initSocket() {
        new ReadThread().start();
    }

    private String formatString(Object str) {
        return  str.toString() != null ? str.toString() : "";
    }

    private String formatFloat(Object obj) {
        String value = obj.toString();

        if (value == null)
            return "";
        int pos = value.indexOf(".");
        if (pos != -1) {
            int left = value.length() - pos - 1;
            if (left > 4) {
                return value.substring(0, value.length() - left + 4);
            }
        }
        return value;
    }

    private void setSettings() {
        final StringBuilder sb = new StringBuilder("&S,");
        sb.append(formatString(mNetworkSSID.getText())).append(",")
                .append(formatString(mNetworkPassword.getText())).append(",")
                .append(formatString(mServerAddress.getText())).append(",")
                .append(formatString(mServerPort.getText())).append(",")
                .append(formatString(mMachineId.getText())).append(",")
                .append(formatFloat(mPhCofA.getText())).append(",")
                .append(formatFloat(mPhCofB.getText())).append(",")
                .append(formatFloat(mTdsCofA.getText())).append(",")
                .append(formatFloat(mTdsCofB.getText()))
                .append("!");

        if (Configs.DEBUG) {
            Log.d(TAG, "[set]" + sb.toString());
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mOutputStream != null) {
                    try {
                        mOutputStream.write(sb.toString().getBytes());
                        mOutputStream.flush();

                        mLoadButton.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MachineSettingActivity.this, "设置成功", Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    mLoadButton.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MachineSettingActivity.this, "Socket创建失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void loadSettings() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mOutputStream != null) {
                    try {
                        mOutputStream.write("&G,!".getBytes());
                        mOutputStream.flush();
                        mLoadButton.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MachineSettingActivity.this, "发送指令成功", Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    mLoadButton.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MachineSettingActivity.this, "Socket创建失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void updateSettings(String params) {
        if (Configs.DEBUG) {
            Log.d(TAG, "[read] string is " + params);
        }
        if (!params.startsWith("&G,") || !params.endsWith("!")) {
            Toast.makeText(this, "读取数据错误", Toast.LENGTH_LONG).show();
        }

        int startIndex = 3;
        int endIndex = params.indexOf(",", startIndex);

        mNetworkSSID.setText(params.substring(startIndex, endIndex));

        startIndex = endIndex + 1;
        endIndex = params.indexOf(",", startIndex);
        mNetworkPassword.setText(params.substring(startIndex, endIndex));

        startIndex = endIndex + 1;
        endIndex = params.indexOf(",", startIndex);
        mServerAddress.setText(params.substring(startIndex, endIndex));

        startIndex = endIndex + 1;
        endIndex = params.indexOf(",", startIndex);
        mServerPort.setText(params.substring(startIndex, endIndex));

        startIndex = endIndex + 1;
        endIndex = params.indexOf(",", startIndex);
        mMachineId.setText(params.substring(startIndex, endIndex));

        startIndex = endIndex + 1;
        endIndex = params.indexOf(",", startIndex);
        mPhCofA.setText(params.substring(startIndex, endIndex));

        startIndex = endIndex + 1;
        endIndex = params.indexOf(",", startIndex);
        mPhCofB.setText(params.substring(startIndex, endIndex));

        startIndex = endIndex + 1;
        endIndex = params.indexOf(",", startIndex);
        mTdsCofA.setText(params.substring(startIndex, endIndex));

        startIndex = endIndex + 1;
        mTdsCofB.setText(params.substring(startIndex, params.length() - 1));
    }

    @Override
    protected void onDestroy() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {

            try {
                mSocket = new Socket(IP, PORT);

                mInputStream = mSocket.getInputStream();
                mOutputStream = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mInputStream != null) {
                byte buffer[] = new byte[512];

                try {
                    int readSize = 0;
                    while ((readSize = mInputStream.read(buffer)) > 0) {
                        final String string = new String(buffer, 0, readSize);
                        mLoadButton.post(new Runnable() {
                            @Override
                            public void run() {
                                updateSettings(string);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
