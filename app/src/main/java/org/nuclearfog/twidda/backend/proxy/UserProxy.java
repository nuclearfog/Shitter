package org.nuclearfog.twidda.backend.proxy;

import org.nuclearfog.twidda.database.GlobalSettings;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

/**
 * custom proxy implementation
 *
 * @author nuclearfog
 */
public class UserProxy extends Proxy {

    private UserProxy(SocketAddress sa) {
        super(Type.HTTP, sa);
    }

    /**
     * return proxy instance with custom settings
     * @param settings app settings
     * @return proxy instance
     */
    public static Proxy get(GlobalSettings settings) {
        if (settings.isProxyEnabled()) {
            String proxyHost = settings.getProxyHost();
            int proxyPort = settings.getProxyPortNumber();
            InetSocketAddress addr = new InetSocketAddress(proxyHost, proxyPort);
            return new UserProxy(addr);
        }
        return Proxy.NO_PROXY;
    }
}