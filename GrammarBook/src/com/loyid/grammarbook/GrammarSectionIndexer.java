package com.loyid.grammarbook;

import java.util.Arrays;

import android.text.TextUtils;
import android.widget.SectionIndexer;

public class GrammarSectionIndexer implements SectionIndexer {
	private static final String TAG = "GrammarSectionIndexer";
	
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
		if (section < 0 || section >= mSections.length) {
			return -1;
		}

		return mPositions[section];
	}

	@Override
	public int getSectionForPosition(int position) {
		if (position < 0 || position >= mCount) {
			return -1;
		}

		int index = Arrays.binarySearch(mPositions, position);

		/*
		 * Consider this example: section positions are 0, 3, 5; the supplied
		 * position is 4. The section corresponding to position 4 starts at
		 * position 3, so the expected return value is 1. Binary search will not
		 * find 4 in the array and thus will return -insertPosition-1, i.e. -3.
		 * To get from that number to the expected value of 1 we need to negate
		 * and subtract 2.
		 */

		return index >= 0 ? index : -index - 2;
	}

	@Override
	public Object[] getSections() {
		return mSections;
	}

}
