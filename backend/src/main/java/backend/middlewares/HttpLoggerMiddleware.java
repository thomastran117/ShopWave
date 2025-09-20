package backend.middlewares;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class HttpLoggerMiddleware implements Filter {

    static {
        AnsiConsole.systemInstall();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        long start = System.currentTimeMillis();
        chain.doFilter(request, response);
        long duration = System.currentTimeMillis() - start;

        String timestamp = Ansi.ansi()
                .fgBright(Ansi.Color.MAGENTA).a(Instant.now().toString()).reset()
                .toString();

        int status = res.getStatus();
        String statusColor;
        if (status >= 200 && status < 300) {
            statusColor = Ansi.ansi().fg(Ansi.Color.GREEN).a(status).reset().toString();
        } else if (status >= 400 && status < 500) {
            statusColor = Ansi.ansi().fg(Ansi.Color.YELLOW).a(status).reset().toString();
        } else if (status >= 500) {
            statusColor = Ansi.ansi().fg(Ansi.Color.RED).a(status).reset().toString();
        } else {
            statusColor = Ansi.ansi().fg(Ansi.Color.DEFAULT).a(status).reset().toString();
        }

        String methodColor = Ansi.ansi().fg(Ansi.Color.CYAN).a(req.getMethod()).reset().toString();

        String pathColor = Ansi.ansi().fg(Ansi.Color.BLUE).a(req.getRequestURI()).reset().toString();

        String latencyColor;
        if (duration < 500) {
            latencyColor = Ansi.ansi().fgBright(Ansi.Color.WHITE).a(duration + "ms").reset().toString();
        } else if (duration < 1000) {
            latencyColor = Ansi.ansi().fg(Ansi.Color.YELLOW).a(duration + "ms").reset().toString();
        } else {
            latencyColor = Ansi.ansi().fg(Ansi.Color.RED).a(duration + "ms").reset().toString();
        }

        String log = String.format("[%s] %s %s %s - %s",
                timestamp, methodColor, pathColor, statusColor, latencyColor);

        System.out.println(log);
    }
}
