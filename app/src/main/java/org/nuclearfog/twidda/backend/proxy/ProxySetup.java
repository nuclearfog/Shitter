package org.nuclearfog.twidda.backend.proxy;

import org.nuclearfog.twidda.database.GlobalSettings;

import java.net.Authenticator;
import java.net.ProxySelector;

/**
 * This class setups a proxy for libraries which don't support proxy setup
 * like VideoView
 *
 * @author nuclearfog
 */
public class ProxySetup {

    private ProxySetup() {
    }

    /**
     * initializes the proxy connection with login
     *
     * @param settings App settings
     */
    public static void setConnection(GlobalSettings settings) {
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