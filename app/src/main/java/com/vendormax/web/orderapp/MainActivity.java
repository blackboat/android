package com.vendormax.web.orderapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.vendormax.web.orderapp.api.OrderAPI;
import com.vendormax.web.orderapp.settings.NotificationReceiver;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class MainActivity extends Activity {

    private EditText etEmail, etPassword;
    private Button btnSignin;
    private CheckBox cbRemember;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;

    private static final int REQUEST_SIGNUP = 0;
    public static final int RESULT_EXIT = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String signout_msg = intent.getStringExtra(AccountActivity.SIGNOUT_MSG);
        setContentView(R.layout.activity_main);

        etEmail = (EditText) findViewById(R.id.login_edit_email);
        etPassword = (EditText) findViewById(R.id.login_edit_password);
        btnSignin = (Button) findViewById(R.id.button_signin);
        cbRemember = (CheckBox) findViewById(R.id.login_check_remember);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            try {
                String str_date = loginPreferences.getString("date", "");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
                Date current_date = Calendar.getInstance().getTime(), login_date = str_date.isEmpty()?current_date:sdf.parse(str_date);
                long diff_days = dateDifference(login_date, current_date);
                if (diff_days >= 30) {
                    cbRemember.setChecked(false);
                } else {
                    etEmail.setText(loginPreferences.getString("email", ""));
                    etPassword.setText(loginPreferences.getString("password", ""));
                    cbRemember.setChecked(true);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (signout_msg != null) {
            Toast.makeText(getApplicationContext(), "Sign out successfully.", Toast.LENGTH_SHORT).show();
        }
//        intent = new Intent(this, AccountActivity.class);
//        startActivity(intent);
    }

    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnSignin.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                ComponentName component=new ComponentName(MainActivity.this, NotificationReceiver.class);
                return true;
            case R.id.action_exit:
                AlertDialog.OnClickListener onClickListener = new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                        System.exit(0);
                    }
                };
                exitDialog(this, onClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_EXIT) {
                finish();
                System.exit(0);
            }
        }
    }

    public void onSignin(View view) {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (cbRemember.isChecked()) {
            loginPrefsEditor.putBoolean("saveLogin", true);
            loginPrefsEditor.putString("email", email);
            loginPrefsEditor.putString("password", password);
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
            String str_date = sdf.format(calendar.getTime());
            loginPrefsEditor.putString("date", str_date);
            loginPrefsEditor.commit();
        } else {
            loginPrefsEditor.clear();
            loginPrefsEditor.commit();
        }

        if (!isValidEmail(email)) {
            Toast.makeText(getApplicationContext(), "Invalid email", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, String> params = new HashMap<String, String>();

        params.put("email", email);
        params.put("password", password);
        OrderAPI.postAuth(params, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d("Success", response.toString());
                    String isSuccess = response.getString("success");
                    if (!isSuccess.equals("true")) {
                        Log.d("isSuccess", isSuccess);
                        Toast.makeText(getApplicationContext(), response.getString("notice"), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    btnSignin.setEnabled(false);
                    OrderAPI.token = response.getString("auth_token");
                    OrderAPI.user_role = response.getString("user_role");
                    OrderAPI.user_id = response.getString("user_id");
                    try {
                        JSONArray customers = response.getJSONArray("customer_id");
                        String ids = customers.getString(0);
                        for (int i = 1; i < customers.length(); i++) {
                            ids = ids + "/" + customers.getString(i);
                        }
                        OrderAPI.customer_id = ids;
                        OrderAPI.customer_ids = ids;
                    } catch (JSONException e) {
                        OrderAPI.customer_id = response.getString("customer_id");
                    }

                    Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                    startActivity(intent);
                    MainActivity.this.finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Toast.makeText(getApplicationContext(), "Invalid email", Toast.LENGTH_SHORT).show();
                Log.d("Fail", "dsf");
            }
        });
    }

    public void onSignup(View view) {
        Intent intent = new Intent(MainActivity.this, SignupActivity.class);
        startActivityForResult(intent, REQUEST_SIGNUP);
    }

    public static boolean isValidEmail(String target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public long dateDifference(Date startDate, Date endDate){
        long different = endDate.getTime() - startDate.getTime();   //milliseconds
        long daysInMilli = 1000 * 60 * 60 * 24;
        long elapsedDays = different / daysInMilli;
        return elapsedDays;
    }

    public static void exitDialog(Context context, AlertDialog.OnClickListener onClickListener) {
        AlertDialog alert = new AlertDialog.Builder(context).create();
        alert.setTitle("Exit");
        alert.setMessage("Do you really want to exit?");
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", onClickListener);
        alert.show();
    }
}
