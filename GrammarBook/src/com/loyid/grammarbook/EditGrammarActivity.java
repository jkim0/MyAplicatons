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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDatabaseHelper = new DatabaseHelper(this);
		setContentView(R.layout.activity_edit_grammar);
		mAddedItemList = (LinearLayout)findViewById(R.id.added_item_list);
		recalculateIndex();
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
		LinearLayout item = (LinearLayout)getLayoutInflater().inflate(R.layout.added_item_layout, mAddedItemList, false);
		Button btn = (Button)item.findViewById(R.id.btn_remove);
		btn.setTag(mAddedItemList.getChildCount());
		mAddedItemList.addView(item);
		recalculateIndex();
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
		
		String[] projection = {
				GrammarProviderContract.Grammars._ID,
				GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR
		};
		
		String selection = GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR + " = ?";
		
		String[] selectionArgs = { strGrammar };
		
		Cursor c = db.query(GrammarProviderContract.Grammars.TABLE_NAME, 
				projection,
				selection,
				selectionArgs,
				null,
				null,
				GrammarProviderContract.Grammars.DEFAULT_SORT_ORDER);
		
		long grammarId = -1;
		if (c != null && c.getCount() > 0) {
			if (c.moveToFirst()) {
				int index = c.getColumnIndex(GrammarProviderContract.Grammars._ID);
				grammarId = c.getLong(index);
			}
		}

		if (c != null) c.close();

		Long now = Long.valueOf(System.currentTimeMillis());
		
		StringBuilder sb = null;
		int count = mAddedItemList.getChildCount();
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

				selection = GrammarProviderContract.Meanings.COLUMN_NAME_WORD + " = ?"
						+ " AND " + GrammarProviderContract.Meanings.COLUMN_NAME_TYPE + " = ?";

				selectionArgs = new String[] { meaning, String.valueOf(type) };
				
				c = db.query(GrammarProviderContract.Meanings.TABLE_NAME, 
						null,
						selection,
						selectionArgs,
						null,
						null,
						GrammarProviderContract.Meanings.DEFAULT_SORT_ORDER);

				long meaningId = -1;
				if (c != null && c.getCount() > 0) {
					if (c.moveToFirst()) {
						int index = c.getColumnIndex(GrammarProviderContract.Meanings._ID);
						meaningId = c.getLong(index);
					}
				}

				if (c != null) c.close();

				if (meaningId < 0) {
					Log.d(TAG, "insert meaning");
					values.put(GrammarProviderContract.Meanings.COLUMN_NAME_CREATED_DATE, now);
					values.put(GrammarProviderContract.Meanings.COLUMN_NAME_MODIFIED_DATE, now);
					meaningId = db.insert(GrammarProviderContract.Meanings.TABLE_NAME, null, values);
				} else {
					Log.d(TAG, "update meaning index = " + meaningId);
					String whereClause = GrammarProviderContract.Meanings._ID + " = ?";
					String[] whereArgs = { String.valueOf(meaningId) };
					values.put(GrammarProviderContract.Meanings.COLUMN_NAME_MODIFIED_DATE, now);
					db.update(GrammarProviderContract.Meanings.TABLE_NAME, values, whereClause, whereArgs);
				}
				
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
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR, strGrammar);
			values.put(GrammarProviderContract.Grammars.COLUMN_NAME_MEANING, strMeaning);
			
			if (grammarId < 0) {
				Log.d(TAG, "insert grammar");
				values.put(GrammarProviderContract.Grammars.COLUMN_NAME_CREATED_DATE, now);
				values.put(GrammarProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE, now);
				
				grammarId = db.insert(GrammarProviderContract.Grammars.TABLE_NAME, null, values);
			} else {
				Log.d(TAG, "update grammar index = " + grammarId);
				String whereClause = GrammarProviderContract.Grammars._ID + " = ?";
				String[] whereArgs = { String.valueOf(grammarId) };
				values.put(GrammarProviderContract.Grammars.COLUMN_NAME_MODIFIED_DATE, now);
				
				db.update(GrammarProviderContract.Grammars.TABLE_NAME, values, whereClause, whereArgs);
			}			
		} else {
			Log.d(TAG, "failed to save Grammar data because there is no meanings");
		}
		
		finish();
	}
}
