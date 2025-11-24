package dao;

import util.SimpleLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class DbManager {
    private static final String DB_URL = "jdbc:sqlite:warehouse.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            SimpleLogger.error("SQLite JDBC not found");
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        conn.setAutoCommit(false);
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void initDatabase() throws Exception {
        // Try to find resources/sql/schema.sql in classpath; fallback to file path in project resources
        InputStream in = DbManager.class.getResourceAsStream("/sql/schema.sql");
        String sql = null;
        if (in != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line).append("\n");
                sql = sb.toString();
            }
        } else {
            // fallback to project file path
            String fallback = "src/main/resources/sql/schema.sql";
            if (Files.exists(Paths.get(fallback))) {
                sql = new String(Files.readAllBytes(Paths.get(fallback)));
            }
        }
        if (sql == null) {
            SimpleLogger.info("No schema.sql found; skipping DB initialization");
            return;
        }
        try (Connection c = DriverManager.getConnection(DB_URL)) {
            c.setAutoCommit(false);
            for (String stmt : sql.split(";")) {
                String s = stmt.trim();
                if (!s.isEmpty()) try (Statement st = c.createStatement()) {
                    st.execute(s);
                }
            }
            c.commit();
        }
    }
}
