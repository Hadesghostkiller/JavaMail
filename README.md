# 📧 ĐỒ ÁN LẬP TRÌNH MẠNG: JAVA MAIL CLIENT (SMTP & POP3)

Dự án này triển khai việc gửi và nhận thư dựa trên các giao thức TCP tầng ứng dụng, bám sát nội dung tài liệu về lập trình Socket và mô hình Client-Server.

---

## 📂 1. CẤU TRÚC DỰ ÁN (PROJECT STRUCTURE)

Tạo các Package trong thư mục `src/main/java` như sau:
- `vn.edu.hcmus.mail.config`: Chứa cấu hình kết nối.
- `vn.edu.hcmus.mail.model`: Định nghĩa đối tượng Email.
- `vn.edu.hcmus.mail.service`: Chứa logic gửi/nhận (SMTP/POP3).
- `vn.edu.hcmus.mail.main`: Chứa hàm chạy chương trình.

---

## 🛠 2. CẤU HÌNH THƯ VIỆN (POM.XML)

Thêm đoạn này vào giữa cặp thẻ `<dependencies>` trong file `pom.xml`:

    <dependency>
        <groupId>com.sun.mail</groupId>
        <artifactId>javax.mail</artifactId>
        <version>1.6.2</version>
    </dependency>

---

## 💻 3. MÃ NGUỒN CHI TIẾT

### 📄 vn.edu.hcmus.mail.config.MailConfig

    // THAY ĐỔI THÔNG TIN TẠI ĐÂY
    public static final String EMAIL_USER = "email_cua_ban@gmail.com";
    public static final String APP_PASSWORD = "16_ky_tu_mat_khau_ung_dung"; 
}

### 📄 vn.edu.hcmus.mail.model.EmailContent


### 📄 vn.edu.hcmus.mail.service.SmtpService


### 📄 vn.edu.hcmus.mail.service.Pop3Service


### 📄 vn.edu.hcmus.mail.main.Main


---

## 🔐 4. HƯỚNG DẪN CẤU HÌNH GMAIL (QUAN TRỌNG)

1. **App Password (Mật khẩu ứng dụng):**
   - Vào [Tài khoản Google > Bảo mật].
   - Bật **Xác minh 2 bước**.
   - Tìm "Mật khẩu ứng dụng", tạo mã 16 ký tự và dán vào `MailConfig.java` (xóa khoảng trắng).

2. **Bật POP3 Access:**
   - Vào [Gmail trên web > Cài đặt > Chuyển tiếp và POP/IMAP].
   - Chọn **Bật POP cho tất cả thư**.
   - Nhấn **Lưu thay đổi** ở dưới cùng.

---

## 🚀 5. HƯỚNG DẪN MỚI CHO INTELJ (CLONE REPO)

Nếu bạn mới cài IntelliJ và chưa biết cách lấy code về, hãy làm theo các bước sau:

### Bước 1: Lấy đường dẫn dự án
- Truy cập vào link GitHub của nhóm.
- Nhấn nút màu xanh **Code**, sau đó copy đường dẫn (ví dụ: `https://github.com/user/JavaMailProject.git`).

### Bước 2: Clone vào IntelliJ IDEA
- Mở IntelliJ lên. Nếu đang ở màn hình chào mừng, chọn **Get from VCS**.
- Nếu đang mở một project khác, vào menu **File > New > Project from Version Control...**
- Tại ô **URL**, dán đường dẫn vừa copy ở Bước 1 vào.
- Tại ô **Directory**, chọn thư mục trên máy bạn muốn lưu code.
- Nhấn nút **Clone**.



### Bước 3: Tin tưởng dự án (Trust Project)
- Một bảng thông báo hiện lên hỏi bạn có tin tưởng project này không, hãy chọn **Trust Project**.

### Bước 4: Tải thư viện Maven (Cực kỳ quan trọng)
- Khi code mở ra, bạn sẽ thấy nhiều dòng bị lỗi đỏ. Đừng lo!
- Nhìn sang cạnh phải màn hình, nhấn vào biểu tượng chữ **M** (Maven).
- Nhấn vào biểu tượng **mũi tên xoay vòng (Reload All Maven Projects)**.
- Đợi IntelliJ tải thư viện xong (thanh Progress ở dưới cùng chạy hết), code sẽ hết lỗi đỏ.

# 📧 Java Mail Project - LTM Group

### Hướng dẫn nhanh cho thành viên:
1. **Clone** repo này về máy.
2. Nhấn **Reload Maven** trong IntelliJ để tải thư viện.
3. Tự tạo **App Password** trên Gmail cá nhân.
4. Bật **POP Access** trong cài đặt Gmail trên trình duyệt.
5. Cập nhật `EMAIL_USER` và `APP_PASSWORD` vào file `MailConfig.java` để chạy thử local.
6. **LƯU Ý:** Không commit thông tin mật khẩu thật lên GitHub.
