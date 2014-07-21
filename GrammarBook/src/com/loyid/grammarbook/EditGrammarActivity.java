package com.loyid.grammarbook;

import android.support.v7.app.ActionBarActivity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class EditGrammarActivity extends ActionBarActivity {
	private static final String TAG = "EditGrammarActivity";
	private LinearLayout mAddedItemList = null;

	private DatabaseHelper mDatabaseHelper = null;
	
	private Button mBtnCheck = null;
	
	private long mGrammarId = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
			saveGrammar();
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
		LinearLayout item = (LinearLayout)getLayoutInflater().inflate(R.layout.added_item_layout, mAddedItemList, false);
		if (type > 0) {
			Spinner spinner = (Spinner)item.findViewById(R.id.spinner_type);
			spinner.setSelection(type);
		}
		if (meaning != null) {
			EditText edit = (EditText)item.findViewById(R.id.edit_meaning);
			edit.setText(meaning);
		}
		mAddedItemList.addView(item);
		recalculateIndex();
	}
	
	private void checkGrammar() {
		EditText grammar = (EditText)findViewById(R.id.edit_grammar);
		String strGrammar = grammar.getText().toString().trim();
		
		long grammarId = getCurrentGrammarId(strGrammar);
		if (grammarId < 0) {
			addMeaningRow();
		} else {
			reloadMeaningRows(grammarId);
		}
	}
	
	private long getCurrentGrammarId(String strGrammar) {
		if (mGrammarId > 0) return mGrammarId;
		
		String selection = GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR + " = ?";		
		String[] selectionArgs = { strGrammar };
		
		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
		Cursor cursor = db.query(GrammarProviderContract.Grammars.TABLE_NAME, 
				null,
				selection,
				selectionArgs,
				null,
				null,
				GrammarProviderContract.Grammars.DEFAULT_SORT_ORDER);
		
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int index = cursor.getColumnIndex(GrammarProviderContract.Grammars._ID);
				mGrammarId = cursor.getLong(index);
			}
			
			cursor.close();
		}
		
		return mGrammarId;
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
			LinearLayout itemLayout = (LinearLayout)mAddedItemList.getChildAt(i);
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
		
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		
		long grammarId = this.getCurrentGrammarId(strGrammar);
		
		Long now = Long.valueOf(System.currentTimeMillis());
		
		StringBuilder sb = null;
		int count = mAddedItemList.getChildCount();
		
		Log.e(TAG, "################# saveGrammar grammarId = " + grammarId);
		if (grammarId < 0 && count > 0) {
			ContentValues values = new ContentValues();
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR, strGrammar);
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_CREATED_DATE, now);
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE, now);
			
			grammarId = db.insert(GrammarProviderContract.Grammars.TABLE_NAME, null, values);
		}
		
		String whereClause = GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID + " = ?";
		String[] whereArgs = { String.valueOf(grammarId) };
		int result = db.delete(GrammarProviderContract.Mappings.TABLE_NAME, whereClause, whereArgs);
		Log.d(TAG, "remove " + result + " items from Mappings table");
		
		for (int i = 0; i < count; i++) {
			LinearLayout itemLayout = (LinearLayout)mAddedItemList.getChildAt(i);
			Spinner spinner = (Spinner)itemLayout.findViewById(R.id.spinner_type);
			EditText edit = (EditText)itemLayout.findViewById(R.id.edit_meaning);
			int type = spinner.getSelectedItemPosition();
			String typeStr = (String)spinner.getSelectedItem();
			String meaning = edit.getText().toString().trim();
			
			if (meaning != null && meaning.length() > 0) {
				ContentValues values = new ContentValues();
				values.put(GrammarProviderContract.Meanings.COLUMN_NAME_WORD, meaning);
				values.put(GrammarProviderContract.Meanings.COLUMN_NAME_TYPE, type);

				String selection = GrammarProviderContract.Meanings.COLUMN_NAME_WORD + " = ?"
						+ " AND " + GrammarProviderContract.Meanings.COLUMN_NAME_TYPE + " = ?";

				String[] selectionArgs = new String[] { meaning, String.valueOf(type) };
				
				Cursor cursor = db.query(GrammarProviderContract.Meanings.TABLE_NAME, 
						null,
						selection,
						selectionArgs,
						null,
						null,
						GrammarProviderContract.Meanings.DEFAULT_SORT_ORDER);

				long meaningId = -1;
				if (cursor != null) {
					if (cursor.getCount() > 0 && cursor.moveToFirst()) {
						int index = cursor.getColumnIndex(GrammarProviderContract.Meanings._ID);
						meaningId = cursor.getLong(index);
					}
					
					cursor.close();
				}

				if (meaningId < 0) {
					Log.d(TAG, "insert meaning");
					values.put(GrammarProviderContract.Meanings.COLUMN_NAME_CREATED_DATE, now);
					values.put(GrammarProviderContract.Meanings.COLUMN_NAME_MODIFIED_DATE, now);
					meaningId = db.insert(GrammarProviderContract.Meanings.TABLE_NAME, null, values);
				} else {
					Log.d(TAG, "update meaning index = " + meaningId);
					whereClause = GrammarProviderContract.Meanings._ID + " = ?";
					whereArgs = new String[] { String.valueOf(meaningId) };
					values.put(GrammarProviderContract.Meanings.COLUMN_NAME_MODIFIED_DATE, now);
					db.update(GrammarProviderContract.Meanings.TABLE_NAME, values, whereClause, whereArgs);
				}
				
				values = new ContentValues();
				values.put(GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID, grammarId);
				values.put(GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID, meaningId);
				db.insert(GrammarProviderContract.Mappings.TABLE_NAME, null, values);
				
				if (sb == null)
					sb = new StringBuilder();
				sb.append(type);
				sb.append("%" + typeStr);
				sb.append("%" + meaning);
				
				if (i != count - 1) {
					sb.append("$");
				}
			}
		
		}
		
		if (sb != null) {
			String strMeaning = sb.toString();
			Log.d(TAG, "############# saveGrammar Grammar = " + strGrammar + " meaning = " + strMeaning);
			
			ContentValues values = new ContentValues();
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_MEANING, strMeaning);
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE, now);
			
			Log.d(TAG, "update grammar index = " + grammarId);
			whereClause = GrammarProviderContract.Grammars._ID + " = ?";
			whereArgs = new String[] { String.valueOf(grammarId) };
				
			db.update(GrammarProviderContract.Grammars.TABLE_NAME, values, whereClause, whereArgs);			
		} else {
			Log.d(TAG, "failed to save Grammar data because there is no meanings");
		}
		
		finish();
	}
}
