package vn.edu.hcmus.mail.main;

import vn.edu.hcmus.mail.model.EmailContent;
import vn.edu.hcmus.mail.service.SmtpService;
import vn.edu.hcmus.mail.service.Pop3Service;
public class Main {
    public static void main(String[] args) {
        // 1. Chuẩn bị nội dung email
        EmailContent email = new EmailContent(
                "imtheone.tellmeurname@gmail.com",
                "Test Đề Tài Lập Trình Mạng",
                "Hi fen lần 3, đây là email gửi từ ứng dụng Java sử dụng giao thức SMTP."
        );

        // 2. Khởi tạo dịch vụ gửi mail
        SmtpService smtpService = new SmtpService();

        try {
            System.out.println("Đang bắt đầu quá trình gửi mail...");

            // 3. Thực hiện gửi
            smtpService.send(email);

            System.out.println("=> Kết quả: Email đã được gửi thành công qua giao thức TCP/IP!");
        } catch (Exception e) {
            System.err.println("=> Lỗi: Không thể gửi mail.");
            e.printStackTrace();
        }
        // 2. Nhận mail
        System.out.println("\n--- Đang kiểm tra hộp thư đến ---");
        Pop3Service pop3Service = new Pop3Service();
        pop3Service.receive();
    }
//test commit vô dev_test branch thên
}