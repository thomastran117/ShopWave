package backend.utilities.impl;

import backend.utilities.intf.Logger;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static org.fusesource.jansi.Ansi.ansi;

@Component
public class LoggerImpl implements Logger {

    static {
        AnsiConsole.systemInstall();
        Runtime.getRuntime().addShutdownHook(new Thread(AnsiConsole::systemUninstall));
    }

    private static String timestamp() {
        return ansi()
                .fgBright(Ansi.Color.BLUE)
                .a(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                .reset().toString();
    }

    private static void log(String level, Ansi.Color color, String message) {
        String ts = timestamp();
        String lvl = ansi().fg(color).a("[" + level + "]").reset().toString();
        System.out.println(String.format("[%s] %s %s", ts, lvl, message));
    }

    @Override
    public void info(String message) {
        log("INFO", Ansi.Color.CYAN, message);
    }

    @Override
    public void debug(String message) {
        log("DEBUG", Ansi.Color.MAGENTA, message);
    }

    @Override
    public void warn(String message) {
        log("WARN", Ansi.Color.YELLOW, message);
    }

    @Override
    public void error(String message) {
        log("ERROR", Ansi.Color.RED, message);
    }

    @Override
    public void critical(String message) {
        log("CRITICAL", Ansi.Color.RED, message.toUpperCase());
    }
}
