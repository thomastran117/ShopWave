package backend.http;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class ClientRequestContext {

    public static final String ATTRIBUTE_KEY = "clientInfo";

    private ClientRequestContext() {}

    public static ClientInfo get() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        return (ClientInfo) attrs.getRequest().getAttribute(ATTRIBUTE_KEY);
    }

    public static void store(HttpServletRequest request, ClientInfo info) {
        request.setAttribute(ATTRIBUTE_KEY, info);
    }
}
