package com.loyid.grammarbook;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.SelectionBoundsAdjuster;
import android.widget.TextView;

public class GrammarListItemView extends ViewGroup implements SelectionBoundsAdjuster {
	private static final String TAG = "GrammarListItemView";
	
	private Context mContext;
	
	private int mPreferredHeight = 0;
	private int mHeaderTextColor = Color.BLACK;
	private int mHeaderTextIndent = 0;
	private int mHeaderTextSize = 12;
	private int mHeaderUnderlineHeight = 1;
	private int mHeaderUnderlineColor = 0;
	private int mCountViewTextSize = 12;
	private int mGrammarsCountTextColor = Color.BLACK;
	private int mTextIndent = 0;
	private int mLineGap = 0;
	private Drawable mActivatedBackgroundDrawable;
	
	private boolean mHorizontalDividerVisible = true;
	private Drawable mHorizontalDividerDrawable;
	private int mHorizontalDividerHeight;
	
	private boolean mHeaderVisible;
	private View mHeaderDivider;
	private int mHeaderBackgroundHeight = 30;
	private TextView mHeaderTextView;
	
	private int mSelectionBoundsMarginLeft;
	private int mSelectionBoundsMarginRight;
	
	private Rect mBoundsWithoutHeader = new Rect();
	
	private boolean mActivatedStateSupported;
	
	private int mMainTextViewHeight;
	private int mSubTextViewHeight;
	
	private TextView mMainTextView;
	private TextView mSubTextView;
	private TextView mCountView;

	public GrammarListItemView(Context context) {
		super(context);
		mContext = context;
	}
	
