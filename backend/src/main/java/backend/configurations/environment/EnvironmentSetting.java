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

    public Cors getCors() {
        return cors;
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

    public static class Cors {
        private String allowedOrigins = "http://localhost:3090,http://localhost:5173";

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

        public Jwt getJwt() {
            return jwt;
        }

        public String getGoogleClientId() {
            return googleClientId != null ? googleClientId : "";
        }

        public void setGoogleClientId(String googleClientId) {
            this.googleClientId = googleClientId != null ? googleClientId : "";
        }

        public static class Jwt {
            private String secret = "change-me-in-env";
            private long accessTokenTtlSeconds = 900;
            private long refreshTokenTtlSeconds = 604800; // 7 days
            private String issuer = "easyfood-api";

            public String getSecret() {
                return secret;
            }

            public void setSecret(String secret) {
                this.secret = secret;
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
                return issuer;
            }

            public void setIssuer(String issuer) {
                this.issuer = issuer;
            }
        }
    }

    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private String password = "";
        private int database = 0;
        private int timeout = 2000;

        public String getHost() {
            return host;
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
            return password;
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

    // --- Database (MySQL) ---

    public static class Database {
        private String url = "jdbc:mysql://localhost:3306/easyfood";
        private String username = "root";
        private String password = "";
        private String driverClassName = "com.mysql.cj.jdbc.Driver";
        private int maximumPoolSize = 10;
        private int minimumIdle = 2;
        private long connectionTimeout = 30_000;
        private long idleTimeout = 600_000;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url != null ? url : "";
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username != null ? username : "";
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password != null ? password : "";
        }

        public String getDriverClassName() {
            return driverClassName;
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
