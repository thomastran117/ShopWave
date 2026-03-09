package backend.configurations.application;

import backend.configurations.environment.EnvironmentSetting;
import backend.http.ClientInfo;
import backend.http.ClientRequestContext;
import backend.http.DeviceType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ClientRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ClientRequestFilter.class);

    private static final int MAX_UA_LENGTH = 512;

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR"
    };

    // ── Device-type patterns ───────────────────────────────────────────

    private static final Pattern BOT_PATTERN = Pattern.compile(
            "bot|crawl|spider|slurp|mediapartners|apis-google|feedfetcher|lighthouse",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "Mobile|iPhone|iPod|Android.*Mobile|Windows Phone|BlackBerry|Opera Mini|IEMobile",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TABLET_PATTERN = Pattern.compile(
            "iPad|Android(?!.*Mobile)|Tablet|Kindle|Silk|PlayBook",
            Pattern.CASE_INSENSITIVE);

    // ── Browser version patterns (precompiled) ─────────────────────────

    private static final Pattern EDGE_VERSION    = Pattern.compile("Edg/([\\d.]+)");
    private static final Pattern OPERA_VERSION   = Pattern.compile("OPR/([\\d.]+)");
    private static final Pattern CHROME_VERSION  = Pattern.compile("Chrome/([\\d.]+)");
    private static final Pattern FIREFOX_VERSION = Pattern.compile("Firefox/([\\d.]+)");
    private static final Pattern SAFARI_VERSION  = Pattern.compile("Version/([\\d.]+)");

    // ── OS version patterns (precompiled) ──────────────────────────────

    private static final Pattern IOS_VERSION     = Pattern.compile("OS ([\\d_]+)");
    private static final Pattern ANDROID_VERSION = Pattern.compile("Android ([\\d.]+)");
    private static final Pattern WINDOWS_VERSION = Pattern.compile("Windows NT ([\\d.]+)");
    private static final Pattern MACOS_VERSION   = Pattern.compile("Mac OS X ([\\d_.]+)");

    private final List<String> trustedProxies;

    public ClientRequestFilter(EnvironmentSetting settings) {
        this.trustedProxies = settings.getProxy().getTrustedProxies();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String rawUa = request.getHeader("User-Agent");
        String ua = sanitizeUa(rawUa);

        ClientInfo info = new ClientInfo(
                resolveIp(request),
                resolveDeviceType(ua),
                resolveBrowser(ua),
                resolveOs(ua),
                ua
        );

        ClientRequestContext.store(request, info);
        try {
            filterChain.doFilter(request, response);
        } finally {
            ClientRequestContext.clear(request);
        }
    }

    // ── IP resolution ──────────────────────────────────────────────────

    private String resolveIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        if (isTrustedProxy(remoteAddr)) {
            for (String header : IP_HEADERS) {
                String value = request.getHeader(header);
                if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                    return value.split(",")[0].trim();
                }
            }
        }

        return remoteAddr;
    }

    private boolean isTrustedProxy(String remoteAddr) {
        if (trustedProxies.isEmpty()) {
            return false;
        }
        try {
            InetAddress remote = InetAddress.getByName(remoteAddr);
            for (String entry : trustedProxies) {
                if (entry.contains("/")) {
                    if (matchesCidr(remote, entry)) return true;
                } else {
                    if (remote.equals(InetAddress.getByName(entry))) return true;
                }
            }
        } catch (UnknownHostException e) {
            log.warn("Could not resolve remote address for proxy check: {}", remoteAddr);
        }
        return false;
    }

    private static boolean matchesCidr(InetAddress address, String cidr) {
        try {
            String[] parts = cidr.split("/");
            InetAddress network = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] addrBytes = address.getAddress();
            byte[] netBytes = network.getAddress();
            if (addrBytes.length != netBytes.length) return false;

            int fullBytes = prefixLength / 8;
            int remainingBits = prefixLength % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (addrBytes[i] != netBytes[i]) return false;
            }
            if (remainingBits > 0 && fullBytes < addrBytes.length) {
                int mask = (0xFF << (8 - remainingBits)) & 0xFF;
                if ((addrBytes[fullBytes] & mask) != (netBytes[fullBytes] & mask)) return false;
            }
            return true;
        } catch (UnknownHostException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.warn("Invalid CIDR entry '{}': {}", cidr, e.getMessage());
            return false;
        }
    }

    // ── User-Agent sanitization ────────────────────────────────────────

    private static String sanitizeUa(String raw) {
        if (raw == null || raw.isBlank()) return "";
        return raw.length() > MAX_UA_LENGTH ? raw.substring(0, MAX_UA_LENGTH) : raw;
    }

    // ── Device type ────────────────────────────────────────────────────

    private static DeviceType resolveDeviceType(String ua) {
        if (ua.isEmpty()) return DeviceType.UNKNOWN;
        if (BOT_PATTERN.matcher(ua).find())    return DeviceType.BOT;
        if (TABLET_PATTERN.matcher(ua).find()) return DeviceType.TABLET;
        if (MOBILE_PATTERN.matcher(ua).find()) return DeviceType.MOBILE;
        return DeviceType.DESKTOP;
    }

    // ── Browser detection ──────────────────────────────────────────────

    private static String resolveBrowser(String ua) {
        if (ua.isEmpty()) return ClientInfo.UNKNOWN_VALUE;

        if (ua.contains("Edg/"))                              return extractVersion(ua, EDGE_VERSION, "Edge");
        if (ua.contains("OPR/") || ua.contains("Opera"))     return extractVersion(ua, OPERA_VERSION, "Opera");
        if (ua.contains("Chrome/") && !ua.contains("Chromium")) return extractVersion(ua, CHROME_VERSION, "Chrome");
        if (ua.contains("Firefox/"))                          return extractVersion(ua, FIREFOX_VERSION, "Firefox");
        if (ua.contains("Safari/") && ua.contains("Version/")) return extractVersion(ua, SAFARI_VERSION, "Safari");
        if (ua.contains("MSIE") || ua.contains("Trident/"))  return "Internet Explorer";
        return ClientInfo.UNKNOWN_VALUE;
    }

    // ── OS detection ───────────────────────────────────────────────────

    private static String resolveOs(String ua) {
        if (ua.isEmpty()) return ClientInfo.UNKNOWN_VALUE;

        if (ua.contains("iPhone") || ua.contains("iPad") || ua.contains("iPod"))
            return extractVersion(ua, IOS_VERSION, "iOS").replace('_', '.');
        if (ua.contains("Android"))
            return extractVersion(ua, ANDROID_VERSION, "Android");
        if (ua.contains("Windows"))
            return extractVersion(ua, WINDOWS_VERSION, "Windows");
        if (ua.contains("Mac OS X"))
            return extractVersion(ua, MACOS_VERSION, "macOS").replace('_', '.');
        if (ua.contains("Linux"))
            return "Linux";
        return ClientInfo.UNKNOWN_VALUE;
    }

    // ── Shared version extractor (safe) ────────────────────────────────

    private static String extractVersion(String ua, Pattern pattern, String name) {
        try {
            Matcher m = pattern.matcher(ua);
            return m.find() ? name + " " + m.group(1) : name;
        } catch (Exception e) {
            return name;
        }
    }
}
