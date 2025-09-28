package backend.utilities;

import jakarta.servlet.http.HttpServletRequest;
import org.fusesource.jansi.Ansi;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static org.fusesource.jansi.Ansi.ansi;
public class HttpLogger {

    public static void log(HttpServletRequest req, int status, long duration) {
        String path = req.getRequestURI();

        if (path.startsWith("/error") || path.startsWith("/api/error") || path.startsWith("/api/error-disabled")) {
                return;
        }

        String timestamp = ansi()
                .fgBrightBlue().a(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                .reset().toString();

        String info = ansi()
                .fgBrightMagenta().a("[INFO]:")
                .reset().toString();

        String methodColor = ansi()
                .fgBrightCyan().a(req.getMethod())
                .reset().toString();

        String pathColor = ansi()
                .fgBright(Ansi.Color.WHITE).a(req.getRequestURI())
                .reset().toString();

        String statusColor =
                status >= 200 && status < 300 ? ansi().fg(Ansi.Color.GREEN).a(status).reset().toString() :
                status >= 300 && status < 400 ? ansi().fg(Ansi.Color.CYAN).a(status).reset().toString() :
                status >= 400 && status < 500 ? ansi().fg(Ansi.Color.YELLOW).a(status).reset().toString() :
                ansi().fg(Ansi.Color.RED).a(status).reset().toString();

        String latencyColor =
                duration < 500 ? ansi().fgBrightGreen().a(duration + "ms").reset().toString() :
                duration < 1000 ? ansi().fgYellow().a(duration + "ms").reset().toString() :
                ansi().fgRed().a(duration + "ms").reset().toString();

        String log = String.format("[%s] %s %s %s - %s - %s",
                timestamp, info, methodColor, pathColor, statusColor, latencyColor);

        System.out.println(log);
    }
}
