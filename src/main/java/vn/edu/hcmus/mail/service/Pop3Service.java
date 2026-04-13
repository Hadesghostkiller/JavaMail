package vn.edu.hcmus.mail.service;

import vn.edu.hcmus.mail.model.EmailContent;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Pop3Service {

    /**
     * Hàm nhận danh sách thư từ Server bất kỳ
     */
    public List<EmailContent> fetchEmails(Properties props, String username, String password) throws MessagingException {
        List<EmailContent> emailList = new ArrayList<>();

        // 1. Tạo Session
        Session session = Session.getInstance(props);

        // 2. Kết nối tới Store (POP3)
        Store store = session.getStore(props.getProperty("mail.store.protocol"));
        store.connect(props.getProperty("mail.pop3.host"), username, password);

        // 3. Mở thư mục INBOX
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        // 4. Lấy danh sách tin nhắn
        Message[] messages = inbox.getMessages();

        for (Message msg : messages) {
            try {
                String from = InternetAddress.toString(msg.getFrom());
                String subject = msg.getSubject();


                // Lưu vào Model của ông (Giả sử EmailContent có constructor hoặc setter phù hợp)
                // Ông có thể tùy biến thêm phần đọc Body ở đây
                EmailContent content = new EmailContent(from, subject, "[Nội dung rút gọn]");
                emailList.add(content);

            } catch (Exception e) {
                System.err.println("Lỗi đọc thư: " + e.getMessage());
            }
        }

        // 5. Đóng kết nối
        inbox.close(false);
        store.close();

        return emailList;
    }
}