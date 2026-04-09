
public class TestDB {
    public static void main(String[] args) {
        // Khởi tạo DatabaseManager (tự động tạo bảng nếu chưa có)
        DatabaseManager db = new DatabaseManager();

        // Chèn vài bản ghi thử nghiệm
        db.saveSentEmail("me@example.com", "friend@example.com", "Hello", "Nội dung email test 1");
        db.saveSentEmail("mywork@company.com", "boss@company.com", "Báo cáo", "Đây là báo cáo tuần");
        db.saveSentEmail("no-reply@service.com", "user@example.com", "Xác nhận", "Tài khoản của bạn đã được tạo");

        // Đọc và in ra tất cả sent email đã lưu
        db.printAllSentEmails();
    }
}