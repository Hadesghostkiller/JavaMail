package vn.edu.hcmus.mail.main;

import vn.edu.hcmus.mail.gui.MainFrame;
import vn.edu.hcmus.mail.model.EmailContent;
import vn.edu.hcmus.mail.service.Pop3Service;
import vn.edu.hcmus.mail.service.SmtpService;

import javax.swing.SwingUtilities;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        // Thiết kế giao diện hiện đại
        MainFrame.setupLook();

        // Chạy GUI trên luồng an toàn (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });

        EmailContent email = new EmailContent(
                "imtheone.tellmeurname@gmail.com",
                "Test De Tai Lap Trinh Mang",
                "Hi fen, email nay test dinh kem file."
        );

        // --- PHAN CUA THANH (NGUOI SO 2) ---
        ArrayList<String> files = new ArrayList<>();
        files.add("D:/NGUYEN_VU_NHAT_THANH/Nam_4_HK2/Lap_trinh_mang/Li_thuyet/Chap1.ppt");
        email.setAttachmentPaths(files);
        // -----------------------------------

        // 2. Khoi tao dich vu
        SmtpService smtpService = new SmtpService();

        try {
            System.out.println("Dang bat dau gui mail...");
            smtpService.send(email);
            System.out.println("=> Gui thanh cong!");
        } catch (Exception e) {
            // Thay printStackTrace bang System.err de IDE het bao loi logging
            System.err.println("=> Loi gui mail: " + e.getMessage());
        }

        // 3. Nhan mail
        System.out.println("\n--- Dang kiem tra hop thu den ---");
        Pop3Service pop3Service = new Pop3Service();
        pop3Service.receive();
    }
}