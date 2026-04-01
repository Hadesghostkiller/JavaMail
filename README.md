Đồ án Lập trình mạng: Chương trình Gửi & Nhận Mail (Java)
Chào mừng các thành viên trong nhóm đến với dự án! Đây là ứng dụng Java Client cho phép gửi email qua giao thức SMTP và nhận email qua giao thức POP3, kết nối trực tiếp với Mail Server của Google (Gmail).

📌 Các tính năng chính
[x] Gửi Email (SMTP): Hỗ trợ gửi thư văn bản thuần túy qua cổng bảo mật TLS (587).

[x] Nhận Email (POP3): Kết nối tới Inbox, lấy danh sách và thông tin các thư mới nhất qua cổng SSL (995).

[ ] Giao diện (GUI): (Đang phát triển) - Dự kiến sử dụng Java Swing/JavaFX.

📂 Cấu trúc Project
Dự án được tổ chức theo mô hình phân lớp để dễ quản lý:

Plaintext
src/main/java/vn/edu/hcmus/mail/
├── config/   # Chứa cấu hình hệ thống (Host, Port, Account)
├── model/    # Định nghĩa các đối tượng dữ liệu (EmailContent)
├── service/  # Logic xử lý giao thức mạng (SmtpService, Pop3Service)
└── main/     # Điểm chạy chương trình (Main class)
🛠 Yêu cầu hệ thống
Java JDK: Phiên bản 17 trở lên (Project đang dùng OpenJDK 25).

IDE: IntelliJ IDEA (khuyến khích).

Build Tool: Maven (để tự động quản lý thư viện javax.mail).

🚀 Các bước bắt đầu (Dành cho thành viên mới)
1. Clone Project
Mở Terminal hoặc dùng tính năng Get from VCS trong IntelliJ:

Bash
git clone <link-github-cua-nhom>
2. Cài đặt Thư viện
Sau khi mở Project, IntelliJ sẽ tự nhận diện file pom.xml. Hãy nhấn nút Load Maven Changes (biểu tượng chữ M) để tải thư viện javax.mail.

3. Cấu hình tài khoản Gmail (Bắt buộc)
Để ứng dụng có thể kết nối với Google, mỗi thành viên cần tự chuẩn bị:

Bật xác minh 2 bước cho tài khoản Gmail cá nhân.

Tạo Mật khẩu ứng dụng (App Password): Truy cập Google Security, tạo mật khẩu 16 ký tự cho ứng dụng "Thư".

Bật POP3 trong Gmail: Vào Cài đặt Gmail -> Chuyển tiếp và POP/IMAP -> Chọn Bật POP cho tất cả thư.

4. Thiết lập cấu hình local
Mở file vn.edu.hcmus.mail.config.MailConfig và điền thông tin của bạn:

Java
public static final String EMAIL_USER = "email_cua_ban@gmail.com";
public static final String APP_PASSWORD = "xxxx-xxxx-xxxx-xxxx"; // 16 ký tự viết liền
📖 Cách chạy chương trình
Tìm đến file vn.edu.hcmus.mail.main.Main.

Nhấn chuột phải chọn Run 'Main.main()'.

Kiểm tra cửa sổ Console:

Phần trên sẽ báo kết quả gửi mail.

Phần dưới sẽ liệt kê 5 email mới nhất trong Inbox của bạn.
