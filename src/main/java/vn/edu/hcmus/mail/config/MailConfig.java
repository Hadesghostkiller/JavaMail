package vn.edu.hcmus.mail.config;

import java.util.Properties;

public class MailConfig {

    // --- THÔNG TIN TÀI KHOẢN  ---
    public static final String EMAIL_USER = "doanltmang@gmail.com";
    public static final String APP_PASSWORD = "sowyawumjpziavyb";

    // Gmail
    public static final String GMAIL_SMTP = "smtp.gmail.com";
    public static final String GMAIL_POP3 = "pop.gmail.com";


    /**
     * Cấu hình SMTP để gửi mail
     * @param host: Địa chỉ máy chủ (ví dụ: smtp.gmail.com)
     * @param port: Cổng (thường là 587 cho TLS)
     */
    public static Properties getSmtpProperties(String host, int port) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        // --- CẤU HÌNH BỔ SUNG CHO OUTLOOK ---
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");


        props.put("mail.smtp.connectiontimeout", "30000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.writetimeout", "30000");

        return props;
    }

    /**
     * Cấu hình POP3 để nhận mail
     * @param host: Địa chỉ máy chủ (ví dụ: pop.gmail.com)
     * @param port: Cổng (thường là 995 cho SSL)
     */
    public static Properties getPop3Properties(String host, int port) {
        Properties props = new Properties();
        props.put("mail.pop3.host", host);
        props.put("mail.pop3.port", String.valueOf(port));

        // Gmail và Outlook bắt buộc dùng SSL cho POP3
        props.put("mail.pop3.ssl.enable", "true");
        props.put("mail.store.protocol", "pop3s");

        return props;
    }
}