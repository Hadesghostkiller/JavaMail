package vn.edu.hcmus.mail.supabase;

import vn.edu.hcmus.mail.model.Email;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SupabaseEmailParser {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @SuppressWarnings("unchecked")
    public static List<Email> parseSentEmails(String json) {
        List<Email> emails = new ArrayList<>();
        if (json == null || json.isEmpty() || json.equals("[]")) return emails;

        try {
            com.google.gson.JsonArray arr = new com.google.gson.JsonParser().parse(json).getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                try {
                    com.google.gson.JsonObject obj = arr.get(i).getAsJsonObject();
                    Email email = new Email(Email.EmailType.SENT);
                    email.setMsgId(getString(obj, "msg_id"));
                    email.setFromEmail(getString(obj, "from_email"));
                    email.setToEmail(getString(obj, "to_email"));
                    email.setSubject(getString(obj, "subject"));
                    email.setBody(getString(obj, "body"));
                    email.setAttachments(parseAttachments(obj.get("attachments")));
                    email.setTimestamp(parseTimestamp(getString(obj, "timestamp")));
                    emails.add(email);
                } catch (Exception e) {
                    System.err.println("[SUPABASE] Lỗi parse sent email: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[SUPABASE] Lỗi parse JSON: " + e.getMessage());
        }
        return emails;
    }

    @SuppressWarnings("unchecked")
    public static List<Email> parseReceivedEmails(String json) {
        List<Email> emails = new ArrayList<>();
        if (json == null || json.isEmpty() || json.equals("[]")) return emails;

        try {
            com.google.gson.JsonArray arr = new com.google.gson.JsonParser().parse(json).getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                try {
                    com.google.gson.JsonObject obj = arr.get(i).getAsJsonObject();
                    Email email = new Email(Email.EmailType.RECEIVED);
                    email.setMsgId(getString(obj, "msg_id"));
                    email.setFromEmail(getString(obj, "from_email"));
                    email.setToEmail(getString(obj, "to_email"));
                    email.setSubject(getString(obj, "subject"));
                    email.setBody(getString(obj, "body"));
                    email.setTimestamp(parseTimestamp(getString(obj, "timestamp")));
                    email.setRead(getBoolean(obj, "is_read"));
                    emails.add(email);
                } catch (Exception e) {
                    System.err.println("[SUPABASE] Lỗi parse received email: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[SUPABASE] Lỗi parse JSON: " + e.getMessage());
        }
        return emails;
    }

    private static String getString(com.google.gson.JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return null;
    }

    private static boolean getBoolean(com.google.gson.JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsBoolean();
        }
        return false;
    }

    private static LocalDateTime parseTimestamp(String timestamp) {
        if (timestamp == null) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(timestamp, DATE_FORMAT);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private static List<String> parseAttachments(com.google.gson.JsonElement element) {
        List<String> attachments = new ArrayList<>();
        if (element != null && !element.isJsonNull() && element.isJsonArray()) {
            for (com.google.gson.JsonElement item : element.getAsJsonArray()) {
                if (!item.isJsonNull()) {
                    attachments.add(item.getAsString());
                }
            }
        }
        return attachments;
    }
}
