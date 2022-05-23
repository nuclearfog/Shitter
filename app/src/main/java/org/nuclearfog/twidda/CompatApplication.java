package org.nuclearfog.twidda;

import android.app.Application;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import org.nuclearfog.twidda.backend.utils.TLSSocketFactory;

/**
 * custom application class to initialize support for old android versions and proxy settings
 *
 * @author nuclearfog
 */
public class CompatApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        // enable support for vector drawables
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

        // check and enable TLS 1.2 support
        TLSSocketFactory.setSupportTLS();
    }
}