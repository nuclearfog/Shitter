package org.nuclearfog.twidda.backend.utils;

import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Runnable class used to update blur background of a toolbar
 *
 * @author nuclearfog
 */
public class ToolbarUpdater implements Runnable {

	private WeakReference<ImageView> bannerRef, toolbarRef;

	/**
	 * @param profile_banner     profile banner view
	 * @param toolbar_background toolbar background view
	 */
	public ToolbarUpdater(ImageView profile_banner, ImageView toolbar_background) {
		bannerRef = new WeakReference<>(profile_banner);
		toolbarRef = new WeakReference<>(toolbar_background);
	}


	@Override
	public void run() {
		ImageView profile_banner = bannerRef.get();
		ImageView toolbar_background = toolbarRef.get();
		if (profile_banner != null && toolbar_background != null) {
			AppStyles.setToolbarBackground(profile_banner, toolbar_background);
		}
	}
}