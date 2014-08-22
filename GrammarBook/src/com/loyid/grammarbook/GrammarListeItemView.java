package com.loyid.grammarbook;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class GrammarListeItemView extends ViewGroup {
	private static final String TAG = "GrammarListItemView";
	
	private Context mContext;

	public GrammarListeItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		
	}

}
