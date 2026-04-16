# Firebase Realtime Database Setup

## Hướng dẫn cài đặt Firebase cho team

### Bước 1: Tạo Firebase Project

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Đăng nhập bằng tài khoản Google
3. Click **"Add project"**
4. Đặt tên project: `javamail-sync`
5. Tắt Google Analytics (không cần thiết cho đồ án)
6. Click **"Create project"**

### Bước 2: Bật Realtime Database

1. Trong Firebase Console, vào **"Build"** → **"Realtime Database"**
2. Click **"Create Database"**
3. Chọn location gần nhất (Ví dụ: Singapore)
4. Chọn **"Start in test mode"** (sẽ cấu hình rules sau)
5. Click **"Enable"**

### Bước 3: Lấy Database URL

1. Sau khi tạo database, bạn sẽ thấy URL dạng:
   ```
   https://javamail-sync-default-rtdb.firebaseio.com
   ```
2. Copy URL này

### Bước 4: Tạo Service Account (Admin SDK)

1. Trong Firebase Console, click **"Project Settings"** (biểu tượng ⚙️)
2. Chuyển sang tab **"Service accounts"**
3. Click **"Generate new private key"**
4. Confirm và tải file JSON về
5. Đổi tên file thành `serviceAccountKey.json`
6. Copy file vào thư mục `config/` trong project

### Bước 5: Cấu hình quyền Database

1. Trong Firebase Console, vào **"Realtime Database"** → **"Rules"**
2. Thay thế rules bằng:

```json
{
  "rules": {
    "emails": {
      ".read": true,
      ".write": true
    }
  }
}
```

3. Click **"Publish"**

### Bước 6: Cấu hình trong code

Mở file `src/main/java/vn/edu/hcmus/mail/config/FirebaseConfig.java`

Thay đổi giá trị `DATABASE_URL`:

```java
public static final String DATABASE_URL = "https://YOUR-PROJECT-default-rtdb.firebaseio.com";
```

### Bước 7: Cấu trúc thư mục

```
JavaMail/
├── config/
│   └── serviceAccountKey.json    ← File tải từ Firebase Console
├── database/
│   └── mail_history.db           ← Database local SQLite
├── src/
│   └── main/
│       └── java/
│           └── vn/edu/hcmus/mail/
│               ├── firebase/
│               │   ├── FirebaseSyncService.java
│               │   └── FirebaseEmailParser.java
│               └── config/
│                   └── FirebaseConfig.java
```

### Cách hoạt động

1. **Khi gửi email**:
   - Email được lưu vào SQLite local
   - Email được sync lên Firebase Realtime Database

2. **Khi nhận email** (refresh inbox):
   - Email được lưu vào SQLite local
   - Email được sync lên Firebase Realtime Database

3. **Team members khác**:
   - Mỗi khi refresh inbox sẽ pull data từ Firebase
   - Data được merge vào local database

### Troubleshooting

**Lỗi "No such device or address" khi đọc service account file**:
- Kiểm tra file `serviceAccountKey.json` có trong thư mục `config/` không
- Kiểm tra đường dẫn trong `FirebaseConfig.SERVICE_ACCOUNT_PATH`

**Lỗi kết nối Firebase**:
- Kiểm tra Internet connection
- Kiểm tra Database URL đúng chưa
- Kiểm tra Rules đã publish chưa

**Muốn tắt Firebase sync**:
- Đổi `FirebaseConfig.DATABASE_URL` thành giá trị mặc định
- Hoặc comment code sync trong SmtpService/Pop3Service
