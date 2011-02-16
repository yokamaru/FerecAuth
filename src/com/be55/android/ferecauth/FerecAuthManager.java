package com.be55.android.ferecauth;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

public class FerecAuthManager {
	Context context;
	SharedPreferences sharedPreferences;

	public FerecAuthManager(Context contextArg) {
		context = contextArg;
	}

	public boolean isTargetNetwork() {
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());

		String ssid = sharedPreferences.getString("auth_ssid", "");

		if (this.getSSID().equals(ssid)) {
			return true;
		}

		return false;
	}

	public boolean needAuth() {
		if (this.testConnection()) {
			return false;
		}

		return true;
	}

	public void doAuth() {
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());

		String authUri = sharedPreferences.getString("auth_uri", "");
		String authUsername = sharedPreferences.getString("auth_username", "");
		String authPassword = sharedPreferences.getString("auth_password", "");
		String authHidden = sharedPreferences.getString("auth_hidden", "");
		
		this.doAuth(authUri, authUsername, authPassword, authHidden);
	}

	public void doAuth(String authUri, String authUsername,
			String authPassword, String authHidden) {
		String postArg = "user=" + authUsername + "&pass=" + authPassword + "&"
				+ authHidden;

		try {
			HttpPost method = new HttpPost(authUri);
			DefaultHttpClient client = new DefaultHttpClient();

			StringEntity paramEntity = new StringEntity(postArg);
			paramEntity.setChunked(false);
			paramEntity.setContentType("application/x-www-form-urlencoded");
			method.setEntity(paramEntity);

			client.execute(method);
		} catch (Exception e) {

		}
	}

	protected String getSSID() {
		WifiManager wifiMan;
		WifiInfo wifiInfo;

		wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifiInfo = wifiMan.getConnectionInfo();

		return wifiInfo.getSSID();
	}

	protected boolean testConnection() {

		try {
			String postString = new SimpleDateFormat("yyyyMMddHHmmss")
					.format(new Date());
			HttpGet method = new HttpGet(
					"http://ferecauth.appspot.com/echo?s=" + postString);

			DefaultHttpClient client = new DefaultHttpClient();
			method.setHeader("Connection", "Keep-Alive");

			HttpResponse response = client.execute(method);

			String retval = EntityUtils.toString(response.getEntity(), "UTF-8");

			if (retval.equals(postString)) {
				return true;
			}
		} catch (Exception e) {

		}

		return false;
	}
}
