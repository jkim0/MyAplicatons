package com.loyid.grammarbook;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class TestListActivity extends ListActivity {
	private static final String TAG = "TestListActivity";
	
	private ListItemAdapter mAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		loadGrammarList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_list, menu);
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
	
	private void loadGrammarList() {
		String[] projection = {
				GrammarProviderContract.Grammars._ID,
				GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR,
				GrammarProviderContract.Grammars.COLUMN_NAME_MEANING
		};
		
		Uri uri = GrammarProviderContract.Grammars.CONTENT_URI;
		uri = uri.buildUpon().appendQueryParameter(GrammarProviderContract.GRAMMAR_BOOK_INDEX_EXTRAS, "true").build();
		Log.d(TAG, "uri = " + uri.toString());
		Cursor cursor = getContentResolver().query(uri,
				projection, null, null, GrammarProviderContract.Grammars.DEFAULT_SORT_ORDER);
		
		if (cursor != null) {
			Log.e(TAG, "loadGrammarList cursor count = " + cursor.getCount());
			mAdapter = new ListItemAdapter(this, R.layout.grammar_list_item, cursor);
		} else {
			Log.e(TAG, "loadGrammarList cursor is null");
		}
				
		setListAdapter(mAdapter);
	}	
	
	private final class ListItemAdapter extends ResourceCursorAdapter implements SectionIndexer {
		private GrammarSectionIndexer mIndexer;
		
		public ListItemAdapter(Context context, int layout, Cursor c) {
			super(context, layout, c);
		}
		
		@Override
		public void changeCursor(Cursor cursor) {
			// TODO Auto-generated method stub
			super.changeCursor(cursor);
			
			Bundle bundle = cursor.getExtras();
			if (bundle.containsKey(GrammarProviderContract.EXTRA_GRAMMAR_BOOK_INDEX_TITLES)) {
				String sections[] = bundle.getStringArray(GrammarProviderContract.EXTRA_GRAMMAR_BOOK_INDEX_TITLES);
				int counts[] = bundle.getIntArray(GrammarProviderContract.EXTRA_GRAMMAR_BOOK_INDEX_COUNTS);
				if (sections.length != counts.length) {
					Log.d(TAG, "out!!!!");
				} else {
					for (int i = 0; i < sections.length; i++) {
						Log.d(TAG, "sections " + sections[i] + " count = " + counts[i]);
					}
				}
			}
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final ListItemCache cache = (ListItemCache) view.getTag();
			String grammar = cursor.getString(cache.grammarColumnIndex);
			cache.grammar.setText(grammar);
			String strMeaning = cursor.getString(cache.meaningColumnIndex);
			
			String[] group = strMeaning.split(GrammarUtils.IDENTIFIER_MEANING_GROUP);
			StringBuilder sb = new StringBuilder();
			SparseArray<ArrayList<String>> array = new SparseArray<ArrayList<String>>();
			int size = group.length;
			ArrayList<String> items;
			for (int i = 0; i < size; i++) {
				String[] means = group[i].split(GrammarUtils.IDENTIFIER_MEANING);
				int type = Integer.valueOf(means[0]);
				String mean;
				if (means.length > 2) {
					mean = means[2];
				} else {
					mean = means[1];
				}
				
				if (array.indexOfKey(type) >= 0) {
					items = array.get(type);
				} else {
					items = new ArrayList<String>();
					array.put(type, items);
				}
				
				items.add(mean);
			}
			
			for (int j = 0; j < array.size(); j++) {
				int type = array.keyAt(j);
				items = array.get(type);
				sb.append("-" + GrammarUtils.getTypeString(TestListActivity.this, type) + " : ");
				for (int k = 0; k < items.size(); k++) {
					sb.append(items.get(k));
					if (k < items.size() - 1)
						sb.append(", ");
				}
				
				if (j < array.size() - 1)
					sb.append("   ");
				
			}
			cache.meaning.setText(sb.toString());
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = super.newView(context, cursor, parent);
			ListItemCache cache = new ListItemCache();
			if (cache.grammarColumnIndex < 0) {
				cache.grammarColumnIndex = cursor.getColumnIndex(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR);
			}
			
			if (cache.meaningColumnIndex < 0) {
				cache.meaningColumnIndex = cursor.getColumnIndex(GrammarProviderContract.Grammars.COLUMN_NAME_MEANING);
			}
			cache.grammar = (TextView) view.findViewById(R.id.grammar);
			cache.meaning = (TextView) view.findViewById(R.id.meaning);
			view.setTag(cache);
			
			return view;
		}
		
		private class ListItemCache {
			public int grammarColumnIndex = -1;
			public TextView grammar;
			public int meaningColumnIndex = -1;
			public TextView meaning;
		}
		
		public void setIndexer(GrammarSectionIndexer indexer) {
			mIndexer = indexer;
		}

		@Override
		public int getPositionForSection(int section) {
			// TODO Auto-generated method stub
			if (mIndexer != null) {
				mIndexer.getPositionForSection(section);
			}
			return 0;
		}

		@Override
		public int getSectionForPosition(int position) {
			// TODO Auto-generated method stub
			if (mIndexer != null) {
				mIndexer.getSectionForPosition(position);
			}
			return 0;
		}

		@Override
		public Object[] getSections() {
			// TODO Auto-generated method stub
			if (mIndexer != null) {
				mIndexer.getSections();
			}
			return null;
		}
	}
	
	private class GrammarSectionIndexer implements SectionIndexer {
		private String[] mSections;
		private int[] mPositions;
		private int mCount;
		private static final String BLANK_HEADER_STRING = " ";
		
		public GrammarSectionIndexer(String[] sections, int[] counts) {
			if (sections == null || counts == null) {
				throw new NullPointerException();
			}
			
			if (sections.length != counts.length) {
				throw new IllegalArgumentException(
						"The sections and counts arrays must have the same length");
			}
			
			mSections = sections;			
			mPositions = new int[counts.length];
			int position = 0;
			for (int i = 0; i < counts.length; i++) {
				if (TextUtils.isEmpty(mSections[i])) {
					mSections[i] = BLANK_HEADER_STRING;
				} else if (!mSections[i].equals(BLANK_HEADER_STRING)) {
					mSections[i] = mSections[i].trim();
				}
				
				mPositions[i] = position;
				position += counts[i];
			}
			
			mCount = position;
		}

		@Override
		public int getPositionForSection(int section) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getSectionForPosition(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object[] getSections() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
