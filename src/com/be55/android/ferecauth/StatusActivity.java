package com.be55.android.ferecauth;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity to show the status
 * 
 */
public class StatusActivity extends Activity {
	private static final int MENU_ID_CONFIG = (Menu.FIRST + 1);
	private static final int MENU_ID_ABOUT = (Menu.FIRST + 2);

	private Intent authServiceIntent;

	SharedPreferences sharedPreferences;

	private ListItem itemStatus;
	private ListItem itemManually;
	private StatusListItemAdapter itemAdapter;

	Button btnStart;
	Button btnStop;
	ListView listViewStatus;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Load preferences
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		//
		// Show welcome dialog
		// (when this is the first time to use this app)
		//
		if (sharedPreferences.getBoolean("lunch_isfirsttime", true)) {

			AlertDialog.Builder welcomeDialogBuilder = new AlertDialog.Builder(
					this);
			welcomeDialogBuilder.setCancelable(false).setTitle(
					getString(R.string.lunch_title)).setMessage(
					getString(R.string.lunch_message)).setPositiveButton(
					getString(R.string.lunch_gopref),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intentConfig = new Intent(getBaseContext(),
									ConfigActivity.class);
							startActivity(intentConfig);
						}
					}).setNegativeButton(getString(R.string.lunch_later),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			AlertDialog welcomeDialog = welcomeDialogBuilder.create();
			welcomeDialog.show();

			// don't show this message again
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(this);
			Editor ed = sp.edit();
			ed.putBoolean("lunch_isfirsttime", false);
			ed.commit();
		}

		//
		// Create listview's items
		//
		listViewStatus = (ListView) findViewById(R.id.listViewStatus);

		ArrayList<ListItem> dataList = new ArrayList<ListItem>();

		// item for status message
		itemStatus = new ListItem();
		itemStatus.setName(getString(R.string.status_item_status_name));
		itemStatus
				.setDescription(getString(R.string.status_item_status_description_stop));
		dataList.add(itemStatus);

		// item for manual auth
		itemManually = new ListItem() {
			@Override
			public void onClick() {
				FerecAuthManager authMan = new FerecAuthManager(
						getApplicationContext());
				authMan.doAuth();
			}
		};
		itemManually.setName(getString(R.string.status_item_manually_name));
		itemManually
				.setDescription(getString(R.string.status_item_manually_description));
		dataList.add(itemManually);

		itemAdapter = new StatusListItemAdapter(this,
				android.R.layout.simple_list_item_2, dataList);

		listViewStatus.setAdapter(itemAdapter);

		//
		// Start service
		//
		authServiceIntent = new Intent(getApplicationContext(),
				FerecAuthService.class);

		if (sharedPreferences.getBoolean("exec_autoauth", false)) {
			startService(authServiceIntent);
			itemStatus
					.setDescription(getString(R.string.status_item_status_description_start));
			itemAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// Toggle status message
		if (FerecAuthService.isRunning) {
			itemStatus
					.setDescription(getString(R.string.status_item_status_description_start));
		} else {
			itemStatus
					.setDescription(getString(R.string.status_item_status_description_stop));
		}
		itemAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, MENU_ID_CONFIG, Menu.NONE,
				R.string.opmenu_preferences).setIcon(
				android.R.drawable.ic_menu_preferences);
		menu
				.add(Menu.NONE, MENU_ID_ABOUT, Menu.NONE,
						R.string.opmenu_aboutthis).setIcon(
						android.R.drawable.ic_menu_info_details);
		return ret;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case MENU_ID_CONFIG:
			// intent to preference activity
			Intent intentConfig = new Intent(getBaseContext(),
					ConfigActivity.class);
			startActivity(intentConfig);
			break;
		case MENU_ID_ABOUT:
			// show own informations
			String ownVersionName = "";

			try {
				ownVersionName = getPackageManager().getPackageInfo(
						this.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			LayoutInflater aboutDialogInflater = (LayoutInflater) this
					.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = aboutDialogInflater.inflate(R.layout.about,
					(ViewGroup) findViewById(R.id.linearLayoutAboutDialogRoot));
			AlertDialog.Builder aboutDialogBuilder = new AlertDialog.Builder(
					this);
			aboutDialogBuilder.setCancelable(true).setView(layout)
					.setPositiveButton("close",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
			((TextView) layout.findViewById(R.id.textViewVersionName)).setText("Version " + ownVersionName);
			((TextView) layout.findViewById(R.id.textViewWebSite)).setMovementMethod(LinkMovementMethod.getInstance());
			((TextView) layout.findViewById(R.id.textViewGithub)).setMovementMethod(LinkMovementMethod.getInstance());
			
			AlertDialog aboutDialog = aboutDialogBuilder.create();
			aboutDialog.show();
			break;
		}

		return ret;
	}

	/**
	 * Adapter for StatusListView
	 */
	private class StatusListItemAdapter extends ArrayAdapter<ListItem> {
		private ArrayList<ListItem> items;
		private LayoutInflater inflater;

		StatusListItemAdapter(Context context, int textViewResourceId,
				ArrayList<ListItem> dataList) {
			super(context, textViewResourceId, dataList);
			this.items = dataList;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View view = convertView;

			if (view == null) {
				view = inflater.inflate(android.R.layout.simple_list_item_2,
						null);
				view
						.setBackgroundResource(android.R.drawable.list_selector_background);
			}

			final ListItem item = (ListItem) items.get(position);

			TextView text1 = (TextView) view.findViewById(android.R.id.text1);
			TextView text2 = (TextView) view.findViewById(android.R.id.text2);

			text1.setText(item.name);
			text2.setText(item.description);

			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					item.onClick();
				}
			});

			return view;
		}
	}

	/**
	 * Items for StatusListView
	 */
	private class ListItem {
		String name;
		String description;

		public ListItem() {
			name = new String();
			description = new String();
		}

		/**
		 * Set first row's value
		 * 
		 * @param value
		 *            first row's value
		 */
		public void setName(String value) {
			this.name = value;
		}

		/**
		 * Set second row's value
		 * 
		 * @param value
		 *            second row's value
		 */
		public void setDescription(String value) {
			this.description = value;
		}

		/**
		 * Event handler that for the item is clicked
		 */
		public void onClick() {
			// Nothing to do
			// It will be overridden
		}
	}
}