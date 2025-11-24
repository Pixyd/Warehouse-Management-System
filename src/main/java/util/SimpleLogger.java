package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimpleLogger {
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static void info(String msg) {
        System.out.println(TF.format(LocalDateTime.now()) + " INFO " + msg);
    }
    public static void error(String msg) {
        System.err.println(TF.format(LocalDateTime.now()) + " ERROR " + msg);
    }
}
