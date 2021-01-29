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
 * Creates a https proxy connection for all connections except Twitter4J
 *
 * @author nuclearfog
 */
class ProxySetup {

    private ProxySetup() {
    }

    /**
     * initializes the proxy connection with login
     *
     * @param settings App settings
     */
    static void setConnection(GlobalSettings settings) {
        ProxyConnection proxyConnection;
        ProxyAuthenticator proxyLogin;

        if (settings.isProxyEnabled()) {
            proxyConnection = new ProxyConnection(settings);
        } else {
            proxyConnection = new ProxyConnection();
        }
        if (settings.isProxyAuthSet()) {
            proxyLogin = new ProxyAuthenticator(settings);
        } else {
            proxyLogin = new ProxyAuthenticator();
        }
        try {
            ProxySelector.setDefault(proxyConnection);
            Authenticator.setDefault(proxyLogin);
        } catch (SecurityException sErr) {
            sErr.printStackTrace();
        }
    }


    /**
     * Connect to a proxy server
     */
    private static class ProxyConnection extends ProxySelector {

        private List<Proxy> proxyList;

        /**
         * Creates a direct connection without proxy
         */
        ProxyConnection() {
            proxyList = new ArrayList<>(1);
            proxyList.add(Proxy.NO_PROXY);
        }

        /**
         * set system proxy for all http requests
         *
         * @param settings App settings
         */
        ProxyConnection(GlobalSettings settings) {
            String proxyHost = settings.getProxyHost();
            int proxyPort = settings.getProxyPortNumber();
            InetSocketAddress socket = new InetSocketAddress(proxyHost, proxyPort);
            Proxy httpsProxy = new Proxy(Proxy.Type.HTTP, socket);
            proxyList = new ArrayList<>(1);
            proxyList.add(httpsProxy);
        }

        @Override
        public List<Proxy> select(URI uri) {
            return proxyList;
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            // ignore to force using proxy and avoid data leak
        }
    }


    /**
     * Creates an authenticator for proxy login
     */
    private static class ProxyAuthenticator extends Authenticator {

        private static final String NO_NM = "";
        private static final char[] NO_PW = {};

        private PasswordAuthentication proxyPass;

        /**
         * unset all login information for proxy
         */
        ProxyAuthenticator() {
            proxyPass = new PasswordAuthentication(NO_NM, NO_PW);
        }

        /**
         * set proxy login
         *
         * @param settings App settings
         */
        ProxyAuthenticator(GlobalSettings settings) {
            String username = settings.getProxyUser();
            char[] password = settings.getProxyPass().toCharArray();
            proxyPass = new PasswordAuthentication(username, password);
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return proxyPass;
        }
    }
}