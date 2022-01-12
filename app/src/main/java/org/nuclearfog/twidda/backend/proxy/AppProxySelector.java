package org.nuclearfog.twidda.backend.proxy;

import org.nuclearfog.twidda.database.GlobalSettings;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * This class hosts {@link UserProxy} and provides the proxy settings on demand
 *
 * @author nuclearfog
 */
public class AppProxySelector extends ProxySelector {

    private List<Proxy> proxyList;


    public AppProxySelector(GlobalSettings settings) {
        Proxy httpsProxy = UserProxy.get(settings);
        proxyList = new ArrayList<>(2);
        proxyList.add(httpsProxy);
    }

    @Override
    public List<Proxy> select(URI uri) {
        return proxyList;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        // ignore
    }
}