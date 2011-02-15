package com.be55.android.ferecauth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

public class WifiConnectReceiver extends BroadcastReceiver {
	final private int STATE_CONNECTED = 1;
	final private int STATE_DISCONNECTED = 2;
	final private int STATE_UNHANDLED = -1;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (this.getState(intent) == this.STATE_CONNECTED) {
			Log.i("FerecAuth", "WiFi connected.");
			
			FerecAuthManager authMan = new FerecAuthManager(context);
			
			if (authMan.isTargetNetwork() && authMan.needAuth()) {
				Log.i("FerecAuth", "Trying to authorize.");
				authMan.doAuth();
			}
			
		} else if (this.getState(intent) == this.STATE_DISCONNECTED) {
			// Stop regular tasks
			Log.i("FerecAuth", "WiFi disconnected.");	
		}
	}

	private int getState(Intent intent) {
		Bundle extras = intent.getExtras();

		if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
			try {
				NetworkInfo info = extras
						.getParcelable(WifiManager.EXTRA_NETWORK_INFO);
				NetworkInfo.State state = info.getState();
				if (state.equals(NetworkInfo.State.CONNECTED)) {
					// Switched from disconnected to connected
					return this.STATE_CONNECTED;
				} else if (state.equals(NetworkInfo.State.DISCONNECTED)) {
					// Switched from connected to disconnected
					return this.STATE_DISCONNECTED;
				}
			} catch (Exception e) // FIXME: Overcatch
			{
				e.printStackTrace();
			}
		} else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent
				.getAction())) {
			int currentState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
					WifiManager.WIFI_STATE_UNKNOWN);
			int previousState = intent.getIntExtra(
					WifiManager.EXTRA_PREVIOUS_WIFI_STATE,
					WifiManager.WIFI_STATE_UNKNOWN);
			if ((previousState != WifiManager.WIFI_STATE_DISABLED)
					&& (currentState == WifiManager.WIFI_STATE_DISABLED)) {
				// Switched from connected to disconnected
				return this.STATE_DISCONNECTED;
			}
		}

		// Unhandled state
		// e.g. disabling, enabling
		// TODO: handle these states
		return this.STATE_UNHANDLED;
	}
}
