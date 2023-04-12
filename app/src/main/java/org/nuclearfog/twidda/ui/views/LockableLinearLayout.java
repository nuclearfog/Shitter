package org.nuclearfog.twidda.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/**
 * Vertical {@link LinearLayout} implementation with child scroll lock
 *
 * @author nuclearfog
 */
public class LockableLinearLayout extends LinearLayout {

	/**
	 * minimum X-Y ratio of a swipe to determine if it's a left right swipe
	 */
	private static final float LEFT_RIGHT_SWIPE_RATIO = 2.0f;

	@Nullable
	private LockCallback callback;
	private boolean lock = false;
	private float xPos = 0.0f;
	private float yPos = 0.0f;

	/**
	 * @inheritDoc
	 */
	public LockableLinearLayout(Context context) {
		this(context, null);
	}

	/**
	 * @inheritDoc
	 */
	public LockableLinearLayout(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * @inheritDoc
	 */
	public LockableLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	/**
	 * @inheritDoc
	 */
	public LockableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		setOrientation(VERTICAL);
	}


	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				yPos = ev.getY();
				break;

			case MotionEvent.ACTION_MOVE:
				float deltaX = ev.getX() - xPos;
				float deltaY = ev.getY() - yPos;
				// detect up/down swipe
				if (deltaY < 0.0f && Math.abs(deltaX * LEFT_RIGHT_SWIPE_RATIO) < Math.abs(deltaY)) {
					if (callback != null) {
						lock = callback.aquireLock();
					}
				}
				// detect left/right swipe
				else {
					lock = false;
				}
				xPos = ev.getX();
				yPos = ev.getY();
				break;
		}
		return lock;
	}

	/**
	 * lock/unlock child scrolling
	 *
	 * @param lock true to prevent child views to be scrolled
	 */
	public void lock(boolean lock) {
		this.lock = lock;
	}

	/**
	 * add callback to aquire child scroll lock
	 */
	public void addLockCallback(LockCallback callback) {
		this.callback = callback;
	}

	/**
	 * Callback to aquire child view scoll lock from activity
	 */
	public interface LockCallback {

		/**
		 * @return true to lock child scroll
		 */
		boolean aquireLock();
	}
}