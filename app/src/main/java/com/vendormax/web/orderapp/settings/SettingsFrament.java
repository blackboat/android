package com.vendormax.web.orderapp.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.vendormax.web.orderapp.R;

import java.util.Arrays;

public class SettingsFrament extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof Callback) {
            mCallback = (Callback) activity;
        } else {
            throw new IllegalStateException("Owner must implement Callback interface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.daily_reminder);

        Preference preference;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        for (int i = 0; i < SettingsActivity.keys.length; i++) {
            preference = findPreference(SettingsActivity.keys[i]);
            preference.setOnPreferenceClickListener(this);

            boolean isChecked = sharedPrefs.getBoolean(SettingsActivity.keys[i], false);
            if (isChecked) {
                int hour = SettingsActivity.getPreferencesValue(String.valueOf(i) + "_hour", getActivity());
                int min = SettingsActivity.getPreferencesValue(String.valueOf(i) + "_min", getActivity());
                String time = hour + ":" + min;
                preference.setSummary(time);
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean isChecked = sharedPrefs.getBoolean(preference.getKey(), false);
        if (!isChecked) {
            preference.setSummary("");
            return false;
        }
        mCallback.onNestedPreferenceSelected(Arrays.asList(SettingsActivity.keys).indexOf(preference.getKey()));
        return false;
    }

    public interface Callback{
        public void onNestedPreferenceSelected(int key);
    }
}
