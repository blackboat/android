package com.vendormax.web.orderapp;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.vendormax.web.orderapp.api.OrderAPI;
import com.vendormax.web.orderapp.settings.NotificationReceiver;
import com.vendormax.web.orderapp.settings.SettingsActivity;

import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;


public class OrderActivity extends FragmentActivity {

    private FragmentTabHost tabHost;
    private static final int RESULT_SETTINGS = 1;
    public static AboutDialogClass aboutDialogClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        setTitle(OrderAPI.customer_name);

        tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        tabHost.addTab(
                tabHost.newTabSpec("tab1").setIndicator("Orders", null),
                TabOrderFragment.class, null);
        tabHost.addTab(
                tabHost.newTabSpec("tab2").setIndicator("Products", null),
                TabProductFragment.class, null);

//        showAboutDlg(OrderAPI.user_name, OrderAPI.user_phone, OrderAPI.logo_url);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_order, menu);
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
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, RESULT_SETTINGS);
                return true;
            case android.R.id.home:
                if (OrderAPI.user_role.equals("distributor")) {
                    OrderAPI.customer_id = "null";
                } else if (OrderAPI.user_role.equals("standard")) {
                    OrderAPI.user_id = "null";
                    OrderAPI.customer_id = OrderAPI.customer_ids;
                } else {
                    OrderAPI.user_id = "null";
                    OrderAPI.customer_id = "null";
                }
                OrderActivity.this.finish();
                return true;
            case R.id.action_detail:
                showAboutDlg(OrderAPI.user_name, OrderAPI.user_phone, OrderAPI.logo_url);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                setNotificationTime();
                break;
        }
    }

    public void setNotificationTime() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 0; i < SettingsActivity.keys.length; i++) {
            boolean isChecked = sharedPrefs.getBoolean(SettingsActivity.keys[i], false);
            if (isChecked) {
                int hour = SettingsActivity.getPreferencesValue(String.valueOf(i) + "_hour", this);
                int min = SettingsActivity.getPreferencesValue(String.valueOf(i) + "_min", this);
                Log.d("enable", String.valueOf(i) + " " + String.valueOf(hour) + " " + String.valueOf(min));
                enablePending(i + 1, hour, min);
            } else {
                disablePending(i);
                Log.d("disable", String.valueOf(i));
            }
        }
    }

    public void disablePending(int id) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_NO_CREATE);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public void enablePending(int day_of_week, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());
        int cur_day_of_week = calendar.get(Calendar.DAY_OF_WEEK);
        int cur_hour = calendar.get(Calendar.HOUR_OF_DAY);
        int cur_min = calendar.get(Calendar.MINUTE);
        int days_diff = day_of_week - cur_day_of_week;
        if (days_diff < 0) {
            days_diff += 7;
        } else if (days_diff == 0) {
            int hours_diff = hour - cur_hour;
            if (hours_diff < 0) {
                days_diff += 7;
            } else if (hours_diff == 0) {
                int mins_diff = minute - cur_min;
                if (mins_diff <= 0) {
                    days_diff += 7;
                }
            }
        }
        int cur_day_of_month = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, cur_day_of_month + days_diff);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(this, NotificationReceiver.class);
        int id = day_of_week - 1;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000*60*60*24*7 , pendingIntent);

    }

    public void showAboutDlg(String name, String phone, String logo) {
        aboutDialogClass = new AboutDialogClass(this, name, phone, logo);
        aboutDialogClass.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        aboutDialogClass.show();
    }

    public class AboutDialogClass extends Dialog {

        public String name;
        public String phone;
        public String logo;

        public AboutDialogClass(Context context, String name, String phone, String logo) {
            super(context);
            this.name = name;
            this.phone = phone;
            this.logo = logo;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_about);
            TextView tvName = (TextView) findViewById(R.id.dlg_about_textName);
            TextView tvPhone = (TextView) findViewById(R.id.dlg_about_textPhone);
            ImageView ivLogo = (ImageView) findViewById(R.id.dlg_about_imageLogo);
            tvName.setText(this.name);
            SpannableString content = new SpannableString(this.phone);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            tvPhone.setTextColor(getResources().getColor(R.color.blue));
            tvPhone.setText(content);
            final String phone = tvPhone.getText().toString();
            tvPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + phone));
                    OrderActivity.this.startActivity(intent);
                }
            });

            if (AccountActivity.logo_drawble != null) {
                ivLogo.setBackground(AccountActivity.logo_drawble);
            }
//            new DownloadImageTask(ivLogo).execute(this.logo);
        }

        private Drawable DownloadImage(String url) {
            try {
                InputStream is = (InputStream) new URL(url).getContent();
                Drawable d = Drawable.createFromStream(is, "src name");
                return d;
            } catch (Exception e) {
                return null;
            }
        }

//        private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
//            ImageView bmImage;
//
//            public DownloadImageTask(ImageView bmImage) {
//                this.bmImage = bmImage;
//            }
//
//            @Override
//            protected Bitmap doInBackground(String... urls) {
//                String urldisplay = urls[0];
//                Bitmap mIcon11 = null;
//                try {
//                    InputStream in = new java.net.URL(urldisplay).openStream();
//                    mIcon11 = BitmapFactory.decodeStream(in);
//                } catch (Exception e) {
//                    Log.e("Error", e.getMessage());
//                    e.printStackTrace();
//                }
//                return mIcon11;
//            }
//
//            protected void onPostExecute(Bitmap result) {
//                bmImage.setImageBitmap(result);
//            }
//        }
    }
}
