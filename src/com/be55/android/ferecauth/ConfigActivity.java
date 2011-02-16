package com.be55.android.ferecauth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class ConfigActivity extends PreferenceActivity {
	private Intent authServiceIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);

		CheckBoxPreference exec_autoauth = (CheckBoxPreference) findPreference("exec_autoauth");

		authServiceIntent = new Intent(getApplicationContext(),
				FerecAuthService.class);

		exec_autoauth
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {

						if (((Boolean) newValue).booleanValue()) {
							// Start service
							startService(authServiceIntent);
						} else {
							// Stop service
							stopService(authServiceIntent);
						}

						return true;
					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();

		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(listener);
	}

	private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {

			if (!key.equals("exec_autoauth")) {
				if (sharedPreferences.getBoolean("exec_autoauth", false)) {
					// Restart service
					stopService(authServiceIntent);
					startService(authServiceIntent);
				}
			}
		}
	};
}
