package com.loyid.grammarbook;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

public class GrammarListActivity extends ActionBarActivity {
	private static final String TAG = "GrammarListActivity";
	
	private ExGrammarListAdapter mListAdapter = null;
	
	private ExpandableListView mListView = null;
	
	private DatabaseHelper mDatabaseHelper = null;
	private GrammarViewBinder mViewBinder = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grammar_list);
		
		mDatabaseHelper = new DatabaseHelper(this);
		
		mViewBinder = new GrammarViewBinder(this);
		
		mListView = (ExpandableListView)findViewById(R.id.ex_grammar_list);
		mListAdapter = new ExGrammarListAdapter(this, null, R.layout.grammar_group_layout,
				new String[] {GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR,
					GrammarProviderContract.Grammars.COLUMN_NAME_MEANING},
				new int[] {R.id.grammar, R.id.meaning},
				R.layout.grammar_child_layout,
				new String[] {GrammarProviderContract.Meanings.COLUMN_NAME_TYPE,
					GrammarProviderContract.Meanings.COLUMN_NAME_WORD},
				new int[] {R.id.child_type, R.id.child_meaning});
		mListAdapter.setViewBinder(mViewBinder);
		mListView.setAdapter(mListAdapter);
		
		loadGrammarList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.grammar_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class GrammarViewBinder implements SimpleCursorTreeAdapter.ViewBinder {
		private Context mContext = null;
		
		public GrammarViewBinder(Context context) {
			mContext = context;
		}
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			// TODO Auto-generated method stub
			int id = view.getId();
			switch (id) {
			/*
			case R.id.grammar:
				mListAdapter.setViewText((TextView)view, text);
				break;
			*/
			case R.id.meaning:
				String text = cursor.getString(columnIndex);
				String[] group = text.split("#");
				StringBuilder sb = new StringBuilder();
				int size = group.length;
				for (int i = 0; i < size; i++) {
					String[] means = group[i].split("%");
					sb.append(means[1] + " - " + means[2]);
					if (i != size-1) {
						sb.append(", ");
					}
				}
				mListAdapter.setViewText((TextView)view, sb.toString());
				return true;
			case R.id.child_type:
				int type = cursor.getInt(columnIndex);
				String[] types = mContext.getResources().getStringArray(R.array.arry_type);
				mListAdapter.setViewText((TextView)view, types[type] + " - ");
				return true;
			/*
			case R.id.child_meaning:
				break;
			*/
			}
			return false;
		}
		
	}
	
	public class ExGrammarListAdapter extends SimpleCursorTreeAdapter {

		public ExGrammarListAdapter(Context context, Cursor cursor,
				int collapsedGroupLayout,
				String[] groupFrom, int[] groupTo, int childLayout,
				String[] childFrom, int[] childTo) {
			super(context, cursor, collapsedGroupLayout, groupFrom,
					groupTo, childLayout, childFrom, childTo);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			// TODO Auto-generated method stub
			int columnIndex = groupCursor.getColumnIndex(GrammarProviderContract.Grammars._ID);
			long grammarId = groupCursor.getLong(columnIndex);
			SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
			String[] projection = {
					GrammarProviderContract.Meanings._ID,
					GrammarProviderContract.Meanings.COLUMN_NAME_TYPE,
					GrammarProviderContract.Meanings.COLUMN_NAME_WORD
			};
			
			String selection = GrammarProviderContract.Meanings._ID + " IN ("
					+ "SELECT " + GrammarProviderContract.Mappings.COLUMN_NAME_MEANING_ID
					+ " FROM " + GrammarProviderContract.Mappings.TABLE_NAME
					+ " WHERE " + GrammarProviderContract.Mappings.COLUMN_NAME_GRAMMAR_ID + " = ?)";
			String[] selectionArgs = { String.valueOf(grammarId) };
			
			Cursor cursor = db.query(GrammarProviderContract.Meanings.TABLE_NAME, 
					projection,
					selection,
					selectionArgs,
					null,
					null,
					GrammarProviderContract.Meanings.DEFAULT_SORT_ORDER);
			
			if (cursor != null) {
				Log.e(TAG, "getChildrenCursor count = " + cursor.getCount());
			} else {
				Log.e(TAG, "getChildrendCursor cursor is null");
			}
			return cursor;
		}
		
		@Override
	    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
			super.bindGroupView(view, context, cursor, isExpanded);
			TextView text = (TextView)view.findViewById(R.id.meaning);
			if (isExpanded) {
				text.setVisibility(ViewGroup.GONE);
			} else {
				text.setVisibility(ViewGroup.VISIBLE);
			}
	    }
		
	}
	
	private void loadGrammarList() {
		Log.e(TAG, "loadGrammarList");
		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
		String[] projection = { 
				GrammarProviderContract.Grammars._ID,
				GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR,
				GrammarProviderContract.Grammars.COLUMN_NAME_MEANING
		};
		
		Cursor cursor = db.query(GrammarProviderContract.Grammars.TABLE_NAME, 
				projection,
				null,
				null,
				null,
				null,
				GrammarProviderContract.Grammars.DEFAULT_SORT_ORDER);
		
		if (cursor != null) {
			Log.e(TAG, "loadGrammarList cursor count = " + cursor.getCount());
			mListAdapter.setGroupCursor(cursor);
		} else {
			Log.e(TAG, "loadGrammarList cursor is null");
		}
	}
}
