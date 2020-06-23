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

import static java.net.Authenticator.RequestorType.PROXY;

/**
 * Creates a https proxy connection for all connections
 */
abstract class ProxySetup {

    static void setConnection(GlobalSettings settings) {
        ProxyConnection proxyConnection;
        ProxyAuthenticator proxyLogin;

        if (settings.isProxyServerSet()) {
            proxyConnection = new ProxyConnection(settings);
            if (settings.isProxyLoginSet()) {
                proxyLogin = new ProxyAuthenticator(settings);
            } else {
                proxyLogin = new ProxyAuthenticator();
            }
        } else {
            proxyConnection = new ProxyConnection();
            proxyLogin = new ProxyAuthenticator();
        }
        ProxySelector.setDefault(proxyConnection);
        Authenticator.setDefault(proxyLogin);
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

        @Override
        protected RequestorType getRequestorType() {
            return PROXY;
        }
    }
}