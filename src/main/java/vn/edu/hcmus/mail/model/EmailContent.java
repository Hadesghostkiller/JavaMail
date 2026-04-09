package vn.edu.hcmus.mail.model;

import java.util.ArrayList;
import java.util.List;

public class EmailContent {
    private String to;
    private String subject;
    private String body;

    // Constructor để khởi tạo đối tượng
    public EmailContent(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    // Các phương thức Getter (Đây là phần bạn đang thiếu)
    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    // Thành + Gửi đính kèm file / video
    private java.util.List<String> attachmentPaths;

    public void setAttachmentPaths(java.util.List<String> attachmentPaths) {
        this.attachmentPaths = attachmentPaths;
    }

    public java.util.List<String> getAttachmentPaths() {
        return attachmentPaths;
    }
}

