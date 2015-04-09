package com.vendormax.web.orderapp.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.TimePicker;

import java.util.Calendar;

public class SettingsActivity extends Activity implements SettingsFrament.Callback {

    public static String[] keys = {"pref_sun", "pref_mon", "pref_tue", "pref_wed", "pref_thu", "pref_fri", "pref_sat"};
    public SettingsFrament mFragment;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mFragment = new SettingsFrament();
        getFragmentManager().beginTransaction()
                .add(android.R.id.content, mFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getFragmentManager().getBackStackEntryCount() == 0) {
                    super.onBackPressed();
                } else {
                    getFragmentManager().popBackStack();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNestedPreferenceSelected(int key) {
//        getFragmentManager().beginTransaction()
//                .replace(android.R.id.content, TimeSettingsFragment.newInstance(), "NESTED")
//                .addToBackStack("NESTED")
//                .commit();
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        final String hour_key = String.valueOf(key) + "_hour";
        final String min_key = String.valueOf(key) + "_min";
        final Context context = this;
        final int key_fin = key;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                setPreferencesValue(hour_key, selectedHour, context);
                setPreferencesValue(min_key, selectedMinute, context);
                String time = selectedHour + ":" + selectedMinute;
                setSummary(key_fin, time);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (getSummary(key_fin).isEmpty()) {
                    setSummary(key_fin, "");
                }
            }
        });
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void setSummary(int key, String time) {
        Preference preference = mFragment.findPreference(keys[key]);
        preference.setSummary(time);
        if (time.isEmpty()) {
            ((CheckBoxPreference)preference).setChecked(false);
        }
    }

    public String getSummary(int key) {
        Preference preference = mFragment.findPreference(keys[key]);
        if (preference.getSummary() == null)
            return "";
        return preference.getSummary().toString();
    }

    public static void setPreferencesValue(String key, int value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getPreferencesValue(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, 0);
    }
}
