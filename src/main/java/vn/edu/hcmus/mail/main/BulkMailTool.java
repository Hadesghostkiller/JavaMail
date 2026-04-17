package vn.edu.hcmus.mail.main;

import vn.edu.hcmus.mail.config.MailConfig;
import vn.edu.hcmus.mail.config.SupabaseConfig;
import vn.edu.hcmus.mail.database.EmailCache;
import vn.edu.hcmus.mail.model.Email;
import vn.edu.hcmus.mail.service.SmtpService;

import javax.mail.*;
import javax.mail.internet.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BulkMailTool {

    private static final String[] RECIPIENTS = {
        "nhatthanh30072004@gmail.com",
        "dutthuan@gmail.com",
        "nvy381932@gmail.com",
        "nvy8688@gmail.com"
    };

    private static final String[] SUBJECTS = {
        "Update from JavaMail Project Team",
        "Regarding our Email Client Application",
        "Meeting Notes - JavaMail Implementation",
        "Bug Fix Report - Database Sync Issue",
        "Feature Request: Offline Mode",
        "Testing Report - Supabase Integration",
        "Progress Update - LTMang Course Project",
        "Database Schema Changes Notification",
        "API Documentation Update",
        "Performance Optimization Results",
        "User Feedback Summary",
        "Code Review Request",
        "Deployment Guide Update",
        "Testing Environment Setup",
        "Production Release Notes"
    };

    private static final String[] BODY_TEMPLATES = {
        "Dear Team,\n\nI wanted to provide an update on our JavaMail client project. We have successfully implemented the following features:\n\n1. Email sending functionality using SMTP protocol\n2. Email receiving using POP3 protocol\n3. Local SQLite database for offline storage\n4. Supabase cloud sync for team collaboration\n5. User-friendly GUI with modern FlatLaf design\n\nThe database schema includes sent_emails and received_emails tables. Each email record contains msg_id, from_email, to_email, subject, body, and timestamp fields.\n\nFor cloud synchronization, we are using Supabase Realtime Database which provides PostgreSQL backend with REST API access. This allows all team members to access the same email history regardless of their device.\n\nNext steps:\n- Implement email search functionality\n- Add email filtering by date range\n- Create email labels/categories\n- Optimize cache management\n\nBest regards,\nJavaMail Project Team",

        "Hello,\n\nThis is an automated message from our JavaMail application testing suite. We are currently stress testing the following components:\n\n1. SMTP Service - Sending bulk emails\n2. POP3 Service - Receiving and parsing emails\n3. Database Manager - SQLite operations\n4. Email Cache - LRU caching with 100 email limit\n5. Supabase Sync - Cloud synchronization\n\nThe Supabase configuration requires:\n- Project URL from Supabase dashboard\n- Anon public key for authentication\n- Service role key for admin operations\n\nDatabase table structure:\nCREATE TABLE sent_emails (\n    id BIGSERIAL PRIMARY KEY,\n    msg_id TEXT UNIQUE,\n    from_email TEXT NOT NULL,\n    to_email TEXT NOT NULL,\n    subject TEXT,\n    body TEXT,\n    attachments JSONB,\n    timestamp TIMESTAMPTZ,\n    created_at TIMESTAMPTZ DEFAULT NOW()\n);\n\nPlease review and provide feedback.\n\nBest,\nTest Suite",

        "Hi Team,\n\nI am pleased to share our progress on the JavaMail client for our LTMang (Network Programming) course project.\n\nProject Overview:\nThis application allows users to send and receive emails using Gmail SMTP/POP3 servers. Key features include:\n\n- Send single or bulk emails with attachments\n- Receive emails from POP3 server\n- Store email history in local SQLite database\n- Sync data to Supabase for team collaboration\n- Modern GUI built with Swing and FlatLaf\n\nTechnical Stack:\n- Java 17\n- Maven for dependency management\n- SQLite for local storage\n- Supabase PostgreSQL for cloud storage\n- JavaMail API for email operations\n\nThe application follows MVC architecture with separate layers for:\n- Service layer (SmtpService, Pop3Service)\n- Database layer (DatabaseManager, EmailRepository, EmailCache)\n- GUI layer (MainFrame, custom components)\n\nWe have integrated Supabase for real-time database synchronization. Each team member can view and manage emails from any device.\n\nRegards,\nProject Member",

        "Dear Developers,\n\nThis email contains important information about our OpenCode integration project.\n\nOpenCode is an AI-powered coding assistant that helps developers with various programming tasks. Our team has been using it to accelerate development of the JavaMail client application.\n\nKey integrations:\n1. Database design - SQLite and Supabase schemas\n2. REST API client for Supabase synchronization\n3. Email parsing and formatting\n4. Concurrent email sending with thread pools\n\nThe EmailCache class implements LRU (Least Recently Used) caching with a maximum capacity of 100 emails. This ensures optimal performance while minimizing memory usage.\n\nFor Supabase integration, we use the PostgREST API:\nBase URL: https://your-project.supabase.co/rest/v1\nAuthentication: Bearer token (anon key)\n\nExample POST request:\nPOST /sent_emails\nHeaders:\n  apikey: [ANON_KEY]\n  Authorization: Bearer [ANON_KEY]\n  Content-Type: application/json\n\nBody:\n{\n  \"msg_id\": \"unique-message-id\",\n  \"from_email\": \"sender@example.com\",\n  \"to_email\": \"recipient@example.com\",\n  \"subject\": \"Email Subject\",\n  \"body\": \"Email content\",\n  \"timestamp\": \"2024-01-01T12:00:00\"\n}\n\nThank you for your attention.\n\nBest regards,\nDevelopment Team",

        "Hello,\n\nWe are writing to inform you about our upcoming changes to the JavaMail database architecture.\n\nCurrent Implementation:\n- Local SQLite database for offline access\n- Supabase cloud database for team sync\n- LRU cache for frequently accessed emails\n\nProposed Changes:\n1. Add email indexing for faster search\n2. Implement incremental sync (only new emails)\n3. Add conflict resolution for concurrent edits\n4. Implement email archiving functionality\n\nThe EmailRepository class provides CRUD operations:\n- saveSentEmail(Email email)\n- saveReceivedEmail(Email email)\n- getAllSentEmails()\n- getAllReceivedEmails()\n- searchSentEmails(String keyword)\n- searchReceivedEmails(String keyword)\n- deleteEmailByMsgId(String msgId, EmailType type)\n\nDatabase initialization:\nDatabaseManager.getInstance().initializeDatabase();\n\nCache operations:\nEmailCache cache = EmailCache.getInstance();\ncache.cacheSentEmail(email);\ncache.cacheReceivedEmail(email);\n\nPlease let us know if you have any questions.\n\nRegards,\nDatabase Team",

        "Dear Team Members,\n\nThis is a test email for our Supabase synchronization system.\n\nSupabase is an open-source Firebase alternative that provides:\n1. PostgreSQL database\n2. Real-time subscriptions\n3. REST API (PostgREST)\n4. Authentication\n5. File storage\n\nOur implementation uses the REST API for all database operations:\n\n1. POST /sent_emails - Create sent email record\n2. POST /received_emails - Create received email record\n3. GET /sent_emails?select=* - Fetch all sent emails\n4. GET /received_emails?select=* - Fetch all received emails\n\nRow Level Security (RLS) is enabled for data protection:\nCREATE POLICY \"Allow all\" ON sent_emails FOR ALL USING (true);\n\nEnvironment Configuration:\nSUPABASE_URL=https://your-project.supabase.co\nSUPABASE_ANON_KEY=your-anon-key\n\nBest practices for Supabase:\n- Use proper indexing on frequently queried columns\n- Implement pagination for large datasets\n- Handle rate limiting appropriately\n- Use service role key only for admin operations\n\nThank you,\nSync Team",

        "Hi,\n\nI am writing to document our email client testing procedures.\n\nTest Cases:\n1. Send single email with attachment\n2. Send bulk emails to multiple recipients\n3. Receive emails from POP3 server\n4. Save emails to local SQLite database\n5. Sync emails to Supabase cloud\n6. View email history\n7. Search emails by keyword\n8. Delete emails\n9. Mark emails as read/unread\n10. Test offline mode\n\nTest Data:\n- Sender: doanltmang@gmail.com\n- Recipients: Multiple test accounts\n- Content: Various lengths and formats\n\nExpected Results:\n- All emails saved to SQLite successfully\n- All emails synced to Supabase\n- Email history displays correctly\n- Search returns accurate results\n\nTest Environment:\n- Java 17\n- Maven 3.8+\n- Windows 10/11\n- IntelliJ IDEA\n\nPlease run these tests and report any issues.\n\nBest,\nQA Team",

        "Dear Colleagues,\n\nWe are excited to announce the release of our new JavaMail client features.\n\nNew Features:\n1. Improved email caching algorithm\n2. Faster Supabase synchronization\n3. Enhanced search functionality\n4. Better error handling\n5. Progress indicators for bulk operations\n\nTechnical Details:\nThe EmailCache class now uses ConcurrentHashMap for thread-safe operations:\nprivate final Map<String, Email> sentCache = new ConcurrentHashMap<>();\nprivate final Map<String, Email> receivedCache = new ConcurrentHashMap<>();\n\nThe LRU eviction policy ensures old emails are removed when cache is full:\nprivate void evictOldestSent() {\n    if (!sentAccessOrder.isEmpty()) {\n        String oldestKey = sentAccessOrder.entrySet().iterator().next().getKey();\n        sentCache.remove(oldestKey);\n        sentAccessOrder.remove(oldestKey);\n    }\n}\n\nSupabase sync is performed asynchronously:\nnew Thread(() -> {\n    SupabaseSyncService.getInstance().syncSentEmail(email);\n}).start();\n\nPlease update your local repositories and test these new features.\n\nBest regards,\nDevelopment Team",

        "Hello,\n\nThis email summarizes our project documentation for the JavaMail client.\n\nProject Structure:\nsrc/main/java/vn/edu/hcmus/mail/\n├── config/\n│   ├── MailConfig.java\n│   └── SupabaseConfig.java\n├── database/\n│   ├── DatabaseManager.java\n│   ├── EmailCache.java\n│   └── EmailRepository.java\n├── model/\n│   └── Email.java\n├── service/\n│   ├── Pop3Service.java\n│   └── SmtpService.java\n├── supabase/\n│   ├── SupabaseSyncService.java\n│   └── SupabaseEmailParser.java\n├── gui/\n│   └── MainFrame.java\n└── main/\n    └── Main.java\n\nDependencies (pom.xml):\n- javax.mail:1.6.2\n- sqlite-jdbc:3.44.1.0\n- flatlaf:3.4.1\n- gson:2.10.1\n\nPlease review and update your local setup accordingly.\n\nRegards,\nDocumentation Team",

        "Dear Team,\n\nI am writing to discuss our database backup strategy.\n\nBackup Schedule:\n- Daily automatic backup at midnight\n- Weekly full backup every Sunday\n- Monthly archive on the last day of month\n\nBackup Locations:\n1. Local: database/mail_history.db\n2. Cloud: Supabase PostgreSQL\n3. Offline: User-specified directory\n\nRecovery Procedures:\n1. SQLite restore: Copy .db file to database/ folder\n2. Supabase restore: Use dashboard or pg_dump\n3. Full system restore: Sequential restoration of all backups\n\nFor Supabase backups:\n- Navigate to Database Backups in Dashboard\n- Select backup timestamp\n- Click Restore\n\nFor local SQLite:\n- Stop application\n- Replace mail_history.db\n- Restart application\n\nContact us for any backup-related issues.\n\nBest,\nOperations Team"
    };

    private static final Random random = new Random();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("=== JavaMail Bulk Send Tool ===");
        System.out.println("This tool will send 100 emails to test our database.");
        System.out.println("Recipients: " + Arrays.toString(RECIPIENTS));
        System.out.println();

        int totalEmails = 100;
        int emailsPerRecipient = totalEmails / RECIPIENTS.length;
        int emailCount = 0;

        ExecutorService executor = Executors.newFixedThreadPool(5);

        long startTime = System.currentTimeMillis();

        for (int round = 0; round < emailsPerRecipient; round++) {
            for (String recipient : RECIPIENTS) {
                emailCount++;
                final int count = emailCount;
                final String toEmail = recipient;

                executor.submit(() -> {
                    try {
                        sendRandomEmail(count, toEmail);
                        System.out.println("[" + count + "/100] Sent to: " + toEmail);
                    } catch (Exception e) {
                        System.err.println("[" + count + "/100] Failed: " + toEmail + " - " + e.getMessage());
                    }
                });

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            System.out.println("--- Round " + (round + 1) + "/" + emailsPerRecipient + " completed ---");
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;

        System.out.println();
        System.out.println("=== BULK SEND COMPLETED ===");
        System.out.println("Total emails sent: " + emailCount);
        System.out.println("Duration: " + duration + " seconds");
        System.out.println("Average: " + (emailCount / Math.max(1, duration)) + " emails/second");
        System.out.println();
        System.out.println("All emails have been saved to:");
        System.out.println("- Local SQLite: database/mail_history.db");
        System.out.println("- Cloud Supabase: Check your dashboard");
    }

    private static void sendRandomEmail(int count, String toEmail) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", MailConfig.SMTP_HOST);
        props.put("mail.smtp.port", MailConfig.SMTP_PORT);
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MailConfig.EMAIL_USER, MailConfig.APP_PASSWORD);
            }
        });

        String subject = SUBJECTS[random.nextInt(SUBJECTS.length)];
        if (count % 5 == 0) {
            subject = "[" + count + "] " + subject;
        }

        String body = BODY_TEMPLATES[random.nextInt(BODY_TEMPLATES.length)];
        body += "\n\n---\nEmail #" + count + "\nSent at: " + LocalDateTime.now().format(DATE_FORMAT) + "\n";
        body += "From: " + MailConfig.EMAIL_USER + "\nTo: " + toEmail + "\n";
        body += "Database ID: " + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "\n";

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(MailConfig.EMAIL_USER));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject, "UTF-8");
        message.setText(body, "UTF-8");

        String msgId = UUID.randomUUID().toString();
        message.setHeader("Message-ID", "<" + msgId + ">");

        Transport.send(message);

        Email emailRecord = new Email(Email.EmailType.SENT);
        emailRecord.setMsgId(msgId);
        emailRecord.setFromEmail(MailConfig.EMAIL_USER);
        emailRecord.setToEmail(toEmail);
        emailRecord.setSubject(subject);
        emailRecord.setBody(body);
        emailRecord.setTimestamp(LocalDateTime.now());

        EmailCache.getInstance().cacheSentEmail(emailRecord);

        if (SupabaseConfig.isEnabled()) {
            vn.edu.hcmus.mail.supabase.SupabaseSyncService.getInstance().syncSentEmail(emailRecord);
        }
    }
}
