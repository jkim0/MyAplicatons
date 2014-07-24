package com.loyid.grammarbook;

import com.loyid.grammarbook.SettingsActivity.GeneralPreferenceFragment;
import com.loyid.grammarbook.SettingsActivity.GrammarEditPreferenceFragment;
import com.loyid.grammarbook.SettingsActivity.TestPreferenceFragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.preference.PreferenceActivity;
import android.util.Log;

public class GrammarPreferenceActivity extends PreferenceActivity {
	private static final String TAG = "GrammarPreferenceActivity";
	
	@Override
    public boolean onNavigateUp() {
        finish();
        return true;
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
}
