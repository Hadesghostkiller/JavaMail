package vn.edu.hcmus.mail.main;

import vn.edu.hcmus.mail.config.MailConfig;
import vn.edu.hcmus.mail.model.EmailContent;
import vn.edu.hcmus.mail.service.SmtpService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class SmtpTester {

    private final SmtpService service;

    public SmtpTester(SmtpService service) {
        this.service = service;
    }

    /**
     * HÀM DUY NHẤT ĐƯỢC GỌI TỪ MAIN
     * Gom tất cả các bước cấu hình và kiểm thử vào đây.
     */
    public void execAllTests() {
        System.out.println("\n[SYSTEM] BẮT ĐẦU CHẠY KIỂM THỬ SMTP TỰ ĐỘNG...");

        // Tự khởi tạo Properties nội bộ
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", MailConfig.SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(MailConfig.SMTP_PORT));

        // Chạy kịch bản 1
        runOutlookTest(props);

        // Chạy kịch bản 2
        runBatchSendTest(props);
    }

    private void runOutlookTest(Properties props) {
        System.out.println("\n--- TEST 1: GỬI LIÊN SERVER (OUTLOOK) ---");
        EmailContent toOutlook = new EmailContent(
                "thuan_java@outlook.com",
                "Test Gmail sang Outlook",
                "Chào fen, đây là email test gửi từ Gmail Server tới Outlook Server."
        );
        try {
            service.send_MultiServer(toOutlook, props, MailConfig.EMAIL_USER, MailConfig.APP_PASSWORD);
            System.out.println("=> KẾT QUẢ: Gửi tới Outlook thành công!");
        } catch (Exception e) {
            System.err.println("=> LỖI: Không thể gửi mail tới Outlook: " + e.getMessage());
        }
    }

    private void runBatchSendTest(Properties props) {
        System.out.println("\n--- TEST 2: GỬI HÀNG LOẠT (BULK SEND) ---");
        ArrayList<String> recipients = new ArrayList<>(Arrays.asList(
                "dutthuan@gmail.com",
                "dutthun@gmail.com"
        ));
        service.send_Bulk(recipients, "Thông báo Bulk", "Nội dung gửi đồng thời.", props, MailConfig.EMAIL_USER, MailConfig.APP_PASSWORD);
    }
}