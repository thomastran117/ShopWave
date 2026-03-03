package backend.configurations.environment;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties(prefix = "app")
public class EnvironmentSetting {

    private final Cors cors = new Cors();
    private final Security security = new Security();
    private final Redis redis = new Redis();
    private final Database database = new Database();
    private final Cache cache = new Cache();
    private final Recaptcha recaptcha = new Recaptcha();

    public Cors getCors() {
        return cors;
    }

    public Recaptcha getRecaptcha() {
        return recaptcha;
    }

    public Security getSecurity() {
        return security;
    }

    public Redis getRedis() {
        return redis;
    }

    public Database getDatabase() {
        return database;
    }

    public Cache getCache() {
        return cache;
    }

    public static class Cors {
        private String allowedOrigins = "";

        public List<String> getAllowedOrigins() {
            if (allowedOrigins == null || allowedOrigins.isBlank()) {
                return List.of("*");
            }
            return Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins != null ? allowedOrigins : "";
        }
    }

    public static class Security {
        private final Jwt jwt = new Jwt();

        private String googleClientId = "";
        private String recaptchaSecretKey = "";

        public Jwt getJwt() {
            return jwt;
        }

        public String getGoogleClientId() {
            return googleClientId != null ? googleClientId : "";
        }

        public void setGoogleClientId(String googleClientId) {
            this.googleClientId = googleClientId != null ? googleClientId : "";
        }

        public String getRecaptchaSecretKey() {
            return recaptchaSecretKey != null ? recaptchaSecretKey : "";
        }

        public void setRecaptchaSecretKey(String recaptchaSecretKey) {
            this.recaptchaSecretKey = recaptchaSecretKey != null ? recaptchaSecretKey : "";
        }

        public static class Jwt {
            private String secret = "change-me-in-env";
            private long accessTokenTtlSeconds = 900;
            private long refreshTokenTtlSeconds = 604800;
            private String issuer = "shopwave-api";

            public String getSecret() {
                return secret != null ? secret : "";
            }

            public void setSecret(String secret) {
                this.secret = secret != null ? secret : "";
            }

            public long getAccessTokenTtlSeconds() {
                return accessTokenTtlSeconds;
            }

            public void setAccessTokenTtlSeconds(long accessTokenTtlSeconds) {
                this.accessTokenTtlSeconds = accessTokenTtlSeconds;
            }

            public long getRefreshTokenTtlSeconds() {
                return refreshTokenTtlSeconds;
            }

            public void setRefreshTokenTtlSeconds(long refreshTokenTtlSeconds) {
                this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
            }

            public String getIssuer() {
                return issuer != null ? issuer : "";
            }

            public void setIssuer(String issuer) {
                this.issuer = issuer != null ? issuer : "";
            }
        }
    }

    /**
     * reCAPTCHA verification: timeouts and retry/backoff for siteverify API calls.
     * Fail closed: on 5xx or timeout after retries, verification returns false.
     */
    public static class Recaptcha {
        private int connectTimeoutMs = 3_000;
        private int readTimeoutMs = 5_000;
        private final Retry retry = new Retry();

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = Math.max(1_000, Math.min(30_000, connectTimeoutMs));
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = Math.max(1_000, Math.min(60_000, readTimeoutMs));
        }

        public Retry getRetry() {
            return retry;
        }

        public static class Retry {
            private int maxAttempts = 4;
            private long initialIntervalMs = 200;
            private double multiplier = 2.0;
            private long maxIntervalMs = 10_000;

            public int getMaxAttempts() {
                return maxAttempts;
            }

            public void setMaxAttempts(int maxAttempts) {
                this.maxAttempts = Math.max(1, Math.min(10, maxAttempts));
            }

            public long getInitialIntervalMs() {
                return initialIntervalMs;
            }

            public void setInitialIntervalMs(long initialIntervalMs) {
                this.initialIntervalMs = Math.max(50, Math.min(60_000, initialIntervalMs));
            }

            public double getMultiplier() {
                return multiplier;
            }

            public void setMultiplier(double multiplier) {
                this.multiplier = Math.max(1.0, Math.min(5.0, multiplier));
            }

            public long getMaxIntervalMs() {
                return maxIntervalMs;
            }

            public void setMaxIntervalMs(long maxIntervalMs) {
                this.maxIntervalMs = Math.max(1_000, Math.min(300_000, maxIntervalMs));
            }
        }
    }

    public static class Cache {
        private String namespace = "app";

        public String getNamespace() {
            return namespace != null ? namespace : "app";
        }

        public void setNamespace(String namespace) {
            this.namespace = (namespace != null && !namespace.isBlank()) ? namespace : "app";
        }
    }

    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private String password = "";
        private int database = 0;
        private int timeout = 2000;

        public String getHost() {
            return host != null ? host : "localhost";
        }

        public void setHost(String host) {
            this.host = host != null ? host : "localhost";
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getPassword() {
            return password != null ? password : "";
        }

        public void setPassword(String password) {
            this.password = password != null ? password : "";
        }

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }

    public static class Database {
        private String url = "jdbc:mysql://localhost:3306/shopland";
        private String username = "root";
        private String password = "password123";
        private String driverClassName = "com.mysql.cj.jdbc.Driver";
        private int maximumPoolSize = 10;
        private int minimumIdle = 2;
        private long connectionTimeout = 30_000;
        private long idleTimeout = 600_000;

        public String getUrl() {
            return url != null ? url : "";
        }

        public void setUrl(String url) {
            this.url = url != null ? url : "";
        }

        public String getUsername() {
            return username != null ? username : "";
        }

        public void setUsername(String username) {
            this.username = username != null ? username : "";
        }

        public String getPassword() {
            return password != null ? password : "";
        }

        public void setPassword(String password) {
            this.password = password != null ? password : "";
        }

        public String getDriverClassName() {
            return driverClassName != null ? driverClassName : "com.mysql.cj.jdbc.Driver";
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName != null ? driverClassName : "com.mysql.cj.jdbc.Driver";
        }

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public int getMinimumIdle() {
            return minimumIdle;
        }

        public void setMinimumIdle(int minimumIdle) {
            this.minimumIdle = minimumIdle;
        }

        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public long getIdleTimeout() {
            return idleTimeout;
        }

        public void setIdleTimeout(long idleTimeout) {
            this.idleTimeout = idleTimeout;
        }
    }
}