package com.ixp.devicemonitor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ixp.util.AdItem;
import com.ixp.util.AdManager;
import com.ixp.util.Configs;
import com.ixp.util.HttpConnection;
import com.ixp.util.HttpUtil;
import com.ixp.util.LocationUtil;

import org.json.JSONObject;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private ArrayList<AdItem> mTopAdItems;
    private ArrayList<AdItem> mBottomAdItems;

    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;

    private View mProgressView;
    private View mRegisterView;

    private RegisterTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AdManager.refreshAds(LocationUtil.getCity());

        mTopAdItems = AdManager.getTopAdList();
        mBottomAdItems = AdManager.getBottomList();

        initView();
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
                return mBottomAdItems.size();
            }

            @Override
            public AdItem getItem(int index) {
                return mBottomAdItems.get(index);
            }
        });

        bottomView.setOnImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = bottomView.getCurrentUrl();

                Intent intent = new Intent(RegisterActivity.this, WebActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

        mUsernameView = (EditText)findViewById(R.id.register_name);
        mPasswordView = (EditText)findViewById(R.id.register_password);
        mConfirmPasswordView = (EditText)findViewById(R.id.register_password_again);

        findViewById(R.id.register_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameView.getText().toString();
                String password = mPasswordView.getText().toString();
                String confirmPassword = mConfirmPasswordView.getText().toString();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, "用户名或密码为空", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "密码不一致", Toast.LENGTH_LONG).show();
                    return;
                }
                register(username, password);
            }
        });

        mProgressView = findViewById(R.id.login_progress);
        mRegisterView = findViewById(R.id.register_form);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    private String buildParams(String username, String password) {
        return "{\"USER_NAME\":\"" + username + "\",\"PASSWORD\":\"" + password + "\"}";
    }

    private void register(String username, String password) {
        showProgress(true);

        String postData = buildParams(username, password);
        HttpUtil.post(Configs.ADD_USER_URL, postData, new HttpConnection.Callback() {
            @Override
            public void onSucess(final byte[] data, final int size) {
                mRegisterView.post(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                        int statusCode = 300;

                        if (data != null && size > 0) {
                            String json = new String(data, 0, size);
                            try {
                                JSONObject object = new JSONObject(json);
                                String statusStr = object.getString("statusCode");
                                if (Configs.DEBUG) {
                                    Log.e(TAG, "statusCode is " + statusStr);
                                }
                                statusCode = Integer.parseInt(statusStr);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (statusCode == 200) {
                            registerSuccess();
                        } else {
                            mUsernameView.requestFocus();
                            Toast.makeText(RegisterActivity.this, "注册失败:" + statusCode, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onFailed(final int code) {
                mRegisterView.post(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                        Toast.makeText(RegisterActivity.this, "网络连接错误:" + code, Toast.LENGTH_LONG).show();

                    }
                });
            }
        });

    }

    private void registerSuccess() {
        Intent intent = new Intent();
        intent.setClass(RegisterActivity.this, DeviceActivity.class);
        startActivity(intent);
        finish();
    }


    public class RegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        RegisterTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                registerSuccess();
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "登陆失败", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
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
