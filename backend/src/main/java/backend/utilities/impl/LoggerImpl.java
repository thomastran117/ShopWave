package backend.utilities.impl;

import backend.utilities.intf.Logger;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

import static org.fusesource.jansi.Ansi.ansi;

@Component
public class LoggerImpl implements Logger {

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

    private final ReentrantLock lock = new ReentrantLock();

    private String timestamp() {
        Instant now = Instant.now();

        String date = ansi()
                .fgBrightBlack()
                .a(DATE_FORMAT.format(now))
                .reset()
                .toString();

        String time = ansi()
                .fgBrightBlack()
                .a(TIME_FORMAT.format(now))
                .reset()
                .toString();

        String separator = ansi()
                .fgBrightBlack()
                .a(" | ")
                .reset()
                .toString();

        return date + separator + time;
    }

    private String level(String label, Ansi.Color color, boolean bright) {
        String padded = String.format(label);
        Ansi a = bright ? ansi().fgBright(color) : ansi().fg(color);
        return a.bold().a(padded).reset().toString();
    }

    private void log(String label, Ansi.Color color, boolean bright, String message) {
        String timestamp = timestamp();
        String level     = level(label, color, bright);
        String line      = String.format("[%s] [%s] : %s", timestamp, level, message);

        lock.lock();
        try {
            System.out.println(line);
        } finally {
            lock.unlock();
        }
    }

    private void logf(String label, Ansi.Color color, boolean bright,
                      String fmt, Object... args) {
        log(label, color, bright, args.length == 0 ? fmt : String.format(fmt, args));
    }

    @Override
    public void info(String message) {
        log("INFO", Ansi.Color.BLUE, true, message);
    }

    @Override
    public void debug(String message) {
        log("DEBUG", Ansi.Color.CYAN, false, message);
    }

    @Override
    public void warn(String message) {
        log("WARN", Ansi.Color.YELLOW, true, message);
    }

    @Override
    public void error(String message) {
        log("ERROR", Ansi.Color.RED, true, message);
    }

    @Override
    public void critical(String message) {
        String highlighted = ansi()
                .bgBrightRed()
                .fgBright(Ansi.Color.WHITE)
                .bold()
                .a(message.toUpperCase())
                .reset()
                .toString();

        log("CRITICAL", Ansi.Color.RED, true, highlighted);
    }

    public void info(String fmt, Object... args)     { logf("INFO",     Ansi.Color.GREEN,  true,  fmt, args); }
    public void debug(String fmt, Object... args)    { logf("DEBUG",    Ansi.Color.CYAN,   false, fmt, args); }
    public void warn(String fmt, Object... args)     { logf("WARN",     Ansi.Color.YELLOW, true,  fmt, args); }
    public void error(String fmt, Object... args)    { logf("ERROR",    Ansi.Color.RED,    true,  fmt, args); }
    public void critical(String fmt, Object... args) { critical(args.length == 0 ? fmt : String.format(fmt, args)); }
}