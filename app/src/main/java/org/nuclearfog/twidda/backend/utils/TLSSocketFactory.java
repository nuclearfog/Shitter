package org.nuclearfog.twidda.backend.utils;

import android.os.Build;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Enable Experimental TLS 1.2 support for devices lower than android 21
 *
 * @author fkrauthan
 * @see <a href="https://gist.githubusercontent.com/fkrauthan/ac8624466a4dee4fd02f/raw/309efc30e31c96a932ab9d19bf4d73b286b00573/TLSSocketFactory.java"/>
 */
public class TLSSocketFactory extends SSLSocketFactory {

    private static final String TLS_1_1 = "TLSv1.1";
    private static final String TLS_1_2 = "TLSv1.2";
    private static final String TLS_1_3 = "TLSv1.3";

    /**
     * protocols required by Twitter API
     */
    private static final String[] PROTOCOLS = {TLS_1_1, TLS_1_2};

    private SSLSocketFactory internalSSLSocketFactory;

    /**
     * check if TLS 1.2 is enabled and enable experimental TLS 1.2 support on Pre-Lollipop devices
     */
    public static void getSupportTLSifNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // check for TLS 1.2 support and activate it
            try {
                boolean tlsEnabled = false;
                SSLParameters param = SSLContext.getDefault().getDefaultSSLParameters();
                String[] protocols = param.getProtocols();
                for (String protocol : protocols) {
                    if (protocol.equals(TLS_1_2) || protocol.equals(TLS_1_3)) {
                        tlsEnabled = true;
                        break;
                    }
                }
                if (!tlsEnabled) {
                    HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory());
                }
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }

    /**
     *
     */
    TLSSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);
        internalSSLSocketFactory = context.getSocketFactory();
    }


    @Override
    public String[] getDefaultCipherSuites() {
        return internalSSLSocketFactory.getDefaultCipherSuites();
    }


    @Override
    public String[] getSupportedCipherSuites() {
        return internalSSLSocketFactory.getSupportedCipherSuites();
    }


    @Override
    public Socket createSocket() throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket());
    }


    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose));
    }


    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }


    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localHost, localPort));
    }


    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }


    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(address, port, localAddress, localPort));
    }


    private Socket enableTLSOnSocket(Socket socket) {
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(PROTOCOLS);
        }
        return socket;
    }
}