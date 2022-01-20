package org.nuclearfog.twidda.backend.proxy;

import org.nuclearfog.twidda.database.GlobalSettings;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * This class hosts {@link UserProxy} and provides the proxy settings on demand
 * Some classes like MediaPlayer doesn't support proxy settings.
 *
 * @author nuclearfog
 */
public class GlobalProxySelector extends ProxySelector {

    private GlobalSettings settings;


    public GlobalProxySelector(GlobalSettings settings) {
        this.settings = settings;
    }

    @Override
    public List<Proxy> select(URI uri) {
        // create proxy list from the settings
        List<Proxy> proxyList = new LinkedList<>();
        proxyList.add(UserProxy.get(settings));
        return proxyList;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        // ignore
    }
}