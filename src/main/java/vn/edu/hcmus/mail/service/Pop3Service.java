package vn.edu.hcmus.mail.service;

import vn.edu.hcmus.mail.config.MailConfig;
import javax.mail.*;
import java.util.Properties;

public class Pop3Service {
    public void receive() {
        // 1. Cấu hình Properties cho POP3
        Properties props = new Properties();
        props.put("mail.pop3.host", MailConfig.POP3_HOST);
        props.put("mail.pop3.port", MailConfig.POP3_PORT);
        props.put("mail.pop3.starttls.enable", "true");

        try {
            // 2. Tạo Session và kết nối tới Store (Kho lưu trữ thư)
            Session session = Session.getInstance(props);
            Store store = session.getStore(MailConfig.PROTOCOL);
            store.connect(MailConfig.POP3_HOST, MailConfig.EMAIL_USER, MailConfig.APP_PASSWORD);

            // 3. Mở thư mục Inbox (Hộp thư đến)
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY); // Chỉ đọc thư

            // 4. Lấy danh sách tin nhắn
            Message[] messages = inbox.getMessages();
            System.out.println("--- BẮT ĐẦU ĐỌC THƯ (Tổng số: " + messages.length + " thư) ---");

            // Đọc thử 5 thư gần nhất (nếu có)
            int start = Math.max(0, messages.length - 5);
            for (int i = messages.length - 1; i >= start; i--) {
                Message msg = messages[i];
                System.out.println("Thứ " + (i + 1) + ":");
                System.out.println("- Từ: " + msg.getFrom()[0]);
                System.out.println("- Tiêu đề: " + msg.getSubject());
                System.out.println("- Ngày: " + msg.getSentDate());
                System.out.println("---------------------------");
            }

            // 5. Đóng kết nối
            inbox.close(false);
            store.close();

        } catch (Exception e) {
            System.err.println("Lỗi khi nhận mail: " + e.getMessage());
            e.printStackTrace();
        }
    }
}