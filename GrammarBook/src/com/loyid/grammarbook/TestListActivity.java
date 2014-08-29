package com.loyid.grammarbook;

import java.util.ArrayList;
import java.util.Arrays;

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
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class TestListActivity extends ListActivity {
	private static final String TAG = "TestListActivity";
	
	private GrammarPinnedListAdapter mAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getListView().setDividerHeight(0);
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
			mAdapter = new GrammarPinnedListAdapter(this, cursor);
		} else {
			Log.e(TAG, "loadGrammarList cursor is null");
		}
				
		setListAdapter(mAdapter);
	}
}
