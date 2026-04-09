package vn.edu.hcmus.mail.service;

import vn.edu.hcmus.mail.config.MailConfig;
import javax.mail.*;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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
                // Xử lý file đính kèm
                saveAttachments(msg);
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

                            // Ghi dữ liệu từ stream vào file (Dùng cho cả Audio/Video dung lượng lớn)
                            try (InputStream is = bodyPart.getInputStream();
                                 FileOutputStream fos = new FileOutputStream(dest)) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    fos.write(buffer, 0, bytesRead);
                                }
                            }
                            System.out.println("  => Saved to: " + dest.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("  [Error saving attachment]: " + e.getMessage());
        }
    }
}