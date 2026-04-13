package vn.edu.hcmus.mail.service;

import vn.edu.hcmus.mail.config.MailConfig;
import javax.mail.*;
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

                // LẤY NỘI DUNG THƯ (BODY)
                sb.append("- Nội dung: \n");
                try {
                    sb.append(getTextFromMessage(msg)); // Gọi hàm phụ trợ bên dưới
                } catch (Exception e) {
                    sb.append("[Không thể hiển thị nội dung]");
                }

                sb.append("\n---------------------------\n\n");
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            sb.append("Lỗi POP3: ").append(e.getMessage());
        }
        return sb.toString(); // Trả kết quả về cho GUI
    }
}