package org.nuclearfog.twidda;

import android.app.Application;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * custom application class
 *
 * @author nuclearfog
 */
public class CompatApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }
    }
}