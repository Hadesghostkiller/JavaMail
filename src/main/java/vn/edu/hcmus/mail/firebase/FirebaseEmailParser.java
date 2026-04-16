package vn.edu.hcmus.mail.firebase;

import com.google.firebase.database.DataSnapshot;
import vn.edu.hcmus.mail.model.Email;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FirebaseEmailParser {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Email parseSentEmail(DataSnapshot snapshot) {
        Email email = new Email(Email.EmailType.SENT);
        email.setMsgId(getString(snapshot, "msgId"));
        email.setFromEmail(getString(snapshot, "fromEmail"));
        email.setToEmail(getString(snapshot, "toEmail"));
        email.setSubject(getString(snapshot, "subject"));
        email.setBody(getString(snapshot, "body"));
        email.setAttachments(parseAttachments(snapshot.child("attachments")));
        String timestamp = getString(snapshot, "timestamp");
        if (timestamp != null) {
            try {
                email.setTimestamp(LocalDateTime.parse(timestamp, DATE_FORMAT));
            } catch (Exception e) {
                email.setTimestamp(LocalDateTime.now());
            }
        }
        return email;
    }

    public static Email parseReceivedEmail(DataSnapshot snapshot) {
        Email email = new Email(Email.EmailType.RECEIVED);
        email.setMsgId(getString(snapshot, "msgId"));
        email.setFromEmail(getString(snapshot, "fromEmail"));
        email.setToEmail(getString(snapshot, "toEmail"));
        email.setSubject(getString(snapshot, "subject"));
        email.setBody(getString(snapshot, "body"));
        String timestamp = getString(snapshot, "timestamp");
        if (timestamp != null) {
            try {
                email.setTimestamp(LocalDateTime.parse(timestamp, DATE_FORMAT));
            } catch (Exception e) {
                email.setTimestamp(LocalDateTime.now());
            }
        }
        Boolean isRead = snapshot.child("isRead").getValue(Boolean.class);
        email.setRead(isRead != null ? isRead : false);
        return email;
    }

    public static List<Email> parseSentEmails(DataSnapshot dataSnapshot) {
        List<Email> emails = new ArrayList<>();
        if (dataSnapshot.exists()) {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                try {
                    emails.add(parseSentEmail(snapshot));
                } catch (Exception e) {
                    System.err.println("[FIREBASE] Lỗi parse sent email: " + e.getMessage());
                }
            }
        }
        return emails;
    }

    public static List<Email> parseReceivedEmails(DataSnapshot dataSnapshot) {
        List<Email> emails = new ArrayList<>();
        if (dataSnapshot.exists()) {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                try {
                    emails.add(parseReceivedEmail(snapshot));
                } catch (Exception e) {
                    System.err.println("[FIREBASE] Lỗi parse received email: " + e.getMessage());
                }
            }
        }
        return emails;
    }

    private static String getString(DataSnapshot snapshot, String field) {
        Object value = snapshot.child(field).getValue();
        return value != null ? value.toString() : null;
    }

    private static List<String> parseAttachments(DataSnapshot snapshot) {
        List<String> attachments = new ArrayList<>();
        if (snapshot.exists()) {
            for (DataSnapshot child : snapshot.getChildren()) {
                Object value = child.getValue();
                if (value != null) {
                    attachments.add(value.toString());
                }
            }
        }
        return attachments;
    }
}
