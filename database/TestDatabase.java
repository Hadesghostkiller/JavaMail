import database.DatabaseManager;
import database.EmailRepository;
import java.util.List;

public class TestDatabase {
    public static void main(String[] args) {
        // 1. Khởi tạo DB
        DatabaseManager.initDatabase();

        EmailRepository repo = new EmailRepository();

        // 2. Test Lưu (Save)
        System.out.println("--- Đang lưu thử mail... ---");
        repo.saveSentEmail("test@gmail.com", "Chào bạn", "Nội dung test database");
        repo.saveSentEmail("hades@github.com", "Đồ án", "Gửi file báo cáo");

        // 3. Test Đọc (Read)
        System.out.println("--- Danh sách mail trong DB: ---");
        List<String[]> emails = repo.getAllSentEmails();
        for (String[] mail : emails) {
            System.out.println("ID: " + mail[0] + " | To: " + mail[1] + " | Sub: " + mail[2] + " | Date: " + mail[3]);
        }

        // 4. Test Xóa (Delete) - Xóa thử mail ID số 1
        if (!emails.isEmpty()) {
            int idToDelete = Integer.parseInt(emails.get(0)[0]);
            System.out.println("--- Đang xóa mail ID: " + idToDelete + " ---");
            repo.deleteSentEmail(idToDelete);
        }
    }
}