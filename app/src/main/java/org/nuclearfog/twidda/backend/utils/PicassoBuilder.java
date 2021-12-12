package org.nuclearfog.twidda.backend.utils;

import android.content.Context;
import android.os.Build;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

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
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                try {
                    // try to enable TLS 1.2 support for picasso
                    TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    factory.init((KeyStore) null);
                    X509TrustManager manager = (X509TrustManager) factory.getTrustManagers()[0];
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    builder.sslSocketFactory(new TLSSocketFactory(), manager);
                    downloader = new OkHttp3Downloader(builder.build());
                } catch (Exception e) {
                    // fallback to default downloader
                    downloader = new OkHttp3Downloader(context);
                }
            } else {
                // use default downloader
                downloader = new OkHttp3Downloader(context);
            }
        }
        return new Picasso.Builder(context).downloader(downloader).build();
    }
}