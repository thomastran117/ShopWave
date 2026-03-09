package backend.configurations.application;

import backend.http.ClientInfo;
import backend.http.ClientRequestContext;
import backend.http.DeviceType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ClientRequestFilter extends OncePerRequestFilter {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private static final Pattern BOT_PATTERN =
            Pattern.compile("bot|crawl|spider|slurp|mediapartners|apis-google|feedfetcher|lighthouse", Pattern.CASE_INSENSITIVE);

    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("Mobile|iPhone|iPod|Android.*Mobile|Windows Phone|BlackBerry|Opera Mini|IEMobile", Pattern.CASE_INSENSITIVE);

    private static final Pattern TABLET_PATTERN =
            Pattern.compile("iPad|Android(?!.*Mobile)|Tablet|Kindle|Silk|PlayBook", Pattern.CASE_INSENSITIVE);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "";
        }

        ClientInfo info = new ClientInfo(
                resolveIp(request),
                resolveDeviceType(userAgent),
                resolveBrowser(userAgent),
                resolveOs(userAgent),
                userAgent
        );

        ClientRequestContext.store(request, info);
        filterChain.doFilter(request, response);
    }

    private String resolveIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                return value.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private DeviceType resolveDeviceType(String ua) {
        if (ua.isEmpty()) {
            return DeviceType.UNKNOWN;
        }
        if (BOT_PATTERN.matcher(ua).find()) {
            return DeviceType.BOT;
        }
        if (TABLET_PATTERN.matcher(ua).find()) {
            return DeviceType.TABLET;
        }
        if (MOBILE_PATTERN.matcher(ua).find()) {
            return DeviceType.MOBILE;
        }
        return DeviceType.DESKTOP;
    }

    private String resolveBrowser(String ua) {
        if (ua.isEmpty()) return "Unknown";

        if (ua.contains("Edg/"))   return extractVersion(ua, "Edg/([\\d.]+)", "Edge");
        if (ua.contains("OPR/") || ua.contains("Opera"))
                                    return extractVersion(ua, "OPR/([\\d.]+)", "Opera");
        if (ua.contains("Chrome/") && !ua.contains("Chromium"))
                                    return extractVersion(ua, "Chrome/([\\d.]+)", "Chrome");
        if (ua.contains("Firefox/"))return extractVersion(ua, "Firefox/([\\d.]+)", "Firefox");
        if (ua.contains("Safari/") && ua.contains("Version/"))
                                    return extractVersion(ua, "Version/([\\d.]+)", "Safari");
        if (ua.contains("MSIE") || ua.contains("Trident/"))
                                    return "Internet Explorer";
        return "Unknown";
    }

    private String resolveOs(String ua) {
        if (ua.isEmpty()) return "Unknown";

        if (ua.contains("iPhone") || ua.contains("iPad") || ua.contains("iPod"))
            return extractVersion(ua, "OS ([\\d_]+)", "iOS").replace('_', '.');
        if (ua.contains("Android"))
            return extractVersion(ua, "Android ([\\d.]+)", "Android");
        if (ua.contains("Windows"))
            return extractVersion(ua, "Windows NT ([\\d.]+)", "Windows");
        if (ua.contains("Mac OS X"))
            return extractVersion(ua, "Mac OS X ([\\d_.]+)", "macOS").replace('_', '.');
        if (ua.contains("Linux"))
            return "Linux";
        return "Unknown";
    }

    private String extractVersion(String ua, String regex, String name) {
        Matcher m = Pattern.compile(regex).matcher(ua);
        return m.find() ? name + " " + m.group(1) : name;
    }
}
