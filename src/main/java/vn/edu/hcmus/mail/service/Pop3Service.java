package vn.edu.hcmus.mail.service;

import vn.edu.hcmus.mail.config.MailConfig;
import vn.edu.hcmus.mail.database.EmailCache;
import vn.edu.hcmus.mail.supabase.SupabaseSyncService;
import vn.edu.hcmus.mail.model.Email;
import javax.mail.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Properties;
import javax.mail.internet.*;

public class Pop3Service {
    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            // Lấy phần đầu tiên của mail (thường là nội dung chữ)
            BodyPart bodyPart = mimeMultipart.getBodyPart(0);
            return bodyPart.getContent().toString();
        }
        return "[Định dạng không hỗ trợ]";
    }

    public String receive() { // Đổi void thành String
        StringBuilder sb = new StringBuilder(); // Dùng để gom dữ liệu
        Properties props = new Properties();
        props.put("mail.pop3.host", MailConfig.POP3_HOST);
        props.put("mail.pop3.port", MailConfig.POP3_PORT);
        props.put("mail.pop3.starttls.enable", "true");

        try {
            Session session = Session.getInstance(props);
            Store store = session.getStore(MailConfig.PROTOCOL);
            store.connect(MailConfig.POP3_HOST, MailConfig.EMAIL_USER, MailConfig.APP_PASSWORD);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            sb.append("--- Tổng số thư: ").append(messages.length).append(" ---\n\n");

            int start = Math.max(0, messages.length - 5);
            for (int i = messages.length - 1; i >= start; i--) {
                Message msg = messages[i];
                sb.append("Thứ ").append(i + 1).append(":\n");
                sb.append("- Từ: ").append(msg.getFrom()[0]).append("\n");
                sb.append("- Tiêu đề: ").append(msg.getSubject()).append("\n");

                // GỌI HÀM LƯU FILE
                saveAttachments(msg);

                // LẤY NỘI DUNG THƯ (BODY)
                sb.append("- Nội dung: \n");
                String bodyContent = "";
                try {
                    bodyContent = getTextFromMessage(msg);
                    sb.append(bodyContent);
                } catch (Exception e) {
                    sb.append("[Không thể hiển thị nội dung]");
                }

                sb.append("\n---------------------------\n\n");

                // Lưu email vào database
                try {
                    String fromEmail = msg.getFrom()[0].toString();
                    InternetAddress addr = new InternetAddress(fromEmail);
                    String msgId = msg.getMessageID();
                    if (msgId == null) msgId = String.valueOf(System.currentTimeMillis());

                    Email emailRecord = new Email(Email.EmailType.RECEIVED);
                    emailRecord.setMsgId(msgId);
                    emailRecord.setFromEmail(addr.getAddress());
                    emailRecord.setToEmail(MailConfig.EMAIL_USER);
                    emailRecord.setSubject(msg.getSubject() != null ? msg.getSubject() : "(Không có tiêu đề)");
                    emailRecord.setBody(bodyContent);
                    emailRecord.setTimestamp(LocalDateTime.now());
                    emailRecord.setRead(false);

                    EmailCache.getInstance().cacheReceivedEmail(emailRecord);
                    System.out.println("[DATABASE] Đã lưu email nhận được vào database: " + emailRecord.getSubject());

                    SupabaseSyncService.getInstance().syncReceivedEmail(emailRecord);
                } catch (Exception dbEx) {
                    System.err.println("[DATABASE] Lỗi khi lưu email: " + dbEx.getMessage());
                }
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            sb.append("Lỗi POP3: ").append(e.getMessage());
        }
        return sb.toString(); // Trả kết quả về cho GUI
    }

    // Xu ly file sau khi dươc gui + tai ve
    private void saveAttachments(Message message) {
        try {
            // Kiểm tra xem mail có chứa nhiều phần (Multipart) không
            if (message.isMimeType("multipart/*")) {
                Multipart multipart = (Multipart) message.getContent();

                // Duyệt qua từng phần của email
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);

                    // Kiểm tra xem phần này có phải là file đính kèm không
                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                        String fileName = bodyPart.getFileName();
                        if (fileName != null) {
                            // Giải mã tên file để tránh lỗi font tiếng Việt
                            fileName = MimeUtility.decodeText(fileName);
                            System.out.println("  [Phát hiện file]: " + fileName);

                            // Tạo thư mục "downloads" nếu chưa có
                            File folder = new File("downloads");
                            if (!folder.exists()) folder.mkdirs();

                            File dest = new File(folder, fileName);

                            // Ghi dữ liệu từ stream vào file
                            try (InputStream is = bodyPart.getInputStream();
                                 FileOutputStream fos = new FileOutputStream(dest)) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    fos.write(buffer, 0, bytesRead);
                                }
                            }
                            System.out.println("  => Đã lưu file thành công tại thư mục downloads!");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("  [Lỗi lưu file]: " + e.getMessage());
        }
    }
}