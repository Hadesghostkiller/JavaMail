package vn.edu.hcmus.mail.main;

import vn.edu.hcmus.mail.gui.MainFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 1. Cấu hình giao diện bóng bẩy
        MainFrame.setupLook();

        // 2. CHỈ CHẠY DUY NHẤT CỬA SỔ GUI
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
            System.out.println("[SYSTEM] Ứng dụng đã sẵn sàng. Hãy thao tác trên GUI.");
        });

        // --- ĐÃ XÓA: Các lệnh AttachmentTask, SmtpTester, Pop3Service cũ ---
        // Không còn tình trạng vừa bật app lên là nó tự gửi mail nữa!
    }
}