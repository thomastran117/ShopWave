package backend.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class EnvConfig {

    // Database
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    // Redis
    private String redisHost;
    private int redisPort;
    private String redisPassword;

    // JWT
    private String jwtSecret;
    private long jwtValidity;
    private long jwtRefreshValidity;

    // Google
    private String googleClientId;

    // --- Getters/Setters ---
    public String getDbUrl() { return dbUrl; }
    public void setDbUrl(String dbUrl) { this.dbUrl = dbUrl; }

    public String getDbUser() { return dbUser; }
    public void setDbUser(String dbUser) { this.dbUser = dbUser; }

    public String getDbPassword() { return dbPassword; }
    public void setDbPassword(String dbPassword) { this.dbPassword = dbPassword; }

    public String getRedisHost() { return redisHost; }
    public void setRedisHost(String redisHost) { this.redisHost = redisHost; }

    public int getRedisPort() { return redisPort; }
    public void setRedisPort(int redisPort) { this.redisPort = redisPort; }

    public String getRedisPassword() { return redisPassword; }
    public void setRedisPassword(String redisPassword) { this.redisPassword = redisPassword; }

    public String getJwtSecret() { return jwtSecret; }
    public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }

    public long getJwtValidity() { return jwtValidity; }
    public void setJwtValidity(long jwtValidity) { this.jwtValidity = jwtValidity; }

    public long getJwtRefreshValidity() { return jwtRefreshValidity; }
    public void setJwtRefreshValidityy(long jwtRefreshValidity) { this.jwtRefreshValidity = jwtRefreshValidity; }

    public String getGoogleClientId() { return googleClientId; }
    public void setGoogleClientId(String googleClientId) { this.googleClientId = googleClientId; }
}
