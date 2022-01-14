package org.nuclearfog.twidda.backend.utils;

import android.content.Context;
import android.os.Build;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.backend.proxy.ProxyAuthenticator;
import org.nuclearfog.twidda.backend.proxy.UserProxy;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.security.KeyStore;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Create Picasso instance with TLS 1.2 support for pre Lollipo devices
 *
 * @author nuclearfog
 */
public class PicassoBuilder {

    private static OkHttp3Downloader downloader;

    private PicassoBuilder() {
    }

    /**
     * @return instance of Picasso with custom downloader
     */
    public static Picasso get(Context context) {
        if (downloader == null) {
            GlobalSettings settings = GlobalSettings.getInstance(context);
            init(settings);
        }
        return new Picasso.Builder(context).downloader(downloader).build();
    }


    private static void init(GlobalSettings settings) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // setup proxy
        if (settings.isProxyEnabled()) {
            builder.proxy(UserProxy.get(settings));
            if (settings.isProxyAuthSet()) {
                builder.proxyAuthenticator(new ProxyAuthenticator(settings));
            }
        }

        // setup TLS 1.2 support if needed
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {
                TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                factory.init((KeyStore) null);
                X509TrustManager manager = (X509TrustManager) factory.getTrustManagers()[0];
                builder.sslSocketFactory(new TLSSocketFactory(), manager);
                downloader = new OkHttp3Downloader(builder.build());
                return;
            } catch (Exception e) {
                // ignore, try without TLS 1.2 support
            }
        }
        downloader = new OkHttp3Downloader(builder.build());
    }
}