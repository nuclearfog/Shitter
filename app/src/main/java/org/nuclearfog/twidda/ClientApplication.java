package org.nuclearfog.twidda;

import android.app.Application;

import org.nuclearfog.twidda.backend.image.ImageCache;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;

/**
 * @author nuclearfog
 */
public class ClientApplication extends Application {

	@Override
	public void onLowMemory() {
		ImageCache.clear();
		PicassoBuilder.clear();
		super.onLowMemory();
	}
}