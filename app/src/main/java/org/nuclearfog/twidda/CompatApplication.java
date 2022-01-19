package org.nuclearfog.twidda;

import android.app.Application;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import org.nuclearfog.twidda.backend.proxy.AppProxySelector;
import org.nuclearfog.twidda.backend.proxy.ProxyAuthenticator;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.net.Authenticator;
import java.net.ProxySelector;

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
        // setup proxy settings
        GlobalSettings settings = GlobalSettings.getInstance(this);
        AppProxySelector proxyConnection = new AppProxySelector(settings);
        ProxyAuthenticator proxyLogin = new ProxyAuthenticator(settings);
        try {
            ProxySelector.setDefault(proxyConnection);
            Authenticator.setDefault(proxyLogin);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}