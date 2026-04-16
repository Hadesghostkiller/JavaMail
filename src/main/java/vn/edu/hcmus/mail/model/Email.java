package vn.edu.hcmus.mail.model;

import java.time.LocalDateTime;
import java.util.List;

public class Email {
    private Long id;
    private String msgId;
    private String fromEmail;
    private String toEmail;
    private String subject;
    private String body;
    private List<String> attachments;
    private LocalDateTime timestamp;
    private boolean isRead;
    private EmailType type;

    public enum EmailType {
        SENT, RECEIVED
    }

    public Email() {}

    public Email(EmailType type) {
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMsgId() { return msgId; }
    public void setMsgId(String msgId) { this.msgId = msgId; }

    public String getFromEmail() { return fromEmail; }
    public void setFromEmail(String fromEmail) { this.fromEmail = fromEmail; }

    public String getToEmail() { return toEmail; }
    public void setToEmail(String toEmail) { this.toEmail = toEmail; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public List<String> getAttachments() { return attachments; }
    public void setAttachments(List<String> attachments) { this.attachments = attachments; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public EmailType getType() { return type; }
    public void setType(EmailType type) { this.type = type; }

    @Override
    public String toString() {
        return "[" + type + "] " + subject + " - " + (type == EmailType.SENT ? "→ " + toEmail : "← " + fromEmail);
    }
}
