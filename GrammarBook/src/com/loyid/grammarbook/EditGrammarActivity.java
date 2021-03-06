package com.loyid.grammarbook;

import java.util.ArrayList;
import java.util.Locale;

import com.loyid.grammarbook.GrammarUtils.Grammar;
import com.loyid.grammarbook.GrammarUtils.Meaning;

import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class EditGrammarActivity extends ActionBarActivity {
	private static final String TAG = "EditGrammarActivity";
	private LinearLayout mAddedItemList = null;
	
	private Button mBtnCheck = null;
	private Button mBtnPlay = null;
	private Button mBtnAdd = null;
	
	private Handler mHandler = null;
	
	private static final int MSG_SAVE_GRAMMAR = 0;
	
	private String mCurrentGrammar = null;
	
	public static final String EXTRA_GRAMMAR_ID = "grammar_id";
	
	private SimpleCursorAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mHandler = new MessageHandler();
		setContentView(R.layout.activity_edit_grammar);
		AutoCompleteTextView edit = (AutoCompleteTextView)findViewById(R.id.edit_grammar);
		edit.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if (!mBtnCheck.isEnabled())
					mBtnCheck.setEnabled(true);
				mAdapter.getFilter().filter(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
		});
		
		String projection[] = {
				GrammarProviderContract.Grammars._ID,
				GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR
		};
		
		Cursor cursor = getContentResolver().query(GrammarProviderContract.Grammars.CONTENT_URI,
				projection,
				null,
				null,
				GrammarProviderContract.Grammars.DEFAULT_SORT_ORDER);
		
		mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_dropdown_item_1line,
				cursor, new String[] { GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR },
				new int[] { android.R.id.text1 }, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		
		edit.setAdapter(mAdapter);
		
		mAddedItemList = (LinearLayout)findViewById(R.id.added_item_list);
		mBtnCheck = (Button)findViewById(R.id.btn_check);
		mBtnCheck.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				checkGrammar();
			}
		});
		mBtnPlay = (Button)findViewById(R.id.btn_play);
		mBtnPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText grammar = (EditText)findViewById(R.id.edit_grammar);
				String strGrammar = grammar.getText().toString().trim();
				if (strGrammar != null && strGrammar.length() > 0) {
					((GrammarBookApplication)getApplicationContext()).playTTS(Locale.US, strGrammar);
				}
			}			
		});
		
		mBtnAdd = (Button)findViewById(R.id.btn_add);
		
		Intent intent = getIntent();
		if (intent.hasExtra("grammar_id")) {
			long grammarId = intent.getLongExtra("grammar_id", -1);
			Grammar info = GrammarUtils.getGrammarInfo(this, grammarId);
			edit.setText(info.mGrammar);
			edit.setEnabled(false);
			checkGrammar();
		} else {
			int grammarColumnIndex = cursor.getColumnIndex(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR);
			mAdapter.setStringConversionColumn(grammarColumnIndex);
			mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
				@Override
				public Cursor runQuery(CharSequence constraint) {
					String partialValue = constraint.toString().toUpperCase();
					String projection[] = {
							GrammarProviderContract.Grammars._ID,
							GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR
					};
					
					Cursor cursor = getContentResolver().query(GrammarProviderContract.Grammars.CONTENT_URI,
							projection,
							"UPPER(" + GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR + ") GLOB ?",
							new String[] { partialValue + "*" },
							GrammarProviderContract.Grammars.DEFAULT_SORT_ORDER);
					
					return cursor;
				}
			});
		}
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_grammar, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		} else if (id == R.id.action_done) {
			mHandler.sendEmptyMessage(MSG_SAVE_GRAMMAR);
			return true;
		} else if (id == R.id.action_settings) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setClass(this, GrammarPreferenceActivity.class);
			intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, "com.loyid.grammarbook.SettingsActivity$GrammarEditPreferenceFragment");
			intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE, R.string.pref_header_label_grammar_edit);
			intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onAddButtonClick(View view) {
		addMeaningRow();
	}
	
	private void addMeaningRow() {
		addMeaningRow(-1, null);
	}
	
	private void addMeaningRow(int type, String meaning) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int max = Integer.valueOf(prefs.getString("max_meaning_count", String.valueOf(GrammarUtils.DEFAULT_MEANING_COUNT)));
		if (mAddedItemList.getChildCount() > max) {
			Log.d(TAG, "Can not add meaning line. It's full. max = " + max);
			Toast.makeText(this, R.string.msg_meaning_row_full, Toast.LENGTH_SHORT).show();
			return;
		}
		
		int count = mAddedItemList.getChildCount();
		RelativeLayout last = null;
		if (count > 0) {
			last = (RelativeLayout)mAddedItemList.getChildAt(count - 1);
			EditText edit = (EditText)last.findViewById(R.id.edit_meaning);
			String old = edit.getText().toString().trim();
			if (old != null && old.length() > 0) {
				last = (RelativeLayout)getLayoutInflater().inflate(R.layout.added_item_layout, mAddedItemList, false);
				mAddedItemList.addView(last);
			}
		} else {
			last = (RelativeLayout)getLayoutInflater().inflate(R.layout.added_item_layout, mAddedItemList, false);
			mAddedItemList.addView(last);
		}
				
		Spinner spinner = (Spinner)last.findViewById(R.id.spinner_type);
		if (type > 0) {
			spinner.setSelection(type);
		}
		
		if (meaning != null) {
			EditText edit = (EditText)last.findViewById(R.id.edit_meaning);
			edit.setText(meaning);
		}
		
		recalculateIndex();
	}
	
	private void showAlertDialog(int resId) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		
		ft.addToBackStack(null);
		
		// Create and show the dialog.
		DialogFragment newFragment = GrammarDialogFragment.newInstance(GrammarDialogFragment.DIALOG_TYPE_ALERT_MSG);
		Bundle args = newFragment.getArguments();
		args.putString(GrammarDialogFragment.FRAGMENT_ARGS_MESSAGE, getString(resId));
		newFragment.show(ft, "dialog");
	}
	
	private void checkGrammar() {		
		EditText grammar = (EditText)findViewById(R.id.edit_grammar);
		String strGrammar = grammar.getText().toString().trim();
		
		if (strGrammar == null || strGrammar.length() == 0) {
			showAlertDialog(R.string.msg_no_grammar_in_edit);
			return;
		}
		
		if (mCurrentGrammar != null && mCurrentGrammar.equals(strGrammar)) {
			Log.d(TAG, "checkGrammar() : new grammar is same with old grammar that was inputted.");
			return;
		}
		mCurrentGrammar = strGrammar;
		
		mBtnCheck.setEnabled(false);
		mBtnAdd.setEnabled(true);
		
		long grammarId = GrammarUtils.getGrammarId(this, strGrammar);
		if (grammarId < 0) {
			addMeaningRow();
		} else {
			reloadMeaningRows(grammarId);
		}
	}
	
	private void reloadMeaningRows(long grammarId) {
		Grammar info = GrammarUtils.getGrammarInfo(this,  grammarId);
		ArrayList<Meaning> meanInfos = info.mMeanings;
		
		for (int i = 0; i < meanInfos.size(); i++) {
			Meaning mean = meanInfos.get(i);
			addMeaningRow(mean.mType, mean.mMeaning);
		}
	}
	
	public void recalculateIndex() {
		for (int i = 0; i < mAddedItemList.getChildCount(); i++) {
			RelativeLayout itemLayout = (RelativeLayout)mAddedItemList.getChildAt(i);
			Button btn = (Button)itemLayout.findViewById(R.id.btn_remove);
			btn.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					int index = (Integer)view.getTag();
					mAddedItemList.removeViewAt(index);
					recalculateIndex();
				}
			});
			btn.setTag(i);			
		}
	}
	
	public void saveGrammar() {
		EditText grammar = (EditText)findViewById(R.id.edit_grammar);
		String strGrammar = grammar.getText().toString().trim();
		
		if (strGrammar == null || strGrammar.length() <= 0) {
			Log.d(TAG, "failed to save Grammar data because you did not input grammar.");
			return;
		}
		
		int count = mAddedItemList.getChildCount();
		Grammar grammarData = new Grammar();
		grammarData.mGrammar = strGrammar;
				
		for (int i = 0; i < count; i++) {
			RelativeLayout itemLayout = (RelativeLayout)mAddedItemList.getChildAt(i);
			Spinner spinner = (Spinner)itemLayout.findViewById(R.id.spinner_type);
			EditText edit = (EditText)itemLayout.findViewById(R.id.edit_meaning);
			
			int type = spinner.getSelectedItemPosition();
			String typeStr = (String)spinner.getSelectedItem();
			String meaning = edit.getText().toString().trim();
			
			if (meaning != null && meaning.length() > 0) {
				grammarData.addMeaning(type, typeStr, meaning);
			}
		}
		
		if (grammarData.mMeanings.size() <= 0) {
			Log.d(TAG, "failed to save Grammar data because there is no meanings.");
			return;
		}
		
		Runnable callback = new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "Save grammar works is finished.");
				finish();
			}
		};
		
		boolean successed = GrammarUtils.addGrammar(this, grammarData, callback);
		
		if (!successed) {
			Log.d(TAG, "failed to save grammar in GrammarUtils.addGrammar works");
		}
	}
	
	private class MessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
			case MSG_SAVE_GRAMMAR:
				saveGrammar();
				break;
			}
			super.handleMessage(msg);
		}
	}
}