	public GrammarListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.GrammarListItemView);
		mPreferredHeight = a.getDimensionPixelSize(R.styleable.GrammarListItemView_list_item_height,
				mPreferredHeight);
		mActivatedBackgroundDrawable = a.getDrawable(R.styleable.GrammarListItemView_activated_background);
		mHorizontalDividerDrawable = a.getDrawable(R.styleable.GrammarListItemView_list_item_divider);
		
		mHeaderTextIndent = a.getDimensionPixelOffset(R.styleable.GrammarListItemView_list_item_header_text_indent,
				mHeaderTextIndent);
		mHeaderTextColor = a.getColor(R.styleable.GrammarListItemView_list_item_header_text_color,
				mHeaderTextColor);
		mHeaderTextSize = a.getDimensionPixelSize(R.styleable.GrammarListItemView_list_item_header_text_size,
				mHeaderTextSize);
		mHeaderBackgroundHeight = a.getDimensionPixelSize(R.styleable.GrammarListItemView_list_item_header_height,
				mHeaderBackgroundHeight);
		mHeaderUnderlineHeight = a.getDimensionPixelSize(R.styleable.GrammarListItemView_list_item_header_underline_height,
						mHeaderUnderlineHeight);
		mHeaderUnderlineColor = a.getColor(R.styleable.GrammarListItemView_list_item_header_underline_color,
						mHeaderUnderlineColor);
		mTextIndent = a.getDimensionPixelOffset(R.styleable.GrammarListItemView_list_item_text_indent,
				mTextIndent);
		mCountViewTextSize = a.getDimensionPixelSize(R.styleable.GrammarListItemView_list_item_grammars_count_text_size,
						mCountViewTextSize);
		mGrammarsCountTextColor = a.getColor(R.styleable.GrammarListItemView_list_item_grammars_count_text_color,
						mGrammarsCountTextColor);
		mLineGap = a.getDimensionPixelSize(R.styleable.GrammarListItemView_list_item_line_gap, mLineGap);

		setPaddingRelative(
				a.getDimensionPixelOffset(R.styleable.GrammarListItemView_list_item_padding_left, 0),
				a.getDimensionPixelOffset(R.styleable.GrammarListItemView_list_item_padding_top, 0),
				a.getDimensionPixelOffset(R.styleable.GrammarListItemView_list_item_padding_right, 0),
				a.getDimensionPixelOffset(R.styleable.GrammarListItemView_list_item_padding_bottom, 0));
		
		a.recycle();
		
		mHorizontalDividerHeight = mHorizontalDividerDrawable.getIntrinsicHeight();

		if (mActivatedBackgroundDrawable != null) {
			mActivatedBackgroundDrawable.setCallback(this);
		}
	}

	private boolean isVisible(View view) {
		return view != null && view.getVisibility() == View.VISIBLE;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int specWidth = resolveSize(0, widthMeasureSpec);
		final int preferredHeight;
		if (mHorizontalDividerVisible) {
			preferredHeight = mPreferredHeight + mHorizontalDividerHeight;
		} else {
			preferredHeight = mPreferredHeight;
		}
		
		mMainTextViewHeight = 0;
		mSubTextViewHeight = 0;
		
		final int effectiveWidth = specWidth - getPaddingLeft() - getPaddingRight();
		
		if (isVisible(mMainTextView)) {
			int nameTextWidth = effectiveWidth;
			mMainTextView.measure(MeasureSpec.makeMeasureSpec(nameTextWidth,
					MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED));
			mMainTextViewHeight = mMainTextView.getMeasuredHeight();
		}
		
		final boolean subTextViewVisible = isVisible(mSubTextView);
		if (subTextViewVisible) {
			mSubTextView.measure(MeasureSpec.makeMeasureSpec(
					effectiveWidth, MeasureSpec.EXACTLY), MeasureSpec
					.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			mSubTextViewHeight = mSubTextView.getMeasuredHeight();
		}
		
		int height = mMainTextViewHeight + mSubTextViewHeight;
		
		if (subTextViewVisible)
			height += mLineGap;
		
		if (mHorizontalDividerVisible) {
			height += mHorizontalDividerHeight;
		}
		
		height = Math.max(height, preferredHeight);
		
		if (mHeaderVisible) {
			final int headerWidth = specWidth - getPaddingLeft()
					- getPaddingRight() - mHeaderTextIndent;
			mHeaderTextView.measure(MeasureSpec.makeMeasureSpec(headerWidth,
					MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
					mHeaderBackgroundHeight, MeasureSpec.EXACTLY));
			if (mCountView != null) {
				mCountView.measure(MeasureSpec.makeMeasureSpec(headerWidth,
						MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(
						mHeaderBackgroundHeight, MeasureSpec.EXACTLY));
			}
			mHeaderBackgroundHeight = Math.max(mHeaderBackgroundHeight,
					mHeaderTextView.getMeasuredHeight());
			height += (mHeaderBackgroundHeight + mHeaderUnderlineHeight);
		}

		setMeasuredDimension(specWidth, height);
	}
	
	public void setText(String main, String sub) {
		setMainText(main);
		setSubText(sub);
	}
	
	public void setMainText(String text) {
		TextView mainTextView = getMainTextView();
		mainTextView.setText(text);
	}
	
	public void setSubText(String text) {
		TextView subTextView = getSubTextView();
		subTextView.setText(text);
	}

	public void setCountView(CharSequence text) {
		if (TextUtils.isEmpty(text)) {
			if (mCountView != null) {
				mCountView.setVisibility(View.GONE);
			}
		} else {
			getCountView();
			mCountView.setText(text);
			mCountView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCountViewTextSize);
			mCountView.setGravity(Gravity.CENTER_VERTICAL);
			mCountView.setTextColor(mGrammarsCountTextColor);
			mCountView.setVisibility(VISIBLE);
		}
	}

	public TextView getCountView() {
		if (mCountView == null) {
			mCountView = new TextView(mContext);
			mCountView.setSingleLine(true);
			mCountView.setEllipsize(getTextEllipsis());
			mCountView.setTextAppearance(mContext, android.R.style.TextAppearance_Medium);
			mCountView.setTextColor(R.color.people_app_theme_color);
			addView(mCountView);
		}
		return mCountView;
	}

	private TruncateAt getTextEllipsis() {
		return TruncateAt.MARQUEE;
	}
	
	public TextView getMainTextView() {
		if (mMainTextView == null) {
			mMainTextView = new TextView(mContext);
			mMainTextView.setSingleLine(true);
			mMainTextView.setEllipsize(getTextEllipsis());
			mMainTextView.setTextAppearance(mContext, R.style.TextAppearanceMedium);
			mMainTextView.setTypeface(mMainTextView.getTypeface(), Typeface.BOLD);
			mMainTextView.setActivated(isActivated());
			mMainTextView.setGravity(Gravity.CENTER_VERTICAL);
			mMainTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
			mMainTextView.setId(R.id.gliv_main_textview);
			addView(mMainTextView);
		}
		return mMainTextView;
	}
	
	/**
	 * Returns the text view for the phonetic name, creating it if necessary.
	 */
	public TextView getSubTextView() {
		if (mSubTextView == null) {
			mSubTextView = new TextView(mContext);
			mSubTextView.setSingleLine(true);
			mSubTextView.setEllipsize(getTextEllipsis());
			mSubTextView.setTextAppearance(mContext, R.style.TextAppearanceSmall);
			mSubTextView.setActivated(isActivated());
			mSubTextView.setId(R.id.gliv_sub_textview);
			addView(mSubTextView);
		}
		return mSubTextView;
	}
	
	public void setDividerVisible(boolean visible) {
		mHorizontalDividerVisible = visible;
	}
	
	public void setActivatedStateSupported(boolean flag) {
		mActivatedStateSupported = flag;
	}
	
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mActivatedStateSupported) {
			mActivatedBackgroundDrawable.setState(getDrawableState());
		}
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		return who == mActivatedBackgroundDrawable || super.verifyDrawable(who);
	}

	@Override
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();
		if (mActivatedStateSupported) {
			mActivatedBackgroundDrawable.jumpToCurrentState();
		}
	}

	@Override
	public void dispatchDraw(Canvas canvas) {
		if (mActivatedStateSupported && isActivated()) {
			mActivatedBackgroundDrawable.draw(canvas);
		}
		if (mHorizontalDividerVisible) {
			mHorizontalDividerDrawable.draw(canvas);
		}

		super.dispatchDraw(canvas);
	}
	
	public void setSectionHeader(String title) {
		if (!TextUtils.isEmpty(title)) {
			if (mHeaderTextView == null) {
				mHeaderTextView = new TextView(mContext);
				mHeaderTextView.setTextColor(mHeaderTextColor);
				mHeaderTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mHeaderTextSize);
				mHeaderTextView.setTextAppearance(mContext, R.style.SectionHeaderStyle);
				mHeaderTextView.setGravity(Gravity.CENTER_VERTICAL);
				mHeaderTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
				addView(mHeaderTextView);
			}
			if (mHeaderDivider == null) {
				mHeaderDivider = new View(mContext);
				mHeaderDivider.setBackgroundColor(mHeaderUnderlineColor);
				addView(mHeaderDivider);
			}
			mHeaderTextView.setText(title);
			mHeaderTextView.setVisibility(View.VISIBLE);
			mHeaderDivider.setVisibility(View.VISIBLE);
			mHeaderTextView.setAllCaps(true);
			mHeaderVisible = true;
		} else {
			if (mHeaderTextView != null) {
				mHeaderTextView.setVisibility(View.GONE);
			}
			if (mHeaderDivider != null) {
				mHeaderDivider.setVisibility(View.GONE);
			}
			mHeaderVisible = false;
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		final int height = bottom - top;
		final int width = right - left;

		int topBound = 0;
		int bottomBound = height;
		int leftBound = getPaddingLeft();
		int rightBound = width - getPaddingRight();

		if (mHeaderVisible) {
			mHeaderTextView.layout(leftBound + mHeaderTextIndent, 0, 
					rightBound, mHeaderBackgroundHeight);
			if (mCountView != null) {
				mCountView.layout(rightBound - mCountView.getMeasuredWidth(),
						0, rightBound, mHeaderBackgroundHeight);
			}
			mHeaderDivider.layout(leftBound, mHeaderBackgroundHeight,
					rightBound, mHeaderBackgroundHeight + mHeaderUnderlineHeight);
			topBound += (mHeaderBackgroundHeight + mHeaderUnderlineHeight);
		}
		
		if (mHorizontalDividerVisible) {
			mHorizontalDividerDrawable.setBounds(leftBound, height - mHorizontalDividerHeight,
					rightBound, height);
			bottomBound -= mHorizontalDividerHeight;
		}

		mBoundsWithoutHeader.set(0, topBound, width, bottomBound);
		
		if (mActivatedStateSupported && isActivated()) {
            mActivatedBackgroundDrawable.setBounds(mBoundsWithoutHeader);
        }
		
		final boolean subTextViewVisible = isVisible(mSubTextView);
		int totalTextHeight = mMainTextViewHeight + mSubTextViewHeight;
		if (subTextViewVisible)
			totalTextHeight += mLineGap;		
		int textTopBound = (bottomBound + topBound - totalTextHeight) / 2;

		if (isVisible(mMainTextView)) {
			mMainTextView.layout(leftBound, textTopBound, rightBound,
					textTopBound + mMainTextViewHeight);
			textTopBound += mMainTextViewHeight;
		}
		
		if (subTextViewVisible) {
			textTopBound += mLineGap;
			mSubTextView.layout(leftBound, textTopBound, rightBound,
					textTopBound + mSubTextViewHeight);
		}
	}

	@Override
	public void adjustListItemSelectionBounds(Rect bounds) {
		// TODO Auto-generated method stub
		bounds.top += mBoundsWithoutHeader.top;
		bounds.bottom = bounds.top + mBoundsWithoutHeader.height();
		bounds.left += mSelectionBoundsMarginLeft;
		bounds.right -= mSelectionBoundsMarginRight;
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
