package ir.naja.pushsdk.android;

import android.content.Context;
import android.os.Build;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import ir.naja.pushsdk.R;
import okhttp3.ConnectionSpec;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.TlsVersion;

public final class PushSsl {
    private static final String TAG = "PushSsl";
    private static volatile OkHttpClient httpClient;
    private static volatile SSLSocketFactory mqttSocketFactory;
    private static volatile X509TrustManager trustManager;

    private PushSsl() {
    }

    public static synchronized void init(Context context) {
        if (httpClient != null) {
            return;
        }
        Context app = context.getApplicationContext();
        try {
            PushLog.i("ssl init start sdk=" + Build.VERSION.SDK_INT
                    + " release=" + Build.VERSION.RELEASE
                    + " device=" + Build.MANUFACTURER + "/" + Build.MODEL);

            installConscryptIfNeeded();
            logSecurityProviders();

            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate ca;
//            try (InputStream stream = app.getResources().openRawResource(R.raw.push_ca)) {//mian
            try (InputStream stream = app.getResources().openRawResource(R.raw.ca)) {//local
                ca = factory.generateCertificate(stream);
            }

            KeyStore customStore = KeyStore.getInstance(KeyStore.getDefaultType());
            customStore.load(null, null);
//            customStore.setCertificateEntry("push_ca", ca);//main
            customStore.setCertificateEntry("ca", ca);//local

            TrustManagerFactory customTmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
            );
            customTmf.init(customStore);

            TrustManagerFactory defaultTmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
            );
            defaultTmf.init((KeyStore) null);

            X509TrustManager customTrust = findX509(customTmf.getTrustManagers());
            X509TrustManager defaultTrust = findX509(defaultTmf.getTrustManagers());
            trustManager = new CompositeTrustManager(customTrust, defaultTrust);

            SSLContext sslContext = createSslContext();
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            SSLSocketFactory baseFactory = sslContext.getSocketFactory();
            mqttSocketFactory = new LoggingSslSocketFactory(baseFactory);

            logSocketFactoryCaps(baseFactory);

            List<ConnectionSpec> specs = buildConnectionSpecs();
            PushLog.i("okhttp ConnectionSpecs=" + specs);

            httpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(mqttSocketFactory, trustManager)
                    .connectionSpecs(specs)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .eventListener(new TlsEventListener())
                    .build();

