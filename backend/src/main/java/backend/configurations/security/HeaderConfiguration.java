package backend.configurations.security;

import org.springframework.context.annotation.Configuration;

@Configuration
public class HeaderConfiguration {

    public String contentSecurityPolicy() {
        return "default-src 'self'; " +
                "img-src 'self' data:; " +
                "script-src 'self'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "frame-ancestors 'none';";
    }

    public String referrerPolicy() {
        return "strict-origin-when-cross-origin";
    }

    public String permissionsPolicy() {
        return "geolocation=(), microphone=(), camera=()";
    }
}
