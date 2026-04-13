package vn.edu.hcmus.mail.AttachmentManager;

import vn.edu.hcmus.mail.model.EmailContent;
import vn.edu.hcmus.mail.service.Pop3Service;
import vn.edu.hcmus.mail.service.SmtpService;

import java.util.ArrayList;

public class AttachmentTask {

    public void AttachmentRun() {
        // 1. Chuẩn bị dữ liệu
        EmailContent email = new EmailContent(
                "nhatthanh30072004@gmail.com",
                " De Tai Lap Trinh Mang ",
                "Hi fen, day la email test dinh kem file tu class rieng."
        );

        // Thêm file đính kèm
        ArrayList<String> files = new ArrayList<>();
        files.add("C:/Users/admin/Desktop/4.jpg");
        email.setAttachmentPaths(files);

        // 2. Gửi mail
        SmtpService smtpService = new SmtpService();
        try {
            System.out.println(">>> Đang gửi mail...");
            smtpService.send(email);
            System.out.println(">>> Gửi thành công!");
        } catch (Exception e) {
            System.err.println(">>>  Lỗi gửi mail: " + e.getMessage());
        }

        // 3. Nhận mail
        System.out.println("\n>>> Đang check mail và tải đính kèm...");
        Pop3Service pop3Service = new Pop3Service();
        pop3Service.receive();
    }
}

