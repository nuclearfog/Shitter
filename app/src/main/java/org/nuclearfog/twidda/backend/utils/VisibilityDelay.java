package org.nuclearfog.twidda.backend.utils;

import android.view.View;

import java.lang.ref.WeakReference;

/**
 * This class is used to set visibility of a view using it's post method memory leak safe
 *
 * @author nuclearfog
 */
public class VisibilityDelay implements Runnable {

	private WeakReference<View> viewRef;
	private boolean visibility;

	/**
	 * @param view       view to set visibility
	 * @param visibility true to set view visible
	 */
	public VisibilityDelay(View view, boolean visibility) {
		viewRef = new WeakReference<>(view);
		this.visibility = visibility;
	}


	@Override
	public void run() {
		View view = viewRef.get();
		if (view != null) {
			if (visibility) {
				view.setVisibility(View.VISIBLE);
			} else {
				view.setVisibility(View.INVISIBLE);
			}
		}
	}
}