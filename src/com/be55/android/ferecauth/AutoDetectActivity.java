package com.be55.android.ferecauth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class AutoDetectActivity extends Activity {
	AlertDialog.Builder alertCautionBuilder;
	AlertDialog alertCaution;

	TextView textViewLog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.autodetect);
		
		

		textViewLog = (TextView) findViewById(R.id.textViewLog);

		LayoutInflater alertCautionInflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = alertCautionInflater.inflate(R.layout.autodetect_caution,
				(ViewGroup) findViewById(R.id.linearLayoutAlertCautionRoot));
		alertCautionBuilder = new AlertDialog.Builder(this);
		alertCautionBuilder.setView(layout).setCancelable(false)
				.setPositiveButton(
						getString(R.string.dialog_autodetect_caution_start),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								doDetect();
							}
						}).setNegativeButton(
						getString(R.string.dialog_autodetect_caution_cancel),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
								finish();
							}
						});
		alertCaution = alertCautionBuilder.create();
		alertCaution.show();
	}

	/**
	 * such like Spaghetti...
	 */
	private void doDetect() {
		final int DETECT_SUCCESS = 0;
		final int DETECT_FAIL_WIFI = 1;
		final int DETECT_FAIL_SSID = 2;
		final int DETECT_FAIL_WEB_CONNECTIVITY = 3;
		final int DETECT_FAIL_AUTH = 4;
		final int DETECT_FAIL_UNKNOWN = 99;

		final AlertDialog.Builder alertDetectBuilder;
		final Handler detectHandler;

		alertDetectBuilder = new AlertDialog.Builder(this);
		alertDetectBuilder.setCancelable(false).setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(getApplicationContext(),
								ConfigActivity.class);
						startActivity(intent);
						finish();
					}
				});

		detectHandler = new Handler() {
			public void handleMessage(Message msg) {
				AlertDialog alertDetectSuccess, alertDetectFail;

				switch (msg.what) {
				case DETECT_SUCCESS:
					alertDetectBuilder
							.setMessage(getString(R.string.dialog_autodetect_finish_success));
					alertDetectSuccess = alertDetectBuilder.create();
					alertDetectSuccess.show();
					break;
				case DETECT_FAIL_WIFI:
					alertDetectBuilder
							.setMessage(getString(R.string.dialog_autodetect_finish_fail_msgbase) + "\n"
									+ getString(R.string.dialog_autodetect_finish_fail_wifi));
					alertDetectFail = alertDetectBuilder.create();
					alertDetectFail.show();
					break;
				case DETECT_FAIL_SSID:
					alertDetectBuilder
							.setMessage(getString(R.string.dialog_autodetect_finish_fail_msgbase) + "\n"
									+ getString(R.string.dialog_autodetect_finish_fail_ssid));
					alertDetectFail = alertDetectBuilder.create();
					alertDetectFail.show();
					break;
				case DETECT_FAIL_WEB_CONNECTIVITY:
					alertDetectBuilder
							.setMessage(getString(R.string.dialog_autodetect_finish_fail_msgbase) + "\n"
									+ getString(R.string.dialog_autodetect_finish_fail_web_connectivity));
					alertDetectFail = alertDetectBuilder.create();
					alertDetectFail.show();
					break;
				case DETECT_FAIL_AUTH:
					alertDetectBuilder
							.setMessage(getString(R.string.dialog_autodetect_finish_fail_msgbase) + "\n"
									+ getString(R.string.dialog_autodetect_finish_fail_auth));
					alertDetectFail = alertDetectBuilder.create();
					alertDetectFail.show();
					break;
				case DETECT_FAIL_UNKNOWN:
				default:
					alertDetectBuilder
							.setMessage(getString(R.string.dialog_autodetect_finish_fail_msgbase) + "\n"
									+ getString(R.string.dialog_autodetect_finish_fail_unknown));
					alertDetectFail = alertDetectBuilder.create();
					alertDetectFail.show();
					break;
				}
			}
		};

		try {
			final AutoDetectManager autoDetectMan = new AutoDetectManager(
					getApplicationContext());

			// Check WiFi status
			textViewLog.append("Checking WiFi status...\n");
			if (autoDetectMan.checkWifiStatus()) {
				textViewLog.append(" => connected");
			} else {
				textViewLog.append(" => disconnected");
				throw new AutoDetectException(DETECT_FAIL_WIFI);
			}
			textViewLog.append("\n");

			// Get SSID
			textViewLog.append("Checking SSID...\n");
			String ssid = autoDetectMan.getSSID();
			textViewLog.append(" => " + ssid);
			if (ssid.equals("")) {
				throw new AutoDetectException(DETECT_FAIL_SSID);
			}
			textViewLog.append("\n");

			// Check Internet connectivity
			textViewLog.append("Checking web connectivity...\n");
			if (autoDetectMan.checkWebConnectivity()) {
				textViewLog.append(" => Yes (means not good...)");
				throw new AutoDetectException(DETECT_FAIL_WEB_CONNECTIVITY);
			} else {
				textViewLog.append(" => No (means okey!)");
			}
			textViewLog.append("\n");

			// Get start page
			textViewLog.append("Getting frame's uri...\n");
			textViewLog.append(" => " + autoDetectMan.getStartPage());
			textViewLog.append("\n");

			// Get form uri
			textViewLog.append("Getting form uri...\n");
			textViewLog.append(" => " + autoDetectMan.getFormUri());
			textViewLog.append("\n");

			// Get form information
			// Nothing to do.
			autoDetectMan.getFormInformation();

			// Input account information
			LayoutInflater alertAccountInflater = (LayoutInflater) this
					.getSystemService(LAYOUT_INFLATER_SERVICE);
			final View layout = alertAccountInflater
					.inflate(
							R.layout.autodetect_account,
							(ViewGroup) findViewById(R.id.linearLayoutAlertAccountRoot));
			AlertDialog.Builder alertAccountBuilder = new AlertDialog.Builder(
					this);
			alertAccountBuilder
					.setView(layout)
					.setCancelable(false)
					.setPositiveButton(
							getString(R.string.dialog_autodetect_account_dotest),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String userName = ((EditText) layout
											.findViewById(R.id.editTextUserName))
											.getText().toString();
									String password = ((EditText) layout
											.findViewById(R.id.editTextPassword))
											.getText().toString();

									// Do authorizeb
									textViewLog.append("Username : ");
									textViewLog.append(userName);
									textViewLog.append("\n");
									textViewLog
											.append("Password : (invisible)");
									textViewLog.append("\n");

									dialog.cancel(); // not effective.

									if (autoDetectMan.testAuth(userName,
											password)) {
										// Save new preferences
										autoDetectMan.savePrefs();
										
										// Restart service
										Intent authServiceIntent = new Intent(getApplicationContext(),
												FerecAuthService.class);
										stopService(authServiceIntent);
										startService(authServiceIntent);
										
										detectHandler
												.sendEmptyMessage(DETECT_SUCCESS);
									} else {
										detectHandler
												.sendEmptyMessage(DETECT_FAIL_AUTH);
									}
								}
							});
			AlertDialog alertAccount = alertAccountBuilder.create();
			alertAccount.show();
		} catch (AutoDetectException e) {
			detectHandler.sendEmptyMessage(e.getErrNo());
		}
	}

	private class AutoDetectException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int errno;

		public AutoDetectException(int errno) {
			this.errno = errno;
		}

		public int getErrNo() {
			return this.errno;
		}
	}
}
