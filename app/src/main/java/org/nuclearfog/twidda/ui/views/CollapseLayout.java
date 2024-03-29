package org.nuclearfog.twidda.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.core.widget.NestedScrollView.OnScrollChangeListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.ui.views.LockableConstraintLayout.LockCallback;

/**
 * Implementation of a layout containing a header and body layout. The header layout can be hidden (collapsed) by scrolling to the bottom.
 * After collapsing, the body view fills the whole layout.
 *
 * @author nuclearfog
 */
public class CollapseLayout extends NestedScrollView implements OnScrollChangeListener, OnPreDrawListener, LockCallback {

	/**
	 * scrollview position threshold to lock/unlock child scrolling
	 */
	private static final int SCROLL_THRESHOLD = 10;

	@Nullable
	private View header;
	@Nullable
	private LockableConstraintLayout body;

	@IdRes
	private int headerId = NO_ID;
	@IdRes
	private int bodyId = NO_ID;

	/**
	 *
	 */
	public CollapseLayout(@NonNull Context context) {
		this(context, null);
	}

	/**
	 *
	 */
	public CollapseLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		if (attrs != null) {
			TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.CollapseLayout);
			headerId = attrArray.getResourceId(R.styleable.CollapseLayout_header, NO_ID);
			bodyId = attrArray.getResourceId(R.styleable.CollapseLayout_body, NO_ID);
			attrArray.recycle();
		}
		setOnScrollChangeListener(this);
		getViewTreeObserver().addOnPreDrawListener(this);
	}


	@Override
	public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
		if (header != null && body != null) {
			body.lock(scrollY > header.getMeasuredHeight() + SCROLL_THRESHOLD && scrollY < header.getMeasuredHeight() - SCROLL_THRESHOLD);
		}
	}


	@Override
	public boolean aquireVerticalScrollLock() {
		return header != null && getScrollY() < header.getMeasuredHeight() - SCROLL_THRESHOLD;
	}


	@Override
	public boolean onPreDraw() {
		getViewTreeObserver().removeOnPreDrawListener(this);
		header = findViewById(headerId);
		View bodyView = findViewById(bodyId);
		if (bodyView instanceof LockableConstraintLayout) {
			body = (LockableConstraintLayout) bodyView;
			body.addLockCallback(this);
			body.getLayoutParams().height = getMeasuredHeight();
			body.requestLayout();
		}
		scrollTo(0, 0);
		return true;
	}
}