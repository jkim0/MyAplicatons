package com.loyid.grammarbook;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView.SelectionBoundsAdjuster;
import android.widget.LinearLayout;

public class TestListItemView extends LinearLayout implements SelectionBoundsAdjuster {

	private Rect mBoundsWithoutHeader = new Rect();
	
	public TestListItemView(Context context) {
		super(context);
	}

	public TestListItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TestListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		final int height = bottom - top;
		final int width = right - left;
		int topBound = 0;
		int bottomBound = height;
		super.onLayout(changed, left, top, right, bottom);
		
		View view = this.findViewById(R.id.section_area);
		if (view.getVisibility() == view.VISIBLE) {
			int headerBackgroundHeight = view.getMeasuredHeight();
			topBound += headerBackgroundHeight;
		}
		
		mBoundsWithoutHeader.set(0, topBound, width, bottomBound);
	}

	@Override
	public void adjustListItemSelectionBounds(Rect bounds) {
		bounds.top += mBoundsWithoutHeader.top;
		bounds.bottom = bounds.top + mBoundsWithoutHeader.height();
		bounds.left += 0;//mSelectionBoundsMarginLeft;
		bounds.right -= 0;//mSelectionBoundsMarginRight;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final float x = event.getX();
		final float y = event.getY();
		// If the touch event's coordinates are not within the view's header,
		// then delegate
		// to super.onTouchEvent so that regular view behavior is preserved.
		// Otherwise, consume
		// and ignore the touch event.
		if (mBoundsWithoutHeader.contains((int) x, (int) y) || !pointInView(x, y, 0)) {
			return super.onTouchEvent(event);
		} else {
			return true;
		}
	}
}
