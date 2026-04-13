package vn.edu.hcmus.mail.main;

import vn.edu.hcmus.mail.config.MailConfig;
import vn.edu.hcmus.mail.gui.MainFrame;
import vn.edu.hcmus.mail.model.EmailContent;
import vn.edu.hcmus.mail.service.Pop3Service;
import vn.edu.hcmus.mail.service.SmtpService;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        // 1. Thiết kế giao diện hiện đại (Giữ nguyên logic cũ)
        setupUI();

        // Khởi tạo dịch vụ SMTP duy nhất
        SmtpService smtpService = new SmtpService();

        // Lấy cấu hình Properties dùng cho Outlook và Bulk
        Properties smtpProps = getSmtpProperties();

        System.out.println("\n[SYSTEM] BAT DAU CHAY CAC BUOC KIEM THU...");

        // 2. Gửi mail đơn kèm file đính kèm (Sử dụng hàm send mặc định)
        runSingleSendWithAttachment(smtpService);

        // 3. Gửi mail tới Outlook (Sử dụng hàm send_MultiServer)
        runOutlookTest(smtpService, smtpProps);

        // 4. Gửi mail hàng loạt (Sử dụng hàm send_Bulk tới 2 mail bạn chọn)
        runBatchSendTest(smtpService, smtpProps);

        // 5. Nhận mail (Logic cũ)
        runReceiveMailTest();
    }

    private static void setupUI() {
        MainFrame.setupLook();
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    private static Properties getSmtpProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", MailConfig.SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(MailConfig.SMTP_PORT));
        return props;
    }

    /**
     * Test gửi Gmail đơn kèm file đính kèm như logic cũ của bạn
     */
    private static void runSingleSendWithAttachment(SmtpService service) {
        System.out.println("\n--- STEP 1: GUI MAIL KEM FILE DINH KEM ---");
        EmailContent email = new EmailContent(
                "nguyenducthuan30102004@gmail.com",
                "Test De Tai Lap Trinh Mang",
                "Hi fen, email nay test dinh kem file."
        );

        ArrayList<String> files = new ArrayList<>();
        files.add("D:/Downloads/btcc_b6.pdf");
        email.setAttachmentPaths(files);

        try {
            System.out.println("Dang bat dau gui mail qua Gmail...");
            service.send(email); // Gọi hàm send() có xử lý Attachment
            System.out.println("=> Gui thanh cong!");
        } catch (Exception e) {
            System.err.println("=> Loi gui mail: " + e.getMessage());
        }
    }

    /**
     * Test gửi liên server tới Outlook
     */
    private static void runOutlookTest(SmtpService service, Properties props) {
        System.out.println("\n--- STEP 2: GUI MAIL TOI OUTLOOK ---");
        EmailContent toOutlook = new EmailContent(
                "thuan_java@outlook.com",
                "Test Gmail sang Outlook",
                "Email nay test chuc nang send_MultiServer."
        );

        try {
            service.send_MultiServer(toOutlook, props, MailConfig.EMAIL_USER, MailConfig.APP_PASSWORD);
            System.out.println("=> Gui toi Outlook thanh cong!");
        } catch (Exception e) {
            System.err.println("=> Loi gui Outlook: " + e.getMessage());
        }
    }

    /**
     * Test gửi hàng loạt tới 2 địa chỉ dutthuan và dutthun
     */
    private static void runBatchSendTest(SmtpService service, Properties props) {
        System.out.println("\n--- STEP 3: GUI MAIL HANG LOAT (Send_Bulk) ---");

        ArrayList<String> recipients = new ArrayList<>(Arrays.asList(
                "dutthuan@gmail.com",
                "dutthun@gmail.com"
        ));

        service.send_Bulk(
                recipients,
                "Thong bao tu Send_Bulk",
                "Noi dung gui dong thoi toi nhieu nguoi.",
                props,
                MailConfig.EMAIL_USER,
                MailConfig.APP_PASSWORD
        );
    }

    /**
     * Kiểm tra hòm thư đến
     */
    private static void runReceiveMailTest() {
        System.out.println("\n--- STEP 4: DANG KIEM TRA HOP THU DEN ---");
        Pop3Service pop3Service = new Pop3Service();
        pop3Service.receive();
    }
}