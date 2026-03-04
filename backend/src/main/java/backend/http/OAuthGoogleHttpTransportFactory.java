package backend.http;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Builds an Apache HttpClient-backed {@link HttpTransport} for Google OAuth (e.g. cert fetch)
 * with configurable timeouts and connection pooling. Prefer this over {@link TimeoutConnectionFactory}
 * (HttpURLConnection) for production-grade timeout and pooling behavior.
 */
public final class OAuthGoogleHttpTransportFactory {

    private static final int DEFAULT_MAX_PER_ROUTE = 5;
    private static final int DEFAULT_MAX_TOTAL = 20;
    private static final int VALIDATE_AFTER_INACTIVITY_MS = 2_000;

    private OAuthGoogleHttpTransportFactory() {}

    /**
     * Builds a shared HttpTransport with the given connect and read timeouts (milliseconds)
     * and a connection pool. Suitable for Google ID token verification (cert fetch).
     */
    public static HttpTransport build(int connectTimeoutMs, int readTimeoutMs) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Math.max(0, connectTimeoutMs))
                .setSocketTimeout(Math.max(0, readTimeoutMs))
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
        connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL);
        connectionManager.setValidateAfterInactivity(VALIDATE_AFTER_INACTIVITY_MS);

        org.apache.http.client.HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();

        return new ApacheHttpTransport(httpClient);
    }
}
