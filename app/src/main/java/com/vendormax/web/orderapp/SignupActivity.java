package com.vendormax.web.orderapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.vendormax.web.orderapp.api.OrderAPI;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SignupActivity extends Activity {

    private EditText etEmail;
    private EditText etPassword;
    private EditText etRetype;
    private Spinner spinType;
    private EditText etUserid;
    private Button btnPlus;
    private Button btnMinus;
    private LinearLayout layoutAccounts;

    private String user_role;
    private ArrayList<EditText> accountids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getActionBar().setDisplayHomeAsUpEnabled(true);

//        Point size = new Point();
//        getWindowManager().getDefaultDisplay().getSize(size);
//        final LinearLayout layout = (LinearLayout) findViewById(R.id.signup_layout);
//        layout.setMinimumHeight(size.y);

        etEmail = (EditText) findViewById(R.id.signup_edit_email);
        etPassword = (EditText) findViewById(R.id.signup_edit_password);
        etRetype = (EditText) findViewById(R.id.signup_edit_retype);
        etUserid = (EditText) findViewById(R.id.signup_edit_userid);
        btnPlus = (Button) findViewById(R.id.signup_button_plus);
        btnMinus = (Button) findViewById(R.id.signup_button_minus);
        layoutAccounts = (LinearLayout) findViewById(R.id.signup_accounts_layout);
        accountids = new ArrayList<EditText>();

        EditText editText = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 20);
        editText.setLayoutParams(lp);
        editText.setHint(R.string.signup_edit_accountid);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        accountids.add(editText);
        layoutAccounts.addView(editText);

        spinType = (Spinner) findViewById(R.id.signup_spinner_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.signup_spinner_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinType.setAdapter(adapter);
        spinType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String type = spinType.getSelectedItem().toString();
                String[] types = getResources().getStringArray(R.array.signup_spinner_type);
                String standard = types[0], distributor = types[1], admin = types[2];
                if (type.equals(standard)) {
                    user_role = "standard";
                    etUserid.setVisibility(View.VISIBLE);
                    layoutAccounts.setVisibility(View.VISIBLE);
                    btnPlus.setVisibility(View.VISIBLE);
                    btnMinus.setVisibility(View.VISIBLE);
                } else if (type.equals(distributor)) {
                    user_role = "distributor";
                    etUserid.setVisibility(View.VISIBLE);
                    layoutAccounts.setVisibility(View.GONE);
                    btnPlus.setVisibility(View.GONE);
                    btnMinus.setVisibility(View.GONE);
                } else if (type.equals(admin)) {
                    user_role = "admin";
                    etUserid.setText("");
                    etUserid.setVisibility(View.GONE);
                    layoutAccounts.setVisibility(View.GONE);
                    btnPlus.setVisibility(View.GONE);
                    btnMinus.setVisibility(View.GONE);
                } else {
                    return;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_signup, menu);
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
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_exit:
                AlertDialog.OnClickListener onClickListener = new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setResult(MainActivity.RESULT_EXIT);
                        finish();
                    }
                };
                MainActivity.exitDialog(this, onClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onSignup(View view) {
        String email = etEmail.getText().toString(),
            password = etPassword.getText().toString(),
            retype = etRetype.getText().toString(),
            userid = etUserid.getText().toString();
        if (email.isEmpty()) {
            Toast.makeText(this, "Fill email", Toast.LENGTH_LONG).show();
            return;
        }
        if (!MainActivity.isValidEmail(email)) {
            Toast.makeText(getApplicationContext(), "Invalid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Fill password", Toast.LENGTH_LONG).show();
            return;
        }
        if (retype.isEmpty()) {
            Toast.makeText(this, "Retype password", Toast.LENGTH_LONG).show();
            return;
        }
        if (!password.equals(retype)) {
            Toast.makeText(this, "Reenter password", Toast.LENGTH_LONG).show();
            return;
        }
        if (user_role != "admin" && userid.isEmpty()) {
            Toast.makeText(this, "Fill UserID", Toast.LENGTH_LONG).show();
            return;
        }
        if (user_role == "standard" && isAllAccountEmpty()) {
            Toast.makeText(this, "Fill at least one AccountID", Toast.LENGTH_LONG).show();
            return;
        }
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("email", email);
        params.put("password", password);
        params.put("retype", retype);
        params.put("role", user_role);
        params.put("userid", userid);

        List<Map<String, String>> listOfMaps = new ArrayList<Map<String, String>>();
        int count = accountids.size();
        for (int i = 0; i < count; i++) {
            Map<String, String> item = new HashMap<String, String>();
            String accountid = accountids.get(i).getText().toString();
            if (!accountid.isEmpty()) {
                item.put("value", accountid);
            }
            listOfMaps.add(item);
        }
        params.put("accountid", listOfMaps);

        final ProgressDialog progressDialog = ProgressDialog.show(this, "Sign up", "Please wait for a moment...", true);

        OrderAPI.postSignup(params, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressDialog.dismiss();
                Log.d("Success", response.toString());
                try {
                    String isSuccess = response.getString("success");
                    if (!isSuccess.equals("true")) {
                        Toast.makeText(SignupActivity.this, response.getString("notice"), Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(SignupActivity.this, "Successful sign up!  " + response.getString("notice"), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                progressDialog.dismiss();
                Log.d("Fail", response.toString());
            }

            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                progressDialog.dismiss();
                Log.d("Fail", response);
            }
        });
    }

    public void onAddAccount(View view) {
        EditText editText = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 20);
        editText.setLayoutParams(lp);
        editText.setHint(R.string.signup_edit_accountid);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        accountids.add(editText);
        layoutAccounts.addView(editText);
    }

    public void onRemoveAccount(View view) {
        int index = accountids.size();
        if (index > 1) {
            layoutAccounts.removeView(accountids.get(index - 1));
            accountids.remove(index - 1);
        }
    }

    private boolean isAllAccountEmpty() {
        for (int i = 0; i < accountids.size(); i++) {
            String accountid = accountids.get(i).getText().toString();
            if (!accountid.isEmpty())
                return false;
        }
        return true;
    }
}