            String subject = ca instanceof X509Certificate
                    ? ((X509Certificate) ca).getSubjectDN().toString()
                    : String.valueOf(ca.getType());
            PushLog.i("Push CA loaded subject=" + subject
                    + " mqttFactory=" + (mqttSocketFactory != null)
                    + " sslProtocol=" + sslContext.getProtocol());
        } catch (Exception e) {
            PushLog.e("Failed to load push CA, using default client: "
                    + e.getClass().getSimpleName() + " " + e.getMessage(), e);
            httpClient = new OkHttpClient.Builder()
                    .connectionSpecs(buildConnectionSpecs())
                    .eventListener(new TlsEventListener())
                    .build();
            mqttSocketFactory = null;
        }
    }

    /** Force re-init (e.g. after push restart) so new TLS settings apply. */
    public static synchronized void reset() {
        httpClient = null;
        mqttSocketFactory = null;
        trustManager = null;
        PushLog.i("ssl reset — next init will rebuild client");
    }

    private static void installConscryptIfNeeded() {
        try {
            Class<?> conscrypt = Class.forName("org.conscrypt.Conscrypt");
            Boolean available = (Boolean) conscrypt.getMethod("isAvailable").invoke(null);
            if (available == null || !available) {
                PushLog.w("Conscrypt reported unavailable");
                return;
            }
            Object provider = conscrypt.getMethod("newProvider").invoke(null);
            // Prefer Conscrypt over platform BoringSSL so TLS 1.3 works on older devices.
            int pos = Security.insertProviderAt((Provider) provider, 1);
            PushLog.i("Conscrypt installed position=" + pos
                    + " (1=primary). Enables TLS1.3 on older Android.");
        } catch (ClassNotFoundException e) {
            PushLog.w("Conscrypt not on classpath — device TLS stack only");
        } catch (Throwable t) {
            PushLog.w("Conscrypt install failed: " + t.getClass().getSimpleName()
                    + " " + t.getMessage());
        }
    }

    private static SSLContext createSslContext() throws Exception {
        // Use generic "TLS" so TLSv1.3 can be negotiated when Conscrypt/platform supports it.
        // Do NOT lock to "TLSv1.2" — servers that prefer TLS1.3 send inappropriate_fallback.
        String[] candidates = new String[]{"TLS", "TLSv1.3", "TLSv1.2"};
        Exception last = null;
        for (String name : candidates) {
            try {
                SSLContext ctx = SSLContext.getInstance(name);
                PushLog.i("SSLContext created with protocol=" + name
                        + " provider=" + ctx.getProvider().getName()
                        + "/" + ctx.getProvider().getVersion());
                return ctx;
            } catch (Exception e) {
                last = e;
                PushLog.w("SSLContext " + name + " unavailable: " + e.getMessage());
            }
        }
        throw last != null ? last : new IllegalStateException("No SSLContext");
    }

    private static List<ConnectionSpec> buildConnectionSpecs() {
        // Single modern spec — no COMPATIBLE_TLS retry (that triggers TLS_FALLBACK_SCSV
        // → server alert inappropriate_fallback when TLS1.3 is expected).
        ConnectionSpec modern = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
                .allEnabledCipherSuites()
                .build();
        return Collections.singletonList(modern);
    }

    private static void logSecurityProviders() {
        try {
            Provider[] providers = Security.getProviders();
            StringBuilder sb = new StringBuilder("Security providers:");
            for (Provider p : providers) {
                sb.append(" [").append(p.getName()).append(" ").append(p.getVersion()).append("]");
            }
            PushLog.i(sb.toString());
        } catch (Exception e) {
            PushLog.w("Could not list security providers: " + e.getMessage());
        }
    }

    private static void logSocketFactoryCaps(SSLSocketFactory factory) {
        try {
            SSLSocket probe = (SSLSocket) factory.createSocket();
            try {
                PushLog.i("SSL supportedProtocols="
                        + Arrays.toString(probe.getSupportedProtocols()));
                PushLog.i("SSL default enabledProtocols="
                        + Arrays.toString(probe.getEnabledProtocols()));
                String[] ciphers = probe.getSupportedCipherSuites();
                PushLog.i("SSL supportedCipherCount=" + ciphers.length
                        + " sample=" + sample(ciphers, 8));
                PushLog.i("SSL default enabledCipherCount="
                        + probe.getEnabledCipherSuites().length
                        + " sample=" + sample(probe.getEnabledCipherSuites(), 8));
            } finally {
                try {
                    probe.close();
                } catch (IOException ignored) {
                }
            }
        } catch (Exception e) {
            PushLog.w("Could not probe SSLSocket caps: " + e.getMessage());
        }
    }

    private static String sample(String[] items, int max) {
        if (items == null || items.length == 0) {
            return "[]";
        }
        int n = Math.min(max, items.length);
        return Arrays.toString(Arrays.copyOf(items, n))
                + (items.length > max ? " …+" + (items.length - max) : "");
    }

    public static OkHttpClient client(Context context) {
        if (httpClient == null && context != null) {
            init(context);
        }
        return httpClient != null ? httpClient : new OkHttpClient();
    }

    public static SSLSocketFactory mqttSocketFactory(Context context) {
        if (mqttSocketFactory == null && context != null) {
            init(context);
        }
        return mqttSocketFactory;
    }

    private static X509TrustManager findX509(TrustManager[] managers) {
        for (TrustManager manager : managers) {
            if (manager instanceof X509TrustManager) {
                return (X509TrustManager) manager;
            }
        }
        throw new IllegalStateException("No X509TrustManager found");
    }

    /** Forces TLS 1.2+ on every socket and logs handshake result. */
    private static final class LoggingSslSocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory delegate;

        LoggingSslSocketFactory(SSLSocketFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose)
                throws IOException {
            return configure((SSLSocket) delegate.createSocket(s, host, port, autoClose), host, port);
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return configure((SSLSocket) delegate.createSocket(host, port), host, port);
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
                throws IOException {
            return configure(
                    (SSLSocket) delegate.createSocket(host, port, localHost, localPort),
                    host,
                    port
            );
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return configure(
                    (SSLSocket) delegate.createSocket(host, port),
                    host.getHostAddress(),
                    port
            );
        }

        @Override
        public Socket createSocket(
                InetAddress address,
                int port,
                InetAddress localAddress,
                int localPort
        ) throws IOException {
            return configure(
                    (SSLSocket) delegate.createSocket(address, port, localAddress, localPort),
                    address.getHostAddress(),
                    port
            );
        }

        private SSLSocket configure(SSLSocket socket, String host, int port) {
            try {
                String[] supported = socket.getSupportedProtocols();
                List<String> enabled = new ArrayList<>();
                for (String p : supported) {
                    if ("TLSv1.3".equals(p) || "TLSv1.2".equals(p)) {
                        enabled.add(p);
                    }
                }
                if (enabled.isEmpty()) {
                    enabled.addAll(Arrays.asList(socket.getEnabledProtocols()));
                }
                // Prefer offering TLS1.3 first in the ClientHello when available.
                Collections.sort(enabled, (a, b) -> {
                    if ("TLSv1.3".equals(a)) return -1;
                    if ("TLSv1.3".equals(b)) return 1;
                    return b.compareTo(a);
                });
                socket.setEnabledProtocols(enabled.toArray(new String[0]));
                boolean has13 = enabled.contains("TLSv1.3");
                PushLog.i("SSLSocket open host=" + host + ":" + port
                        + " supportedProtocols=" + Arrays.toString(supported)
                        + " enabledProtocols=" + enabled
                        + " hasTls13=" + has13
                        + " enabledCipherCount=" + socket.getEnabledCipherSuites().length);
                if (!has13) {
                    PushLog.w("Device/provider has NO TLS 1.3 — server may reject with "
                            + "handshake_failure / inappropriate_fallback");
                }
            } catch (Exception e) {
                PushLog.w("SSLSocket configure failed: " + e.getMessage());
            }
            socket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
                @Override
                public void handshakeCompleted(HandshakeCompletedEvent event) {
                    try {
                        SSLSession session = event.getSession();
                        PushLog.i("SSL handshake OK host=" + host
                                + " protocol=" + session.getProtocol()
                                + " cipher=" + session.getCipherSuite()
                                + " peer=" + (session.getPeerPrincipal() != null
                                ? session.getPeerPrincipal().getName() : "n/a"));
                    } catch (Exception e) {
                        PushLog.w("handshakeCompleted log failed: " + e.getMessage());
                    }
                }
            });
            return socket;
        }
    }

    private static final class TlsEventListener extends EventListener {
        @Override
        public void secureConnectStart(okhttp3.Call call) {
            PushLog.i("TLS secureConnectStart url=" + call.request().url());
        }

        @Override
        public void secureConnectEnd(okhttp3.Call call, Handshake handshake) {
            if (handshake == null) {
                PushLog.w("TLS secureConnectEnd handshake=null url=" + call.request().url());
                return;
            }
            PushLog.i("TLS secureConnectEnd url=" + call.request().url()
                    + " tls=" + handshake.tlsVersion()
                    + " cipher=" + handshake.cipherSuite()
                    + " peerCerts=" + handshake.peerCertificates().size()
                    + " protocol=" + call.request().url().scheme());
        }

        @Override
        public void connectFailed(
                okhttp3.Call call,
                java.net.InetSocketAddress inetSocketAddress,
                java.net.Proxy proxy,
                Protocol protocol,
                IOException ioe
        ) {
            PushLog.e("TLS connectFailed addr=" + inetSocketAddress
                    + " proxy=" + proxy
                    + " protocol=" + protocol
                    + " url=" + call.request().url()
                    + " err=" + PushLog.errorSummary(ioe), ioe);
            if (ioe != null) {
                Throwable c = ioe.getCause();
                int depth = 0;
                while (c != null && depth < 5) {
                    PushLog.e("TLS cause[" + depth + "]="
                            + c.getClass().getSimpleName() + ": " + c.getMessage());
                    c = c.getCause();
                    depth++;
                }
            }
        }
    }

    private static final class CompositeTrustManager implements X509TrustManager {
        private final X509TrustManager custom;
        private final X509TrustManager system;

        CompositeTrustManager(X509TrustManager custom, X509TrustManager system) {
            this.custom = custom;
            this.system = system;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws java.security.cert.CertificateException {
            try {
                custom.checkClientTrusted(chain, authType);
            } catch (java.security.cert.CertificateException e) {
                system.checkClientTrusted(chain, authType);
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws java.security.cert.CertificateException {
            String subject = (chain != null && chain.length > 0)
                    ? chain[0].getSubjectDN().toString()
                    : "empty";
            String issuer = (chain != null && chain.length > 0)
                    ? chain[0].getIssuerDN().toString()
                    : "empty";
            PushLog.i("checkServerTrusted authType=" + authType
                    + " chainLen=" + (chain == null ? 0 : chain.length)
                    + " subject=" + subject
                    + " issuer=" + issuer);
            try {
                custom.checkServerTrusted(chain, authType);
                PushLog.i("checkServerTrusted custom CA accepted");
            } catch (java.security.cert.CertificateException e) {
                PushLog.w("custom CA rejected, trying system: " + e.getMessage());
                try {
                    system.checkServerTrusted(chain, authType);
                    PushLog.i("checkServerTrusted system CA accepted");
                } catch (java.security.cert.CertificateException e2) {
                    PushLog.e("checkServerTrusted both CAs rejected: " + e2.getMessage(), e2);
                    throw e2;
                }
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            X509Certificate[] customIssuers = custom.getAcceptedIssuers();
            X509Certificate[] systemIssuers = system.getAcceptedIssuers();
            X509Certificate[] all = Arrays.copyOf(
                    customIssuers,
                    customIssuers.length + systemIssuers.length
            );
            System.arraycopy(systemIssuers, 0, all, customIssuers.length, systemIssuers.length);
            return all;
        }
    }
}
