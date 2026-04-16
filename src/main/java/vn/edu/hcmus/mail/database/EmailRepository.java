package vn.edu.hcmus.mail.database;

import vn.edu.hcmus.mail.model.Email;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class EmailRepository {
    private final DatabaseManager dbManager;

    public EmailRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public EmailRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public boolean saveSentEmail(Email email) {
        String sql = """
            INSERT INTO sent_emails (msg_id, from_email, to_email, subject, body, attachments, sent_at, status, sync_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, email.getMsgId());
            pstmt.setString(2, email.getFromEmail());
            pstmt.setString(3, email.getToEmail());
            pstmt.setString(4, email.getSubject());
            pstmt.setString(5, email.getBody());
            pstmt.setString(6, attachmentsToJson(email.getAttachments()));
            pstmt.setString(7, email.getTimestamp().toString());
            pstmt.setString(8, "sent");
            pstmt.setString(9, "synced");
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("Email already exists: " + email.getMsgId());
                return false;
            }
            System.err.println("Error saving sent email: " + e.getMessage());
            return false;
        }
    }

    public boolean saveReceivedEmail(Email email) {
        String sql = """
            INSERT INTO received_emails (msg_id, from_email, to_email, subject, body, received_at, is_read, status, sync_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, email.getMsgId());
            pstmt.setString(2, email.getFromEmail());
            pstmt.setString(3, email.getToEmail());
            pstmt.setString(4, email.getSubject());
            pstmt.setString(5, email.getBody());
            pstmt.setString(6, email.getTimestamp().toString());
            pstmt.setInt(7, email.isRead() ? 1 : 0);
            pstmt.setString(8, "received");
            pstmt.setString(9, "synced");
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                return false;
            }
            System.err.println("Error saving received email: " + e.getMessage());
            return false;
        }
    }

    public List<Email> getAllSentEmails(int limit, int offset) {
        List<Email> emails = new ArrayList<>();
        String sql = "SELECT * FROM sent_emails ORDER BY sent_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                emails.add(mapSentEmail(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error reading sent emails: " + e.getMessage());
        }
        return emails;
    }

    public List<Email> getAllReceivedEmails(int limit, int offset) {
        List<Email> emails = new ArrayList<>();
        String sql = "SELECT * FROM received_emails ORDER BY received_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                emails.add(mapReceivedEmail(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error reading received emails: " + e.getMessage());
        }
        return emails;
    }

    public List<Email> getAllSentEmails() {
        return getAllSentEmails(100, 0);
    }

    public List<Email> getAllReceivedEmails() {
        return getAllReceivedEmails(100, 0);
    }

    public Optional<Email> getSentEmailByMsgId(String msgId) {
        String sql = "SELECT * FROM sent_emails WHERE msg_id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, msgId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapSentEmail(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding sent email: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Email> getReceivedEmailByMsgId(String msgId) {
        String sql = "SELECT * FROM received_emails WHERE msg_id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, msgId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapReceivedEmail(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding received email: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean deleteSentEmail(Long id) {
        String sql = "DELETE FROM sent_emails WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting sent email: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteReceivedEmail(Long id) {
        String sql = "DELETE FROM received_emails WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting received email: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteEmailByMsgId(String msgId, Email.EmailType type) {
        String table = type == Email.EmailType.SENT ? "sent_emails" : "received_emails";
        String sql = "DELETE FROM " + table + " WHERE msg_id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, msgId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting email: " + e.getMessage());
            return false;
        }
    }

    public List<Email> searchSentEmails(String keyword) {
        List<Email> emails = new ArrayList<>();
        String sql = """
            SELECT * FROM sent_emails 
            WHERE subject LIKE ? OR body LIKE ? OR to_email LIKE ?
            ORDER BY sent_at DESC
            """;
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                emails.add(mapSentEmail(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching sent emails: " + e.getMessage());
        }
        return emails;
    }

    public List<Email> searchReceivedEmails(String keyword) {
        List<Email> emails = new ArrayList<>();
        String sql = """
            SELECT * FROM received_emails 
            WHERE subject LIKE ? OR body LIKE ? OR from_email LIKE ?
            ORDER BY received_at DESC
            """;
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                emails.add(mapReceivedEmail(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching received emails: " + e.getMessage());
        }
        return emails;
    }

    public List<Email> searchAllEmails(String keyword) {
        List<Email> results = new ArrayList<>();
        results.addAll(searchSentEmails(keyword));
        results.addAll(searchReceivedEmails(keyword));
        return results;
    }

    public boolean markAsRead(Long id) {
        String sql = "UPDATE received_emails SET is_read = 1 WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error marking as read: " + e.getMessage());
            return false;
        }
    }

    public int getUnreadCount() {
        String sql = "SELECT COUNT(*) FROM received_emails WHERE is_read = 0";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting unread: " + e.getMessage());
        }
        return 0;
    }

    public void getSentEmailsStream(Consumer<Email> consumer) {
        String sql = "SELECT * FROM sent_emails ORDER BY sent_at DESC";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                consumer.accept(mapSentEmail(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error reading stream sent emails: " + e.getMessage());
        }
    }

    public void getReceivedEmailsStream(Consumer<Email> consumer) {
        String sql = "SELECT * FROM received_emails ORDER BY received_at DESC";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                consumer.accept(mapReceivedEmail(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error reading stream received emails: " + e.getMessage());
        }
    }

    private Email mapSentEmail(ResultSet rs) throws SQLException {
        Email email = new Email(Email.EmailType.SENT);
        email.setId(rs.getLong("id"));
        email.setMsgId(rs.getString("msg_id"));
        email.setFromEmail(rs.getString("from_email"));
        email.setToEmail(rs.getString("to_email"));
        email.setSubject(rs.getString("subject"));
        email.setBody(rs.getString("body"));
        email.setAttachments(jsonToAttachments(rs.getString("attachments")));
        email.setTimestamp(LocalDateTime.parse(rs.getString("sent_at")));
        return email;
    }

    private Email mapReceivedEmail(ResultSet rs) throws SQLException {
        Email email = new Email(Email.EmailType.RECEIVED);
        email.setId(rs.getLong("id"));
        email.setMsgId(rs.getString("msg_id"));
        email.setFromEmail(rs.getString("from_email"));
        email.setToEmail(rs.getString("to_email"));
        email.setSubject(rs.getString("subject"));
        email.setBody(rs.getString("body"));
        email.setTimestamp(LocalDateTime.parse(rs.getString("received_at")));
        email.setRead(rs.getInt("is_read") == 1);
        return email;
    }

    private String attachmentsToJson(List<String> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < attachments.size(); i++) {
            sb.append("\"").append(attachments.get(i).replace("\"", "\\\"")).append("\"");
            if (i < attachments.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private List<String> jsonToAttachments(String json) {
        List<String> attachments = new ArrayList<>();
        if (json == null || json.equals("[]")) return attachments;
        String content = json.substring(1, json.length() - 1);
        if (content.isEmpty()) return attachments;
        for (String item : content.split(",")) {
            attachments.add(item.trim().replace("\"", ""));
        }
        return attachments;
    }

    public void printAllSentEmails() {
        List<Email> emails = getAllSentEmails();
        System.out.println("=== SENT EMAILS ===");
        for (Email email : emails) {
            System.out.println(email);
        }
    }

    public void printAllReceivedEmails() {
        List<Email> emails = getAllReceivedEmails();
        System.out.println("=== RECEIVED EMAILS ===");
        for (Email email : emails) {
            System.out.println(email);
        }
    }
}
