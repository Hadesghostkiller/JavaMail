package vn.edu.hcmus.mail.service;
import vn.edu.hcmus.mail.model.EmailContent;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmtpService {

    // Tạo pool luồng để gửi mail hàng loạt mà không gây treo ứng dụng
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * 1. HÀM GỬI ĐƠN (Chuẩn hóa)
     * Thay vì fix cứng MailConfig, ta có thể truyền Properties vào để dùng cho mọi Server (Gmail, Outlook,...)
     */
    public void send(EmailContent email, Properties props, String username, String password) throws MessagingException {
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // Debug log để ông dễ theo dõi TCP/IP như trong log ông gửi
        session.setDebug(true);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.getTo()));
        message.setSubject(email.getSubject());
        message.setText(email.getBody());

        Transport.send(message);
    }

    /**
     * Sử dụng Thread để không bị khóa tài khoản và tăng hiệu suất
     */
    public void sendBulk(List<String> recipients, String subject, String body, Properties props, String username, String password) {
        for (String to : recipients) {
            executor.execute(() -> {
                try {
                    EmailContent email = new EmailContent(to, subject, body);
                    send(email, props, username, password);

                    System.out.println("[SUCCESS] Đã gửi tới: " + to);

                    // Nghỉ 2 giây giữa mỗi lần gửi để tránh bị Server coi là Spam
                    Thread.sleep(2000);

                } catch (MessagingException e) {
                    System.err.println("[ERROR] Lỗi gửi tới " + to + ": " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    // Đừng quên đóng executor khi tắt ứng dụng
    public void shutdown() {
        executor.shutdown();
    }
}