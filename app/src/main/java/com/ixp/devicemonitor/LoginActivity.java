package com.ixp.devicemonitor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ixp.util.Configs;
import com.ixp.util.DataManager;
import com.ixp.util.HttpConnection;
import com.ixp.util.HttpUtil;
import com.ixp.util.UserInfo;

import org.json.JSONObject;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";


    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.register_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        findViewById(R.id.sign_in_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        tryAutoLogin();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_settings), MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startSettingActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startSettingActivity() {
        Intent intent = new Intent(LoginActivity.this, MachineSettingActivity.class);
        startActivity(intent);
    }

    private void tryAutoLogin() {
        String username = DataManager.getString(KEY_USERNAME);
        String password = DataManager.getString(KEY_PASSWORD);

        if (username != null && password != null) {
            mUsernameView.setText(username);
            mPasswordView.setText(password);
            showProgress(true);
            startLogin(username, password);
        }
    }

    private void register() {
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void loginSuccess() {
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, DevicesActivity.class);
        startActivity(intent);
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            DataManager.storeString(KEY_USERNAME, username
            );
            DataManager.storeString(KEY_PASSWORD, password);
            startLogin(username, password);
            //mAuthTask = new UserLoginTask(username, password);
            //mAuthTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private String buildParams(String username, String password) {
        return "{\"USER_NAME\":\"" + username + "\",\"PASSWORD\":\"" + password + "\"}";
    }

    private void startLogin(String username, String password) {
        if (Configs.DEBUG) {
            Log.d("LoginActivity", buildParams(username, password));
        }

        HttpUtil.post(Configs.LOGIN_URL, buildParams(username, password), new HttpConnection.Callback() {
            @Override
            public void onSucess(final byte[] data, final int size) {
                mLoginFormView.post(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                        int statusCode = 300;
                        String jsonStr = null;

                        if (data != null && size > 0) {
                            jsonStr = new String(data, 0, size);
                            try {
                                JSONObject object = new JSONObject(jsonStr);
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
                            UserInfo info = UserInfo.createFromJson(jsonStr);
                            info.logout = 0;
                            if (Configs.DEBUG) {
                                Log.d(TAG, info.toString());
                            }
                            Configs.userInfo = info;
                            loginSuccess();
                            finish();
                        } else {
                            // mPasswordView.setError(getString(R.string.error_incorrect_password));
                            mUsernameView.setError(getString(R.string.error_invalid_username));
                            mUsernameView.requestFocus();
                            Toast.makeText(LoginActivity.this, "登陆失败:" + statusCode, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onFailed(final int code) {
                mLoginFormView.post(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                        Toast.makeText(LoginActivity.this, "网络连接错误:" + code, Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
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
            showProgress(false);

            if (success) {
                loginSuccess();
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

