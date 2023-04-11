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
	private boolean lock = true;
	private float yPos = 0.0f;

	/**
	 * @inheritDoc
	 */
	public LockableConstraintLayout(Context context) {
		super(context);
	}

	/**
	 * @inheritDoc
	 */
	public LockableConstraintLayout(Context context, AttributeSet attr) {
		super(context, attr);
	}


	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				yPos = ev.getY();
				break;

			case MotionEvent.ACTION_MOVE:
				if (ev.getY() < yPos && callback != null) {
					lock = callback.aquireLock();
				}
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