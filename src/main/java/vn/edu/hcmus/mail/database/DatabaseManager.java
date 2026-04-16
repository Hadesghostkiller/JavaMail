package vn.edu.hcmus.mail.database;

import java.sql.*;
import java.time.LocalDateTime;

public class DatabaseManager {
    private static final String DB_PATH = "database/mail_history.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            createTables();
        } catch (Exception e) {
            System.err.println("Error initializing Database: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String createSentEmails = """
            CREATE TABLE IF NOT EXISTS sent_emails (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                msg_id TEXT UNIQUE,
                from_email TEXT NOT NULL,
                to_email TEXT NOT NULL,
                subject TEXT,
                body TEXT,
                attachments TEXT,
                sent_at TEXT NOT NULL,
                status TEXT DEFAULT 'sent',
                sync_status TEXT DEFAULT 'synced'
            )
            """;

        String createReceivedEmails = """
            CREATE TABLE IF NOT EXISTS received_emails (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                msg_id TEXT UNIQUE,
                from_email TEXT NOT NULL,
                to_email TEXT NOT NULL,
                subject TEXT,
                body TEXT,
                received_at TEXT NOT NULL,
                is_read INTEGER DEFAULT 0,
                status TEXT DEFAULT 'received',
                sync_status TEXT DEFAULT 'synced'
            )
            """;

        String createIndexes = """
            CREATE INDEX IF NOT EXISTS idx_sent_to ON sent_emails(to_email);
            CREATE INDEX IF NOT EXISTS idx_sent_date ON sent_emails(sent_at);
            CREATE INDEX IF NOT EXISTS idx_received_from ON received_emails(from_email);
            CREATE INDEX IF NOT EXISTS idx_received_date ON received_emails(received_at);
            CREATE INDEX IF NOT EXISTS idx_msg_id ON sent_emails(msg_id);
            CREATE INDEX IF NOT EXISTS idx_received_msg_id ON received_emails(msg_id);
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSentEmails);
            stmt.execute(createReceivedEmails);
            stmt.execute(createIndexes);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
