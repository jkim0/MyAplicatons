package com.loyid.grammarbook;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
	private static final String TAG = "SettingsActivity";
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = false;
	
	private static final int REQUEST_FILE_SELECT_CODE = 0;
	
	private static final int MSG_LOAD_DATA_FROM_FILE = 0;
	private static final int MSG_EXPORT_DATA_TO_FILE = 1;
	
	private MessageHandler mHandler = null;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public Intent onBuildStartFragmentIntent(String fragmentName, Bundle args,
			int titleRes, int shortTitleRes) {
		// TODO Auto-generated method stub
		Intent intent = super.onBuildStartFragmentIntent(fragmentName, args, titleRes,
				shortTitleRes);
		intent.setClass(this, GrammarPreferenceActivity.class);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mHandler = new MessageHandler();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'General' preferences.
		addPreferencesFromResource(R.xml.pref_general);

		// Add 'Grammar edit' preferences, and a corresponding header.
		PreferenceCategory fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_label_grammar_edit);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_grammar_edit);

		// Add 'Test' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_label_test);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_test);
		
		Preference loadData = findPreference("load_data");
		Preference exportData = findPreference("export_data");
		Preference reset = findPreference("reset");
		
		loadData.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				reloadDataFromFile();
				return true;
			}
		});
		
		exportData.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				exportDataToFile();
				return true;
			}
		});
		
		reset.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				resetData();
				return true;
			}
		});
		
		bindPreferenceSummaryToValue(findPreference("max_meaning_count"));
		bindPreferenceSummaryToValue(findPreference("test_count"));
		bindPreferenceSummaryToValue(findPreference("test_example_count"));
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	/** {@inheritDoc} */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);
			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}

	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);
			
			Preference loadData = findPreference("load_data");
			Preference exportData = findPreference("export_data");
			Preference reset = findPreference("reset");
			
			loadData.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					//reloadDataFromFile();
					return false;
				}
			});
			
			exportData.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					//exportDataFromFile();
					return false;
				}
			});
			
			reset.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					// TODO Auto-generated method stub
					return false;
				}
			});
		}
	}

	/**
	 * This fragment shows grammar edit preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GrammarEditPreferenceFragment extends
			PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_grammar_edit);
			
			bindPreferenceSummaryToValue(findPreference("max_meaning_count"));
		}
	}

	/**
	 * This fragment shows test preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class TestPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_test);
			
			bindPreferenceSummaryToValue(findPreference("test_count"));
			bindPreferenceSummaryToValue(findPreference("test_example_count"));
		}
	}
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	protected boolean isValidFragment(String fragmentName) {
		Log.e(TAG, "isValidFragment fragmentName = " + fragmentName);
		if ( GeneralPreferenceFragment.class.getName().equals(fragmentName) 
				|| GrammarEditPreferenceFragment.class.getName().equals(fragmentName)
				|| TestPreferenceFragment.class.getName().equals(fragmentName))
			return true;
		return super.isValidFragment(fragmentName);
	}
	
	public void reloadDataFromFile() {
		//loadDataFromFile();
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		
		this.startActivityForResult(intent, REQUEST_FILE_SELECT_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_FILE_SELECT_CODE) {
				Uri uri = data.getData();
				mHandler.sendMessage(mHandler.obtainMessage(MSG_LOAD_DATA_FROM_FILE, uri));
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void loadDataFromFile(){
		String fileName= "file:///sdcard/Download/Grammars.txt";
		Uri fileUri = Uri.parse( fileName );
		mHandler.sendMessage(mHandler.obtainMessage(MSG_LOAD_DATA_FROM_FILE, fileUri));
	}
	
	private void loadDataFromFile(final Uri fileUri) {
		Log.d(TAG, "loadDataFromFile uri = " + fileUri);
		LoadDataAyncTask task = new LoadDataAyncTask();
		task.execute(fileUri);
	}
	
	private void exportDataToFile() {
		String fileName= "file:///sdcard/Download/Grammars1.txt";
		Uri fileUri = Uri.parse( fileName );
		mHandler.sendMessage(mHandler.obtainMessage(MSG_EXPORT_DATA_TO_FILE, fileUri));
	}
	
	private void exportDataToFile(final Uri fileUri) {
		Log.d(TAG, "exportDataToFile uri = " + fileUri);
		ExportDataAyncTask task = new ExportDataAyncTask();
		task.execute(fileUri);
	}
	
	private void resetData() {
	}
	
	private class MessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_LOAD_DATA_FROM_FILE: {
				Uri fileUri = (Uri)msg.obj;
				loadDataFromFile(fileUri);
				break;
			}
			case MSG_EXPORT_DATA_TO_FILE: {
				Uri fileUri = (Uri)msg.obj;
				exportDataToFile(fileUri);
				break;
			}
			}
			super.handleMessage(msg);
		}
	}
	
	private void showProgressDialog(int type) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		
		ft.addToBackStack(null);
		
		String message = null;
		switch(type) {
		case 0:
			message = getString(R.string.msg_progress_loading_data);
			break;
		case 1:
			message = getString(R.string.msg_progress_exporting_data);
			break;		
		}
		
		// Create and show the dialog.
		DialogFragment newFragment = GrammarDialogFragment.newInstance(GrammarDialogFragment.DIALOG_TYPE_PROGRESS);
		Bundle args = newFragment.getArguments();
		args.putString(GrammarDialogFragment.FRAGMENT_ARGS_MESSAGE, message);
		newFragment.setCancelable(false);
		newFragment.show(ft, "dialog");
	}
	
	private void dismissDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			DialogFragment df = (DialogFragment)prev;
			df.dismiss();
			ft.remove(prev);
		}
		
		ft.addToBackStack(null);
	}
	
	private class LoadDataAyncTask extends AsyncTask<Uri, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			showProgressDialog(0);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Uri... params) {
			boolean result = GrammarUtils.loadDataFromFile(SettingsActivity.this, params[0], null);
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			dismissDialog();
			super.onPostExecute(result);
		}
	}
	
	private class ExportDataAyncTask extends AsyncTask<Uri, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			showProgressDialog(1);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Uri... params) {
			boolean result = GrammarUtils.exportDataToFile(SettingsActivity.this, params[0], null);
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			dismissDialog();
			super.onPostExecute(result);
		}
	}
}
