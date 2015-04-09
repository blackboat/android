package com.vendormax.web.orderapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.vendormax.web.orderapp.api.OrderAPI;
import com.vendormax.web.orderapp.settings.NotificationReceiver;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;


public class AccountActivity extends Activity {

    private EditText etUserid, etAccountid;
    private Spinner spAccountid;

    public final static String SIGNOUT_MSG = "signout";
    public final static int REQUEST_ORDER = 0;
    public static Drawable logo_drawble;
    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        etUserid = (EditText) findViewById(R.id.account_edit_userid);
        etAccountid = (EditText) findViewById(R.id.account_edit_accountid);
        spAccountid = (Spinner) findViewById(R.id.account_spinner_accountid);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String userid = OrderAPI.user_id;
        String accountid = OrderAPI.customer_id;
//        userid = "139833";
//        accountid = "550";
        Log.d("account_ID", accountid);

        if (!userid.equals("null")) {
            etUserid.setText(userid);
            etUserid.setFocusable(false);
        } else {
            etUserid.setFocusable(true);
        }

        if (!accountid.equals("null")) {
            if (accountid.contains("/")) {
                spAccountid.setVisibility(View.VISIBLE);
                etAccountid.setVisibility(View.GONE);
                String[] ids = accountid.split("/");
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ids);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spAccountid.setAdapter(adapter);
            } else {
                spAccountid.setVisibility(View.GONE);
                etAccountid.setVisibility(View.VISIBLE);
                etAccountid.setText(accountid);
                etAccountid.setFocusable(false);
            }
        } else {
            etAccountid.setFocusable(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_account, menu);
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
                return true;
            case R.id.action_exit:
                AlertDialog.OnClickListener onClickListener = new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                        System.exit(0);
                    }
                };
                MainActivity.exitDialog(this, onClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ORDER) {
            if (resultCode == MainActivity.RESULT_EXIT) {
                finish();
                System.exit(0);
            }
        }
    }

    public void onSignout(View view) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("auth_token", OrderAPI.token);
        OrderAPI.postSignout(params, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                intent.putExtra(SIGNOUT_MSG, "Signed out successfully.");
                startActivity(intent);
                AccountActivity.this.finish();
            }
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.d("Fail", "dsf");
            }
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.d("Fail", response);
            }
        });
    }

    public void onOrder(View view) {
        OrderAPI.user_id = etUserid.getText().toString();
        if (etAccountid.getVisibility() == View.VISIBLE) {
            OrderAPI.customer_id = etAccountid.getText().toString();
        } else {
            OrderAPI.customer_id = spAccountid.getSelectedItem().toString();
        }
        HashMap<String, String> params = new HashMap<String, String>();

        params.put("auth_token", OrderAPI.token);
        params.put("user_id", OrderAPI.user_id);
        params.put("account_id", OrderAPI.customer_id);

        progressDialog = ProgressDialog.show(this, "Loading", "Please wait...", true);

        OrderAPI.postAccount(params, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                progressDialog.dismiss();
                try {
                    Log.d("Success", response.toString());
                    String isSuccess = response.getString("success");
                    if (!isSuccess.equals("true")) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Invalid user or account", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    OrderAPI.customer_name = response.getString("customer_name");
                    OrderAPI.user_name = response.getString("user_name");
                    OrderAPI.user_phone = response.getString("user_phone");
                    OrderAPI.logo_url = response.getString("logo_url");
                    new DownloadImageTask().execute(OrderAPI.logo_url);
                    Intent intent = new Intent(AccountActivity.this, OrderActivity.class);
                    startActivityForResult(intent, REQUEST_ORDER);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                progressDialog.dismiss();
                Log.d("Fail", "dsf");
            }
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                progressDialog.dismiss();
                Log.d("Fail", response);
            }
        });
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Drawable> {
        ImageView bmImage;

        public DownloadImageTask() { }

        @Override
        protected Drawable doInBackground(String... urls) {
            try {
                String urldisplay = urls[0];
                InputStream is = (InputStream) new URL(urldisplay).getContent();
                Drawable d = Drawable.createFromStream(is, "src name");
                return d;
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(Drawable result) {
            logo_drawble = result;
            progressDialog.dismiss();
        }
    }
}
