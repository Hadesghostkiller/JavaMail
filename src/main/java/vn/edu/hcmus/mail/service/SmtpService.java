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
import java.util.Properties;

public class SmtpService {
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
        // message.setText(email.getBody());

        // 1. Tạo thùng chứa Multipart
        Multipart multipart = new MimeMultipart();

        // 2. Tạo phần nội dung văn bản (Body)
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(email.getBody(), "UTF-8");
        multipart.addBodyPart(textPart);

        // 3. Xử lý danh sách đính kèm
        if (email.getAttachmentPaths() != null) {
            for (String filePath : email.getAttachmentPaths()) {
                File file = new File(filePath);

                // Kiểm tra dung lượng < 25MB
                if (file.exists() && file.length() <= 25 * 1024 * 1024) {
                    MimeBodyPart attachPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(filePath);
                    attachPart.setDataHandler(new DataHandler(source));

                    // Mã hóa tên file để tránh lỗi font tiếng Việt
                    String fileName = MimeUtility.encodeText(file.getName(), "UTF-8", "B");
                    attachPart.setFileName(fileName);

                    multipart.addBodyPart(attachPart);
                } else {
                    System.out.println("=> File không hợp lệ hoặc quá 25MB: " + file.getName());
                }
            }
        }

        // 4. Gán toàn bộ multipart vào message
        message.setContent(multipart);

        Transport.send(message);
    }
}