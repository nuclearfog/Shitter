package org.nuclearfog.twidda;

import android.app.Application;

import org.nuclearfog.twidda.backend.image.ImageCache;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.unifiedpush.android.connector.ConstantsKt;
import org.unifiedpush.android.connector.UnifiedPush;

import java.util.ArrayList;

/**
 * @author nuclearfog
 */
public class ClientApplication extends Application {


	@Override
	public void onCreate() {
		super.onCreate();
		ArrayList<String> features = new ArrayList<>(1);
		features.add(UnifiedPush.FEATURE_BYTES_MESSAGE);
		UnifiedPush.registerApp(this, ConstantsKt.INSTANCE_DEFAULT, features, "");
	}


	@Override
	public void onTerminate() {
		super.onTerminate();
		UnifiedPush.unregisterApp(this, ConstantsKt.INSTANCE_DEFAULT);
	}


	@Override
	public void onLowMemory() {
		ImageCache.clear();
		PicassoBuilder.clear();
		super.onLowMemory();
	}
}