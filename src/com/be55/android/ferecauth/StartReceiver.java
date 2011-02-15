package com.be55.android.ferecauth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		Intent authServiceIntent = new Intent(context.getApplicationContext(),
				FerecAuthService.class);
		if (sharedPreferences.getBoolean("exec_autoauth", false)) {
			context.startService(authServiceIntent);
		}
	}
}
