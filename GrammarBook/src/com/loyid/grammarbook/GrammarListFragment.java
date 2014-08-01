package com.loyid.grammarbook;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

/**
 * A list fragment representing a list of Items. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link GrammarDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class GrammarListFragment extends ListFragment {
	private static final String TAG = "GrammarListFragment";

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface OnItemSelectedListener {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(long id);
	}
	
	private OnItemSelectedListener  mOnItemSelectedListener = null;
	
	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		mOnItemSelectedListener = listener;
	}
	
	private GrammarListItemAdapter mAdapter = null;
	
	private DatabaseHelper mDatabaseHelper = null;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public GrammarListFragment() {
	}
	
	private final class GrammarListItemAdapter extends ResourceCursorAdapter {
		public GrammarListItemAdapter(Context context, int layout, Cursor c) {
			super(context, layout, c);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final GrammarListItemCache cache = (GrammarListItemCache) view.getTag();
			String grammar = cursor.getString(cache.grammarColumnIndex);
			cache.grammar.setText(grammar);
			String strMeaning = cursor.getString(cache.meaningColumnIndex);
			
			Log.d(TAG, "strMeaning = " + strMeaning);
			String[] group = strMeaning.split("#");
			StringBuilder sb = new StringBuilder();
			SparseArray<ArrayList<String>> array = new SparseArray<ArrayList<String>>();
			int size = group.length;
			ArrayList<String> items;
			for (int i = 0; i < size; i++) {
				String[] means = group[i].split("%");
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
				sb.append("-" + GrammarUtils.getTypeString(getActivity(), type) + " : ");
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
			GrammarListItemCache cache = new GrammarListItemCache();
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
	}
	
	final static class GrammarListItemCache {
		public int grammarColumnIndex = -1;
		public TextView grammar;
		public int meaningColumnIndex = -1;
		public TextView meaning;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDatabaseHelper = new DatabaseHelper(getActivity());
		loadGrammarList();
	}
	
	private void loadGrammarList() {
		String[] projection = {
				GrammarProviderContract.Grammars._ID,
				GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR,
				GrammarProviderContract.Grammars.COLUMN_NAME_MEANING
		};
		
		Cursor cursor = getActivity().getContentResolver().query(GrammarProviderContract.Grammars.CONTENT_URI,
				projection, null, null, GrammarProviderContract.Grammars.DEFAULT_SORT_ORDER);
		
		if (cursor != null) {
			Log.e(TAG, "loadGrammarList cursor count = " + cursor.getCount());
			mAdapter = new GrammarListItemAdapter(getActivity(), R.layout.grammar_list_item, cursor);
		} else {
			Log.e(TAG, "loadGrammarList cursor is null");
		}
				
		setListAdapter(mAdapter);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mOnItemSelectedListener.onItemSelected(id);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
}
