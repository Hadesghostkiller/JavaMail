package vn.edu.hcmus.mail.main;

import vn.edu.hcmus.mail.model.EmailContent;
import vn.edu.hcmus.mail.service.SmtpService;
import java.util.ArrayList;
import java.util.Arrays;

public class SmtpTester {
    private final SmtpService service;

    public SmtpTester(SmtpService service) {
        this.service = service;
    }

    // Hàm này giờ chỉ để tham khảo, không nên gọi tự động nữa
    public void execAllTests() {
        System.out.println("\n[SYSTEM] Chế độ chạy thử SMTP (Manual Test via GUI)...");

        // Test 1: Gửi đơn lẻ (để null cho filePaths)
        try {
            EmailContent testMail = new EmailContent("thuan_java@outlook.com", "Test", "Body");
            service.send(testMail);
        } catch (Exception e) {
            System.err.println("Lỗi test: " + e.getMessage());
        }

        // Test 2: Gửi hàng loạt (Chỉ truyền 4 tham số như hàm mới bạn vừa sửa)
        ArrayList<String> recipients = new ArrayList<>(Arrays.asList("test1@gmail.com", "test2@gmail.com"));
        service.send_Bulk(recipients, "Subject", "Body", null); // null nghĩa là không có đính kèm
    }
}