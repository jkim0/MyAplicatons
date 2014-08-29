package com.loyid.grammarbook;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class GrammarPinnedListAdapter extends CursorAdapter implements
		SectionIndexer {
	private static final String TAG = "GrammarPinnedListAdapter";

	private GrammarSectionIndexer mIndexer;

	private boolean mSectionHeaderDisplayEnabled = true;
	
	private boolean mSelectionVisible;

	public static final class Placement {
		private int position = ListView.INVALID_POSITION;
		public boolean firstInSection;
		public boolean lastInSection;
		public String sectionHeader;
		public int count;

		public void invalidate() {
			position = ListView.INVALID_POSITION;
		}
	}

	private Placement mPlacementCache = new Placement();

	public GrammarPinnedListAdapter(Context context, Cursor c) {
		super(context, c);

		updateSectionIndexer(c);
	}

	private void updateSectionIndexer(Cursor cursor) {
		if (cursor == null) {
			setIndexer(null);
			return;
		}

		Bundle bundle = cursor.getExtras();
		Log.d(TAG, "updateSectionIndexer bundle = " + bundle);
		if (bundle != null
				&& bundle
						.containsKey(GrammarProviderContract.EXTRA_GRAMMAR_BOOK_INDEX_TITLES)) {
			String sections[] = bundle
					.getStringArray(GrammarProviderContract.EXTRA_GRAMMAR_BOOK_INDEX_TITLES);
			int counts[] = bundle
					.getIntArray(GrammarProviderContract.EXTRA_GRAMMAR_BOOK_INDEX_COUNTS);
			setIndexer(new GrammarSectionIndexer(sections, counts));
		} else {
			setIndexer(null);
		}
	}

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);

		updateSectionIndexer(cursor);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ListItemCache cache = (ListItemCache) view.getTag();
		final GrammarListItemView itemView = (GrammarListItemView)view;
		String grammar = cursor.getString(cache.grammarColumnIndex);
		String strMeaning = cursor.getString(cache.meaningColumnIndex);

		String[] group = strMeaning
				.split(GrammarUtils.IDENTIFIER_MEANING_GROUP);
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
			sb.append("-" + GrammarUtils.getTypeString(mContext, type) + " : ");
			for (int k = 0; k < items.size(); k++) {
				sb.append(items.get(k));
				if (k < items.size() - 1)
					sb.append(", ");
			}

			if (j < array.size() - 1)
				sb.append("   ");

		}
		itemView.setText(grammar, sb.toString());
		bindSectionHeaderAndDivider(itemView, cursor.getPosition());
	}

	protected void bindSectionHeaderAndDivider(GrammarListItemView view, int position) {
		if (isSectionHeaderDisplayEnabled()) {
			Placement placement = getItemPlacementInSection(position);
			if (placement.count >= 0) {
				view.setCountView(""+placement.count);
			} else {
				view.setCountView(null);
			}
			view.setSectionHeader(placement.sectionHeader);
			view.setDividerVisible(!placement.lastInSection);
		} else {
			view.setSectionHeader(null);
			view.setDividerVisible(true);
			view.setCountView(null);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		GrammarListItemView view = new GrammarListItemView(context, null);
		view.setActivatedStateSupported(isSelectionVisible());
		ListItemCache cache = new ListItemCache();
		if (cache.grammarColumnIndex < 0) {
			cache.grammarColumnIndex = cursor
					.getColumnIndex(GrammarProviderContract.Grammars.COLUMN_NAME_GRAMMAR);
		}

		if (cache.meaningColumnIndex < 0) {
			cache.meaningColumnIndex = cursor
					.getColumnIndex(GrammarProviderContract.Grammars.COLUMN_NAME_MEANING);
		}
		view.setTag(cache);

		return view;
	}

	private class ListItemCache {
		public int grammarColumnIndex = -1;
		public int meaningColumnIndex = -1;
	}

	public void setIndexer(GrammarSectionIndexer indexer) {
		mIndexer = indexer;
	}

	public SectionIndexer getIndexer() {
		return mIndexer;
	}

	@Override
	public int getPositionForSection(int section) {
		if (mIndexer == null) {
			return -1;
		}

		return mIndexer.getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		// TODO Auto-generated method stub
		if (mIndexer == null) {
			return -1;
		}

		return mIndexer.getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		// TODO Auto-generated method stub
		if (mIndexer == null) {
			return new String[] { " " };
		}

		return mIndexer.getSections();
	}

	private int getCountForSection(int section) {
		if (mIndexer == null) {
			return -1;
		}

		return mIndexer.getCountForSection(section);
	}

	public Placement getItemPlacementInSection(int position) {
		if (mPlacementCache.position == position) {
			return mPlacementCache;
		}

		mPlacementCache.position = position;
		if (isSectionHeaderDisplayEnabled()) {
			int section = getSectionForPosition(position);
			if (section != -1 && getPositionForSection(section) == position) {
				mPlacementCache.firstInSection = true;
				mPlacementCache.sectionHeader = (String) getSections()[section];
				mPlacementCache.count = getCountForSection(section);
			} else {
				mPlacementCache.firstInSection = false;
				mPlacementCache.sectionHeader = null;
				mPlacementCache.count = -1;
			}

			mPlacementCache.lastInSection = (getPositionForSection(section + 1) - 1 == position);
		} else {
			mPlacementCache.firstInSection = false;
			mPlacementCache.lastInSection = false;
			mPlacementCache.sectionHeader = null;
			mPlacementCache.count = -1;
		}

		return mPlacementCache;
	}

	public boolean isSectionHeaderDisplayEnabled() {
		return mSectionHeaderDisplayEnabled;
	}

	public void setSectionHeaderDisplayEnabled(boolean flag) {
		this.mSectionHeaderDisplayEnabled = flag;
	}
	
	public boolean isSelectionVisible() {
		return mSelectionVisible;
	}

	public void setSelectionVisible(boolean visible) {
		this.mSelectionVisible = visible;
	}
}
