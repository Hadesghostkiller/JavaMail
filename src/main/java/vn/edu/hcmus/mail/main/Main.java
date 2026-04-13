package vn.edu.hcmus.mail.main;

import vn.edu.hcmus.mail.gui.MainFrame;
import vn.edu.hcmus.mail.service.Pop3Service;
import vn.edu.hcmus.mail.service.SmtpService;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 1. Thiết kế giao diện hiện đại (Giữ nguyên logic cũ)
        MainFrame.setupLook();

        // 2. Chạy GUI trên luồng an toàn (Giữ nguyên logic cũ)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });

        // 3. GỌI DUY NHẤT 1 HÀM TỪ SMTP TESTER
        new SmtpTester(new SmtpService()).execAllTests();

        // 4. Nhận mail (Logic cũ)
        System.out.println("\n--- Đang kiểm tra hộp thư đến (POP3) ---");
        new Pop3Service().receive();
    }
}