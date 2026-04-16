# Supabase Realtime Database Setup

## Hướng dẫn cài đặt Supabase cho team

### Bước 1: Tạo tài khoản Supabase

1. Truy cập [supabase.com](https://supabase.com/)
2. Đăng ký / Đăng nhập bằng GitHub account
3. Click **"New project"**

### Bước 2: Tạo Project mới

1. Đặt tên project: `javamail-sync`
2. Chọn region gần nhất (Ví dụ: Singapore)
3. Đặt Database Password (lưu lại!)
4. Click **"Create new project"**
5. Đợi project được tạo (~2 phút)

### Bước 3: Lấy API Keys

1. Vào **Project Settings** (biểu tượng ⚙️)
2. Chọn tab **API**
3. Copy các giá trị:
   - **Project URL**: `https://xxxxx.supabase.co`
   - **anon public** key
   - **service_role** key (cần thiết để ghi data)

### Bước 4: Cấu hình trong code

Mở file `src/main/java/vn/edu/hcmus/mail/config/SupabaseConfig.java`

```java
public static String SUPABASE_URL = "https://xxxxx.supabase.co";
public static String SUPABASE_ANON_KEY = "eyJhbGc...";
public static String SUPABASE_SERVICE_KEY = "eyJhbGc...";
```

### Bước 5: Tạo Database Tables

Trong Supabase Dashboard → **SQL Editor** → Chạy SQL sau:

```sql
-- Tạo bảng sent_emails
CREATE TABLE sent_emails (
    id BIGSERIAL PRIMARY KEY,
    msg_id TEXT UNIQUE,
    from_email TEXT NOT NULL,
    to_email TEXT NOT NULL,
    subject TEXT,
    body TEXT,
    attachments JSONB,
    timestamp TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Tạo bảng received_emails
CREATE TABLE received_emails (
    id BIGSERIAL PRIMARY KEY,
    msg_id TEXT UNIQUE,
    from_email TEXT NOT NULL,
    to_email TEXT NOT NULL,
    subject TEXT,
    body TEXT,
    timestamp TIMESTAMPTZ,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Bật Row Level Security (RLS)
ALTER TABLE sent_emails ENABLE ROW LEVEL SECURITY;
ALTER TABLE received_emails ENABLE ROW LEVEL SECURITY;

-- Cho phép đọc/ghi không giới hạn (cho đồ án)
CREATE POLICY "Allow all" ON sent_emails FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all" ON received_emails FOR ALL USING (true) WITH CHECK (true);
```

Click **"Run"** để tạo bảng.

### Cấu trúc thư mục

```
JavaMail/
├── config/
│   └── .env                    ← File môi trường (không push lên Git)
├── database/
│   └── mail_history.db         ← Database local SQLite
├── src/
│   └── main/
│       └── java/
│           └── vn/edu/hcmus/mail/
│               ├── supabase/
│               │   ├── SupabaseSyncService.java
│               │   └── SupabaseEmailParser.java
│               └── config/
│                   └── SupabaseConfig.java
```

### Cách hoạt động

1. **Khi gửi email**:
   - Email được lưu vào SQLite local
   - Email được sync lên Supabase

2. **Khi nhận email** (refresh inbox):
   - Email được lưu vào SQLite local
   - Email được sync lên Supabase

3. **Team members khác**:
   - Nhấn **"Pull từ Supabase"** để lấy data từ cloud
   - Data được merge vào local database

### Troubleshooting

**Lỗi kết nối Supabase**:
- Kiểm tra Internet connection
- Kiểm tra URL và API key đúng chưa
- Kiểm tra RLS policies đã enable chưa

**Muốn tắt Supabase sync**:
- Comment code sync trong `SmtpService.java` và `Pop3Service.java`
- Hoặc đổi giá trị `isEnabled = false` trong `SupabaseConfig.java`

###Ưu điểm của Supabase so với Firebase

- ✅ Open-source, self-hostable
- ✅ PostgreSQL - quen thuộc với developer
- ✅ Miễn phí tier cao hơn
- ✅ Không giới hạn projects
- ✅ API RESTful đơn giản
