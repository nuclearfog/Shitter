package org.nuclearfog.twidda.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;

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
	private static final int SCROLL_THRESHOLD = 50;

	private int headerId, bodyId;
	@Nullable
	private View header;
	@Nullable
	private LockableConstraintLayout body;

	private float xPos, yPos;

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
			headerId = attrArray.getResourceId(R.styleable.CollapseLayout_header, 0);
			bodyId = attrArray.getResourceId(R.styleable.CollapseLayout_body, 0);
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
	public boolean aquireLock() {
		return header != null && getScrollY() < header.getMeasuredHeight() - SCROLL_THRESHOLD;
	}


	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getActionMasked()) {
			case MotionEvent.ACTION_MOVE:
				float deltaX = ev.getX() - xPos;
				float deltaY = ev.getY() - yPos;
				// lock x-axis when swiping up/down
				if (Math.abs(deltaX) > Math.abs(deltaY) * 1.1f) {
					setNestedScrollingEnabled(false);
				}
				// fall through

			case MotionEvent.ACTION_DOWN:
				// note start coordinates of the gesture
				xPos = ev.getX();
				yPos = ev.getY();
				break;

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				setNestedScrollingEnabled(true);
				break;
		}
		return false;
	}


	@Override
	public boolean onPreDraw() {
		getViewTreeObserver().removeOnPreDrawListener(this);
		if (headerId != 0) {
			header = findViewById(headerId);
		}
		if (bodyId != 0) {
			body = findViewById(bodyId);
			body.addLockCallback(this);
		}
		if (body != null) {
			body.getLayoutParams().height = getMeasuredHeight();
			body.requestLayout();
		}
		scrollTo(0, 0);
		return true;
	}
}