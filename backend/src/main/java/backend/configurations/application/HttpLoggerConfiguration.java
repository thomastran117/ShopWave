package backend.configurations.application;

import backend.utilities.HttpLogger;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HttpLoggerConfiguration implements Filter {

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
        
        HttpLogger.log(req, res.getStatus(), duration);
    }
}
