package vn.edu.hcmus.mail.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import vn.edu.hcmus.mail.config.FirebaseConfig;
import vn.edu.hcmus.mail.model.Email;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FirebaseSyncService {
    private static FirebaseSyncService instance;
    private FirebaseDatabase database;
    private boolean isConnected = false;
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FirebaseSyncService() {
        initializeFirebase();
    }

    public static synchronized FirebaseSyncService getInstance() {
        if (instance == null) {
            instance = new FirebaseSyncService();
        }
        return instance;
    }

    private void initializeFirebase() {
        try {
            FileInputStream serviceAccount = new FileInputStream(FirebaseConfig.SERVICE_ACCOUNT_PATH);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(serviceAccount)
                    .setDatabaseUrl(FirebaseConfig.DATABASE_URL)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            database = FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL);
            database.getReference(".info/connected").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    isConnected = snapshot.getValue(Boolean.class);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    isConnected = false;
                }
            });
            System.out.println("[FIREBASE] Kết nối Firebase thành công!");
        } catch (IOException e) {
            System.err.println("[FIREBASE] Lỗi khởi tạo: " + e.getMessage());
            System.err.println("[FIREBASE] Vui lòng thêm file serviceAccountKey.json vào thư mục config/");
            isConnected = false;
        }
    }

    public void syncSentEmail(Email email) {
        if (!FirebaseConfig.isEnabled()) {
            System.out.println("[FIREBASE] Firebase chưa được kích hoạt. Bỏ qua sync.");
            return;
        }
        new Thread(() -> {
            try {
                DatabaseReference ref = database.getReference("emails/sent").push();
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("msgId", email.getMsgId());
                emailData.put("fromEmail", email.getFromEmail());
                emailData.put("toEmail", email.getToEmail());
                emailData.put("subject", email.getSubject());
                emailData.put("body", email.getBody());
                emailData.put("timestamp", email.getTimestamp().format(DATE_FORMAT));
                emailData.put("attachments", email.getAttachments() != null ? email.getAttachments() : new java.util.ArrayList<>());
                emailData.put("syncStatus", "synced");

                ref.setValueAsync(emailData);
                System.out.println("[FIREBASE] Đã sync email đã gửi: " + email.getSubject());
            } catch (Exception e) {
                System.err.println("[FIREBASE] Lỗi sync sent email: " + e.getMessage());
            }
        }).start();
    }

    public void syncReceivedEmail(Email email) {
        if (!FirebaseConfig.isEnabled()) {
            System.out.println("[FIREBASE] Firebase chưa được kích hoạt. Bỏ qua sync.");
            return;
        }
        new Thread(() -> {
            try {
                DatabaseReference ref = database.getReference("emails/received").push();
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("msgId", email.getMsgId());
                emailData.put("fromEmail", email.getFromEmail());
                emailData.put("toEmail", email.getToEmail());
                emailData.put("subject", email.getSubject());
                emailData.put("body", email.getBody());
                emailData.put("timestamp", email.getTimestamp().format(DATE_FORMAT));
                emailData.put("isRead", email.isRead());
                emailData.put("syncStatus", "synced");

                ref.setValueAsync(emailData);
                System.out.println("[FIREBASE] Đã sync email nhận: " + email.getSubject());
            } catch (Exception e) {
                System.err.println("[FIREBASE] Lỗi sync received email: " + e.getMessage());
            }
        }).start();
    }

    public void pullAllEmails(DataCallback callback) {
        if (!FirebaseConfig.isEnabled()) {
            callback.onError("Firebase chưa được kích hoạt");
            return;
        }
        new Thread(() -> {
            try {
                DatabaseReference ref = database.getReference("emails");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        callback.onSuccess(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public void pullSentEmails(DataCallback callback) {
        if (!FirebaseConfig.isEnabled()) {
            callback.onError("Firebase chưa được kích hoạt");
            return;
        }
        new Thread(() -> {
            try {
                DatabaseReference ref = database.getReference("emails/sent");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        callback.onSuccess(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public void pullReceivedEmails(DataCallback callback) {
        if (!FirebaseConfig.isEnabled()) {
            callback.onError("Firebase chưa được kích hoạt");
            return;
        }
        new Thread(() -> {
            try {
                DatabaseReference ref = database.getReference("emails/received");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        callback.onSuccess(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onError(databaseError.getMessage());
                    }
                });
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public interface DataCallback {
        void onSuccess(DataSnapshot dataSnapshot);
        void onError(String error);
    }
}
