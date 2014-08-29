package com.loyid.grammarbook;

import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
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
public class GrammarListFragment extends ListFragment implements Callback {
	private static final String TAG = "GrammarListFragment";

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = 0;
	
	private GrammarPinnedListAdapter mAdapter = null;
	
	private boolean mDualPane = false;
	
	private Handler mHandler = new Handler(this);
	
	private static final int MSG_MOVE_CURRENT_POSITION = 0;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public GrammarListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		ListView listView = getListView();
		listView.setDividerHeight(0);
		listView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
		
		if (getActivity().findViewById(R.id.grammar_detail_container) != null) {
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			listView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
			mDualPane = true;
		}
		

		int leftPadding = 0;
		int rightPadding = 0;
		if (mDualPane) {
			leftPadding = getActivity().getResources().getDimensionPixelOffset(
					R.dimen.list_visible_scrollbar_padding);
		} else {
			rightPadding = getActivity().getResources().getDimensionPixelOffset(
					R.dimen.list_visible_scrollbar_padding);
		}
		
		listView.setPadding(leftPadding, listView.getPaddingTop(),
                rightPadding, listView.getPaddingBottom());

		
		if (savedInstanceState != null)
			mActivatedPosition = savedInstanceState.getInt(STATE_ACTIVATED_POSITION);
		
		loadGrammarList();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		Adapter adapter = getListAdapter();
		long itemId = adapter.getItemId(info.position);
		if (id == R.id.action_detail) {
			showDetails(info.position);
			return true;
		} else if (id == R.id.action_edit) {
			Intent editIntent = new Intent(getActivity(), EditGrammarActivity.class);
			editIntent.putExtra(EditGrammarActivity.EXTRA_GRAMMAR_ID, itemId);			
			startActivity(editIntent);
			return true;
		} else if (id == R.id.action_delete) {
			GrammarUtils.deleteGrammar(getActivity(), itemId);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = this.getActivity().getMenuInflater();
		inflater.inflate(R.menu.grammar_list_context, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.grammar_list, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_add) {
			Intent intent = new Intent(getActivity(), EditGrammarActivity.class);
			startActivity(intent);
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
		Cursor cursor = getActivity().getContentResolver().query(uri,
				projection, null, null, GrammarProviderContract.Grammars.DEFAULT_SORT_ORDER);
		
		if (cursor != null) {
			Log.e(TAG, "loadGrammarList cursor count = " + cursor.getCount());
			mAdapter = new GrammarPinnedListAdapter(getActivity(), cursor);
			mAdapter.setSelectionVisible(mDualPane);
		} else {
			Log.e(TAG, "loadGrammarList cursor is null");
		}
				
		setListAdapter(mAdapter);
		
		if (mDualPane) {
			showDetails(mActivatedPosition);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		Log.d(TAG, "onViewCreated()");
		
		registerForContextMenu(getListView());
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {		
		showDetails(position);
	}
	
	private void showDetails(int position) {
		Log.d(TAG, "showDetails position = " + position);
		if (getListAdapter().getCount() <= 0) {
			Log.d(TAG, "There is no items.");
			return;
		}
		mActivatedPosition = position;
		long id = getListAdapter().getItemId(position);
		if (mDualPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			mHandler.sendMessage(mHandler.obtainMessage(0, position, 0));
			Bundle arguments = new Bundle();
			arguments.putLong(GrammarDetailFragment.ARG_GRAMMAR_ID, id);
			GrammarDetailFragment fragment = new GrammarDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.replace(R.id.grammar_detail_container, fragment)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(getActivity(), GrammarDetailActivity.class);
			detailIntent.putExtra(GrammarDetailFragment.ARG_GRAMMAR_ID, id);
			startActivity(detailIntent);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_MOVE_CURRENT_POSITION:
			int position = msg.arg1;
			int first = getListView().getFirstVisiblePosition();
			int last = getListView().getLastVisiblePosition();
			
			getListView().setItemChecked(position, true);
			if (first > position || last < position) {
				getListView().setSelection(position);
			}
			break;
		}
		return true;
	}
}
