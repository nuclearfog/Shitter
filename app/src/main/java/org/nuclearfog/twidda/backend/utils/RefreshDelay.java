package org.nuclearfog.twidda.backend.utils;


import java.lang.ref.WeakReference;

/**
 * memory leak save runnable class used to enable delayed swiperefresh animation
 *
 * @author nuclearfog
 */
public class RefreshDelay implements Runnable {

	private WeakReference<RefreshCallback> callback;

	public RefreshDelay(RefreshCallback callback) {
		this.callback = new WeakReference<>(callback);
	}

	@Override
	public void run() {
		RefreshCallback callback = this.callback.get();
		if (callback != null) {
			callback.onRefreshDelayed();
		}
	}

	/**
	 * callback to enable swiperefresh
	 */
	public interface RefreshCallback {

		void onRefreshDelayed();
	}
}