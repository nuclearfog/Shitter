package org.nuclearfog.twidda.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * {@link ConstraintLayout} implementation with child scroll lock
 *
 * @author nuclearfog
 */
public class LockableConstraintLayout extends ConstraintLayout {

	@Nullable
	private LockCallback callback;
	private boolean xLock = false;
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
		switch (ev.getAction()) {
			case MotionEvent.ACTION_MOVE:
				float deltaX = ev.getX() - xPos;
				float deltaY = ev.getY() - yPos;
				// detect scroll down, then aquire scroll lock
				if (!xLock && Math.abs(deltaY) > Math.abs(deltaX) * 3.0f) {
					xLock = true;
				}
				if (xLock && deltaY < 0.0f && callback != null) {
					yLock = callback.aquireLock();
				}
				// fall through

			case MotionEvent.ACTION_DOWN:
				xPos = ev.getX();
				yPos = ev.getY();
				break;

			case MotionEvent.ACTION_UP:
				xLock = false;
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
		 * @return true to lock child scroll
		 */
		boolean aquireLock();
	}
}