package backend.http;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class ClientRequestContext {

    public static final String ATTRIBUTE_KEY = "clientInfo";

    private ClientRequestContext() {}

    /**
     * Returns the {@link ClientInfo} for the current HTTP request, or {@link ClientInfo#UNKNOWN}
     * when called outside an HTTP request context (async threads, non-servlet dispatches, etc.).
     */
    public static ClientInfo get() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
            return ClientInfo.UNKNOWN;
        }
        Object value = servletAttrs.getRequest().getAttribute(ATTRIBUTE_KEY);
        return value instanceof ClientInfo info ? info : ClientInfo.UNKNOWN;
    }

    public static void store(HttpServletRequest request, ClientInfo info) {
        request.setAttribute(ATTRIBUTE_KEY, info);
    }

    public static void clear(HttpServletRequest request) {
        request.removeAttribute(ATTRIBUTE_KEY);
    }
}
