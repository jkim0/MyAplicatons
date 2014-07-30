package com.loyid.grammarbook;

import java.util.Locale;

import com.loyid.grammarbook.GrammarUtils.Grammar;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v7.app.ActionBarActivity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class EditGrammarActivity extends ActionBarActivity implements OnInitListener {
	private static final String TAG = "EditGrammarActivity";
	private LinearLayout mAddedItemList = null;

	private DatabaseHelper mDatabaseHelper = null;
	
	private Button mBtnCheck = null;
	private Button mBtnPlay = null;
	private Button mBtnAdd = null;
	
	private Handler mHandler = null;
	
	private static final int MSG_SAVE_GRAMMAR = 0;
	
	private TextToSpeech mTTS;
	
	private int mMaxLineCount = GrammarUtils.DEFAULT_MEANING_COUNT;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
		mMaxLineCount = prefs.getInt("max_meaning_count", GrammarUtils.DEFAULT_MEANING_COUNT);
		mHandler = new MessageHandler();
		mTTS = new TextToSpeech(this, this);
		mDatabaseHelper = new DatabaseHelper(this);
		setContentView(R.layout.activity_edit_grammar);
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
					mTTS.setLanguage(Locale.US);
					mTTS.speak(strGrammar, TextToSpeech.QUEUE_FLUSH, null);
				}
			}			
		});
		
		mBtnAdd = (Button)findViewById(R.id.btn_add);
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
		if (id == R.id.action_done) {
			mHandler.sendEmptyMessage(MSG_SAVE_GRAMMAR);
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
		if (mAddedItemList.getChildCount() > mMaxLineCount) {
			Log.d(TAG, "Can not add meaing line. It's full. max = " + mMaxLineCount);
			Toast.makeText(this, R.string.msg_meaning_row_full, Toast.LENGTH_SHORT).show();
			return;
		}
		
		RelativeLayout item = (RelativeLayout)getLayoutInflater().inflate(R.layout.added_item_layout, mAddedItemList, false);
		Spinner spinner = (Spinner)item.findViewById(R.id.spinner_type);
		if (type > 0) {
			spinner.setSelection(type);
		}
		if (meaning != null) {
			EditText edit = (EditText)item.findViewById(R.id.edit_meaning);
			edit.setText(meaning);
		}
		mAddedItemList.addView(item);	
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
		String selection = GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID + " = ?";
		String [] selectionArgs = { String.valueOf(grammarId) };
		String[] projection = { GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID };
		
		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
		Cursor cursor = db.query(GrammarProviderContract.Mappings.TABLE_NAME,
				projection,
				selection,
				selectionArgs,
				null,
				null,
				GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID + " ASC");
		
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				selection = GrammarProviderContract.Meanings._ID + " = ? ";
				for (int i = 0; i < cursor.getCount(); i++) {
					int columnIndex = cursor.getColumnIndex(GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID);
					long meaningId = cursor.getInt(columnIndex);
					selectionArgs = new String[] { String.valueOf(meaningId) };
					projection = new String[] { 
							GrammarProviderContract.Meanings.COLUMN_NAME_TYPE,
							GrammarProviderContract.Meanings.COLUMN_NAME_WORD
					};
					
					Cursor meaningCursor = db.query(GrammarProviderContract.Meanings.TABLE_NAME,
							projection,
							selection,
							selectionArgs,
							null,
							null,
							GrammarProviderContract.Meanings.DEFAULT_SORT_ORDER);
					
					if (meaningCursor != null) {
						if (meaningCursor.getCount() > 0 && meaningCursor.moveToFirst()) {
							columnIndex = meaningCursor.getColumnIndex(GrammarProviderContract.Meanings.COLUMN_NAME_TYPE);
							int type = meaningCursor.getInt(columnIndex);
							columnIndex = meaningCursor.getColumnIndex(GrammarProviderContract.Meanings.COLUMN_NAME_WORD);
							String meaning = meaningCursor.getString(columnIndex);
							addMeaningRow(type, meaning);
						}
						meaningCursor.close();
					}
					
					cursor.moveToNext();
				}
			}
			cursor.close();
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
				Log.d(TAG, "####### meaning = " + meaning);
				grammarData.addMeaing(type, typeStr, meaning);
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

	@Override
	public void onInit(int status) {
		boolean isInit = status == TextToSpeech.SUCCESS;
		int msg = isInit ? R.string.msg_init_tts_success : R.string.msg_init_tts_fail;
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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

	@Override
	protected void onDestroy() {
		if (mTTS != null) {
			mTTS.shutdown();
		}
		super.onDestroy();
	}
}
