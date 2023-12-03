package org.nuclearfog.twidda.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * custom {@link ConstraintLayout} implementation to lock/unlock child scrolling depending on scroll position and gestures
 *
 * @author nuclearfog
 */
public class LockableConstraintLayout extends ConstraintLayout {

	@Nullable
	private LockCallback callback;
	private boolean yLock = false;
	private float yPos = 0.0f;
	private float xPos = 0.0f;

	/**
	 * @inheritDoc
	 */
	public LockableConstraintLayout(Context context) {
		this(context, null);
	}

	/**
	 * @inheritDoc
	 */
	public LockableConstraintLayout(Context context, @Nullable AttributeSet attr) {
		super(context, attr);
	}


	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getActionMasked()) {
			case MotionEvent.ACTION_SCROLL:
			case MotionEvent.ACTION_MOVE:
				float deltaY = ev.getAxisValue(MotionEvent.AXIS_Y) - yPos;
				float deltaX = ev.getAxisValue(MotionEvent.AXIS_X) - xPos;
				// lock vertical scroll if left/right swipe was detected
				if (Math.abs(deltaX) > Math.abs(deltaY))
					requestDisallowInterceptTouchEvent(true);
				// aquire vertical scroll lock
				else if (deltaY < 0.0f && callback != null)
					yLock = callback.aquireVerticalScrollLock();
				yPos = ev.getAxisValue(MotionEvent.AXIS_Y);
				break;

			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_DOWN:
				// note the current coordinates touch event
				xPos = ev.getAxisValue(MotionEvent.AXIS_X);
				yPos = ev.getAxisValue(MotionEvent.AXIS_Y);
				// fall through

			default:
				yLock = false;
				requestDisallowInterceptTouchEvent(false);
				break;
		}
		return yLock;
	}

	/**
	 * lock/unlock child scrolling
	 *
	 * @param lock true to prevent child views to be scrolled
	 */
	public void lock(boolean lock) {
		this.yLock = lock;
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
		 * aquire scroll lock for child views
		 *
		 * @return true to lock child scroll
		 */
		boolean aquireVerticalScrollLock();
	}
}