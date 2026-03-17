package backend.utilities;

import jakarta.servlet.http.HttpServletRequest;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

import static org.fusesource.jansi.Ansi.ansi;

public class HttpLogger {

    static {
        AnsiConsole.systemInstall();
        Runtime.getRuntime().addShutdownHook(new Thread(AnsiConsole::systemUninstall));
    }

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    .withZone(ZoneId.systemDefault());

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
                    .withZone(ZoneId.systemDefault());

    private static final ReentrantLock lock = new ReentrantLock();

    private static final String[] IGNORED_PATHS = {
            "/error", "/api/error", "/api/error-disabled"
    };

    private static String timestamp() {
        Instant now = Instant.now();

        String date = ansi()
                .fgBrightBlack().a(DATE_FORMAT.format(now))
                .reset().toString();

        String separator = ansi()
                .fgBrightBlack().a(" | ")
                .reset().toString();

        String time = ansi()
                .fgBrightBlack().a(TIME_FORMAT.format(now))
                .reset().toString();

        return date + separator + time;
    }

    private static String method(String httpMethod) {
        String padded = String.format(httpMethod);
        return ansi().fgBright(Ansi.Color.CYAN).bold().a(padded).reset().toString();
    }

    private static String status(int code) {
        Ansi a = ansi();

        if      (code < 300) a = a.fgBright(Ansi.Color.GREEN);
        else if (code < 400) a = a.fgBright(Ansi.Color.CYAN);
        else if (code < 500) a = a.fgBright(Ansi.Color.YELLOW);
        else                 a = a.fgBright(Ansi.Color.RED);

        return a.bold().a(code).reset().toString();
    }

    private static String latency(long ms) {
        Ansi a = ansi();

        if      (ms < 500)  a = a.fgBright(Ansi.Color.GREEN);
        else if (ms < 1000) a = a.fgBright(Ansi.Color.YELLOW);
        else                a = a.fgBright(Ansi.Color.RED);

        return a.a(ms + "ms").reset().toString();
    }

    private static String path(HttpServletRequest req) {
        String uri = req.getRequestURI();

        String queryString = req.getQueryString();
        if (queryString != null && !queryString.isBlank()) {
            uri += "?" + ansi().fgBrightBlack().a(queryString).reset();
        }

        return ansi().fgBright(Ansi.Color.WHITE).a(uri).reset().toString();
    }

    private static boolean isIgnored(String path) {
        for (String ignored : IGNORED_PATHS) {
            if (path.startsWith(ignored)) return true;
        }
        return false;
    }

    public static void log(HttpServletRequest req, int statusCode, long duration) {
        String uri = req.getRequestURI();
        if (isIgnored(uri)) return;

        String line = String.format("[%s] [%s] : %s %s - %s - %s",
                timestamp(),
                ansi().fgBright(Ansi.Color.BLUE).bold().a("HTTP").reset(),
                method(req.getMethod()),
                path(req),
                status(statusCode),
                latency(duration)
        );

        lock.lock();
        try {
            System.out.println(line);
        } finally {
            lock.unlock();
        }
    }
}