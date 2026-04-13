package vn.edu.hcmus.mail.main;

import vn.edu.hcmus.mail.config.MailConfig;
import vn.edu.hcmus.mail.model.EmailContent;
import vn.edu.hcmus.mail.service.Pop3Service;
import vn.edu.hcmus.mail.service.SmtpService;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Main {
     static void main(String[] args) {




        // --- 1. KHỞI TẠO HỆ THỐNG ---
        SmtpService smtpService = new SmtpService();
        Pop3Service pop3Service = new Pop3Service();

        // Sử dụng cấu hình Gmail SMTP
        Properties smtpPropsGmail = MailConfig.getSmtpProperties(MailConfig.GMAIL_SMTP, 587);
        Properties pop3PropsGmail = MailConfig.getPop3Properties(MailConfig.GMAIL_POP3, 995);

        // Thông tin tài khoản Gmail
        String senderUser = MailConfig.EMAIL_USER;
        String senderPass = MailConfig.APP_PASSWORD;





        ////// Gửi tới cái Outlook thuan_java@outlook.com////////////
        EmailContent toOutlook = new EmailContent(
                "thuan_java@outlook.com",
                "Test gửi từ Gmail sang Outlook - Đồ án Lập trình mạng",
                "Chào fen, đây là email gửi từ Server Gmail tới Server Outlook."
        );
        System.out.println("======================================================");
        System.out.println("STEP 1: GỬI MAIL tới outlook");

        try {
            System.out.println(">>> Đang kết nối tới SMTP Gmail để gửi tới Outlook...");
            smtpService.send(toOutlook, smtpPropsGmail, senderUser, senderPass);
            System.out.println("=> KẾT QUẢ: Gửi tới Outlook thành công!");
        } catch (Exception e) {
            System.err.println("=> LỖI: Không thể gửi mail liên server.");
        }





        //////// Dùng STMP của gmail để Gửi hàng loạt///////////////
        List<String> recipients = Arrays.asList(
                "nguyenducthuan30102004@gmail.com",
                "thuan_java@outlook.com"
                // Gửi cho cả 2 server luôn
        );

        System.out.println("\n======================================================");
        System.out.println("STEP 2: GỬI HÀNG LOẠT (SỬ DỤNG MULTI-THREADING)");
        System.out.println(">>> Đang đẩy danh sách vào hàng đợi gửi ngầm...");
        smtpService.sendBulk(
                recipients,
                "Thông báo Bulk Email",
                "Hello các con vợ, các ngươi có đoán được ta là ai không? để ta gợi ý nhé, ta là một sumurai.",
                smtpPropsGmail, senderUser, senderPass);





        // Đọc từ Gmail để lấy kết quả
        System.out.println("\n======================================================");
        System.out.println("STEP 3: KIỂM TRA HỘP THƯ ĐẾN (POP3 GMAIL)");
        try {
            List<EmailContent> inbox = pop3Service.fetchEmails(pop3PropsGmail, senderUser, senderPass);
            System.out.println(">>> Tổng số thư trong Inbox: " + inbox.size());

            int count = 0;
            for (int i = inbox.size() - 1; i >= 0 && count < 5; i--) {
                EmailContent mail = inbox.get(i);
                System.out.println("---------------------------");
                System.out.println("Thư thứ " + (i + 1) + ":");
                System.out.println("- Từ: " + mail.getTo());
                System.out.println("- Tiêu đề: " + mail.getSubject());
                count++;
            }
        } catch (Exception e) {
            System.err.println("=> LỖI: Không thể đọc thư từ POP3.");
        }


        // --- 5. CHỜ CÁC LUỒNG NGẦM ---
        System.out.println("\n======================================================");
        System.out.println(">>> Đang chờ kết quả gửi Bulk (10s)...");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n--- CHƯƠNG TRÌNH HOÀN TẤT ---");
    }
}