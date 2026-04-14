package vn.edu.hcmus.mail.service;

import vn.edu.hcmus.mail.config.MailConfig;
import vn.edu.hcmus.mail.model.EmailContent;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmtpService {

    // Thread pool quản lý tối đa 5 luồng gửi mail song song
    private final ExecutorService threadPool = Executors.newFixedThreadPool(5);


     /// 1. HÀM GỬI GMAIL (Mặc định - Hỗ trợ đính kèm file)
    public void send(EmailContent email) throws MessagingException, UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.debug", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", MailConfig.SMTP_HOST);
        props.put("mail.smtp.port", MailConfig.SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MailConfig.EMAIL_USER, MailConfig.APP_PASSWORD);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(MailConfig.EMAIL_USER));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.getTo()));
        message.setSubject(email.getSubject(), "UTF-8");

        // Xử lý Multipart (Nội dung văn bản + File đính kèm)
        Multipart multipart = new MimeMultipart();

        // Phần text của email
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(email.getBody(), "UTF-8");
        multipart.addBodyPart(textPart);

        // Phần xử lý file đính kèm
        if (email.getAttachmentPaths() != null) {
            for (String filePath : email.getAttachmentPaths()) {
                File file = new File(filePath);
                if (file.exists() && file.length() <= 25 * 1024 * 1024) { // Giới hạn 25MB
                    MimeBodyPart attachPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(filePath);
                    attachPart.setDataHandler(new DataHandler(source));

                    String fileName = MimeUtility.encodeText(file.getName(), "UTF-8", "B");
                    attachPart.setFileName(fileName);
                    multipart.addBodyPart(attachPart);
                } else {
                    System.out.println("=> File không hợp lệ hoặc quá 25MB: " + file.getName());
                }
            }
        }

        message.setContent(multipart);
        Transport.send(message);
    }


     /// 2. HÀM GỬI LIÊN SERVER (Outlook, Yahoo,...)
    public void send_MultiServer(EmailContent content, Properties config, String user, String pass) throws MessagingException {
        Session session = Session.getInstance(config, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        session.setDebug(true); // Bật để theo dõi log TCP/IP

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(user));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(content.getTo()));
        message.setSubject(content.getSubject());
        message.setText(content.getBody());

        Transport.send(message);
    }


    ///3. HÀM GỬI HÀNG LOẠT (Bulk Mail)
    public void send_Bulk(List<String> recipients, String subject, String body, List<String> filePaths) {
        for (String emailAddr : recipients) {
            threadPool.execute(() -> {
                try {
                    // Tạo đối tượng mail có đầy đủ cả chữ lẫn danh sách file
                    EmailContent email = new EmailContent(emailAddr, subject, body);
                    email.setAttachmentPaths(filePaths); // <== QUAN TRỌNG: Phải nạp file vào đây

                    // GỌI CHÍNH HÀM SEND (Hàm này đã có logic xử lý Multipart rồi)
                    send(email);

                    System.out.println("[SUCCESS] Đã gửi kèm file tới: " + emailAddr);
                    Thread.sleep(2000);
                } catch (Exception e) {
                    System.err.println("[ERROR] Lỗi tại: " + emailAddr + " -> " + e.getMessage());
                }
            });
        }
    }

    /**
     * Dọn dẹp tài nguyên khi tắt ứng dụng
     */
    public void stopService() {
        if (!threadPool.isShutdown()) {
            threadPool.shutdown();
        }
    }
}