package org.nuclearfog.twidda;

import android.app.Application;

import org.nuclearfog.twidda.backend.utils.ImageCache;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;

/**
 * @author nuclearfog
 */
public class CustomApplication extends Application {

	@Override
	public void onLowMemory() {
		ImageCache.getInstance(this).clear();
		PicassoBuilder.clear(this);
		super.onLowMemory();
	}
}