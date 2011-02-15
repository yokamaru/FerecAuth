package com.be55.android.ferecauth;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

public class AutoDetectManager extends FerecAuthManager {
	Context context;
	
	private boolean wifiStatus = false;
	private String ssid = "";
	private boolean isWebConnected = true;
	private String startPageUri = "";
	private String formUri = "";
	private String formHiddenField = "";
	private String username = "";
	private String password = "";
	
	public AutoDetectManager(Context contextArg) {
		super(contextArg);

		context = contextArg;
	}

	/**
	 * Get Wifi status
	 * 
	 * @return returns true if wifi connected
	 */
	public boolean checkWifiStatus() {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		int state = wifiManager.getWifiState();

		if (state == WifiManager.WIFI_STATE_ENABLED) {
			this.wifiStatus = true;
		}

		return this.wifiStatus;
	}

	/**
	 * Get WiFi SSID
	 * 
	 * @return SSID
	 */
	public String getSSID() {
		try {
			this.ssid = super.getSSID();
		} catch (Exception e) {
			
		}
		
		return this.ssid;
	}

	/**
	 * Check Web connectivity
	 * 
	 * @return Web connectivity
	 */
	public boolean checkWebConnectivity() {
		this.isWebConnected = super.testConnection(); 
		
		return this.isWebConnected;
	}

	public String getStartPage() {
		String currentUri = "";

		try {
			String postString = new SimpleDateFormat("yyyyMMddHHmmss")
					.format(new Date());
			HttpGet method = new HttpGet(
					"http://2ndlab.skr.jp/ferecauth/echo.php?s=" + postString);
			HttpContext httpContext = new BasicHttpContext();

			DefaultHttpClient client = new DefaultHttpClient();
			method.setHeader("Connection", "Keep-Alive");

			client.execute(method, httpContext);

			HttpUriRequest currentReq = (HttpUriRequest) httpContext
					.getAttribute(ExecutionContext.HTTP_REQUEST);
			HttpHost currentHost = (HttpHost) httpContext
					.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			currentUri = currentHost.toURI() + currentReq.getURI();
		} catch (Exception e) {

		}
		
		this.startPageUri = currentUri;

		return this.startPageUri;
	}
	
	public String getFormUri() {
		this.formUri = this.startPageUri + "login.gsp";
		
		return this.formUri;
	}

	/**
	 * 
	 * 
	 * @see http://android.g.hatena.ne.jp/keigoi/20100211/1265892976
	 */
	public void getFormInformation() {
		this.formHiddenField = "login=1";
	}

	public boolean testAuth(String username, String password){
		this.doAuth(this.formUri, username, password, this.formHiddenField);
		
		this.username = username;
		this.password = password;
		
		// Bad idea...
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return this.testConnection();
	}

	public void savePrefs() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor ed = sp.edit();
		
		ed.putString("auth_ssid", this.ssid);
		ed.putString("auth_username", this.username);
		ed.putString("auth_password", this.password);
		ed.putString("auth_uri", this.formUri);
		ed.putString("auth_hidden", this.formHiddenField);
		
		ed.commit();
	}
}
