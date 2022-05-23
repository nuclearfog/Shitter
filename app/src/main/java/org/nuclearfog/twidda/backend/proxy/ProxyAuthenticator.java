package org.nuclearfog.twidda.backend.proxy;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.database.GlobalSettings;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * this class provides proxy authentication
 * when proxy settings changes, the new setup will be applied immediately to all okhttp instances
 *
 * @author nuclearfog
 */
public class ProxyAuthenticator extends Authenticator implements okhttp3.Authenticator {
    private GlobalSettings settings;

    /**
     * @param settings global app settings instance
     */
    public ProxyAuthenticator(GlobalSettings settings) {
        this.settings = settings;
    }


    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        if (settings.isProxyAuthSet()) {
            String username = settings.getProxyUser();
            char[] password = settings.getProxyPass().toCharArray();
            return new PasswordAuthentication(username, password);
        }
        return new PasswordAuthentication("", new char[0]);
    }


    @Override
    public Request authenticate(Route route, @NonNull Response response) {
        if (settings.isProxyAuthSet()) {
            String credential = Credentials.basic(settings.getProxyUser(), settings.getProxyPass());
            return response.request().newBuilder().header("Proxy-Authorization", credential).build();
        }
        return null;
    }
}