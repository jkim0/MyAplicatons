package com.loyid.grammarbook;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * An activity representing a single Item detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link GrammarListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link GrammarDetailFragment}.
 */
public class GrammarDetailActivity extends Activity {
	private static final String TAG = "GrammarDetailActivity";

	private GrammarDetailFragment mFragment;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		
		Log.d(TAG, "orientation changed");
		
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.d(TAG, "orientation changed");
			finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grammar_detail);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putLong(GrammarDetailFragment.ARG_GRAMMAR_ID, getIntent()
					.getLongExtra(GrammarDetailFragment.ARG_GRAMMAR_ID, -1));
			GrammarDetailFragment fragment = new GrammarDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.add(R.id.grammar_detail_container, fragment).commit();
			mFragment = fragment;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.grammar_detail, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_edit) {
			editCurrentGrammar();
			return true;
		} else if (id == R.id.action_delete) {
			showConfirmDialog();
			return true;
		} else if (id == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void editCurrentGrammar() {
		if (mFragment != null) {
			mFragment.editCurrentGrammar();
		}
	}
	
	private void showConfirmDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		
		ft.addToBackStack(null);
		
		// Create and show the dialog.
		GrammarDialogFragment newFragment = GrammarDialogFragment.newInstance(GrammarDialogFragment.DIALOG_TYPE_YES_NO);
		newFragment.setOnClickListener(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					deleteCurrentGrammar();
				}				
			}			
		});
		Bundle args = newFragment.getArguments();
		args.putString(GrammarDialogFragment.FRAGMENT_ARGS_MESSAGE, getString(R.string.msg_delete_grammar_dialog));
		newFragment.show(ft, "dialog");
	}
	
	private void deleteCurrentGrammar() {
		if (mFragment != null) {
			mFragment.deleteCurrentGrammar();
		}
	}
}
