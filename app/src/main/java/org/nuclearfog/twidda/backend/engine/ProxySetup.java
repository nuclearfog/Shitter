package org.nuclearfog.twidda.backend.engine;

import org.nuclearfog.twidda.database.GlobalSettings;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a https proxy connection for all connections
 */
class ProxySetup extends ProxySelector {

    private String proxyHost;
    private int proxyPort;
    private boolean proxySet;


    ProxySetup(GlobalSettings settings) {
        proxySet = settings.isProxyServerSet();

        if (proxySet) {
            proxyHost = settings.getProxyHost();
            proxyPort = Integer.parseInt(settings.getProxyPort());
        }
        if (settings.isProxyLoginSet()) {
            Authenticator.setDefault(new ProxyAuthenticator(settings));
        } else {
            Authenticator.setDefault(null);
        }
    }


    @Override
    public List<Proxy> select(URI uri) {
        Proxy httpsProxy;
        if (proxySet) {
            InetSocketAddress socket = new InetSocketAddress(proxyHost, proxyPort);
            httpsProxy = new Proxy(Proxy.Type.HTTP, socket);
        } else {
            httpsProxy = Proxy.NO_PROXY;
        }
        List<Proxy> result = new ArrayList<>(1);
        result.add(httpsProxy);
        return result;
    }


    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
    }


    /**
     * Creates an authenticator for proxy login
     */
    class ProxyAuthenticator extends Authenticator {

        private String username;
        private char[] password;

        ProxyAuthenticator(GlobalSettings settings) {
            username = settings.getProxyUser();
            password = settings.getProxyPass().toCharArray();
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }
}