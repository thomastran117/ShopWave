package backend.http;

import com.google.api.client.http.javanet.ConnectionFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Opens HTTP connections with connect and read timeouts applied so that
 * long network stalls do not block threads.
 *
 * @deprecated Prefer {@link OAuthGoogleHttpTransportFactory} (Apache HttpClient) for
 * production-grade timeout and connection pooling. This implementation uses
 * {@link java.net.HttpURLConnection} and remains available for non-OAuth use if needed.
 */
@Deprecated
public final class TimeoutConnectionFactory implements ConnectionFactory {

    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public TimeoutConnectionFactory(int connectTimeoutMs, int readTimeoutMs) {
        this.connectTimeoutMs = Math.max(0, connectTimeoutMs);
        this.readTimeoutMs = Math.max(0, readTimeoutMs);
    }

    @Override
    public HttpURLConnection openConnection(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (connectTimeoutMs > 0) {
            conn.setConnectTimeout(connectTimeoutMs);
        }
        if (readTimeoutMs > 0) {
            conn.setReadTimeout(readTimeoutMs);
        }
        return conn;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }
}
