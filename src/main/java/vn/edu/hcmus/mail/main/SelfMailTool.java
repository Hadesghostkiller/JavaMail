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

public class SelfMailTool {

    private static final String SELF_EMAIL = "doanltmang@gmail.com";

    private static final String[] SUBJECTS = {
        "Test Email #",
        "Email Client Test #",
        "POP3 Receive Test #",
        "Database Sync Test #",
        "Supabase Integration Test #",
        "JavaMail Project Test #",
        "LTMang Course Test #",
        "SMTP Service Test #",
        "Email History Test #",
        "Offline Mode Test #",
        "Cache Performance Test #",
        "Bulk Send Test #",
        "Attachment Test #",
        "Search Function Test #",
        "Delete Email Test #"
    };

    private static final String[] CONTENTS = {
        "This is a test email for our JavaMail client application.\n\nThe application supports:\n- Sending emails via SMTP\n- Receiving emails via POP3\n- Local SQLite database storage\n- Supabase cloud synchronization\n\nThis test helps verify the receive functionality.",
        
        "Automated test email for the JavaMail project.\n\nFeatures being tested:\n1. Email sending with attachments\n2. Email receiving from POP3 server\n3. Local database caching\n4. Cloud sync to Supabase\n\nPlease ignore this message.",
        
        "Database synchronization test email.\n\nWe are testing the following flow:\n1. Send email to self\n2. Receive via POP3\n3. Save to SQLite\n4. Sync to Supabase\n\nThis ensures all components work together correctly.",
        
        "OpenCode integration test message.\n\nOpenCode is an AI coding assistant that helped us build:\n- Database layer (SQLite)\n- Sync layer (Supabase)\n- Email services (SMTP/POP3)\n\nThis tests the complete workflow.",
        
        "Supabase synchronization test.\n\nOur Supabase setup includes:\n- REST API endpoint\n- PostgreSQL database\n- Row Level Security policies\n- Automatic sync on send/receive\n\nThis email helps verify sync functionality."
    };

    private static final Random random = new Random();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("=== JavaMail Self-Send Test Tool ===");
        System.out.println("This tool will send 100 emails to: " + SELF_EMAIL);
        System.out.println("Then you can test receiving via POP3!");
        System.out.println();

        int totalEmails = 100;
        int batchSize = 10;

        ExecutorService executor = Executors.newFixedThreadPool(3);
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= totalEmails; i++) {
            final int emailNumber = i;
            executor.submit(() -> {
                try {
                    sendSelfEmail(emailNumber);
                    if (emailNumber % batchSize == 0) {
                        System.out.println("[" + emailNumber + "/" + totalEmails + "] Batch completed");
                    }
                } catch (Exception e) {
                    System.err.println("[" + emailNumber + "] Failed: " + e.getMessage());
                }
            });

            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(15, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;

        System.out.println();
        System.out.println("=== SELF-SEND COMPLETED ===");
        System.out.println("Total emails sent: " + totalEmails);
        System.out.println("Duration: " + duration + " seconds");
        System.out.println();
        System.out.println("Next steps:");
        System.out.println("1. Run the main application");
        System.out.println("2. Go to 'Hộp Thư Đến' tab");
        System.out.println("3. Click 'LÀM MỚI' to receive all " + totalEmails + " emails");
        System.out.println("4. Check SQLite and Supabase for synced data");
    }

    private static void sendSelfEmail(int number) throws Exception {
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

        String subjectBase = SUBJECTS[random.nextInt(SUBJECTS.length)];
        String subject = subjectBase + number;

        String contentBase = CONTENTS[random.nextInt(CONTENTS.length)];
        StringBuilder body = new StringBuilder();
        body.append(contentBase);
        body.append("\n\n");
        body.append("--- TEST EMAIL #").append(number).append(" ---\n");
        body.append("Sent at: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n");
        body.append("From: ").append(MailConfig.EMAIL_USER).append("\n");
        body.append("To: ").append(SELF_EMAIL).append("\n");
        body.append("Database Ref: ").append(UUID.randomUUID().toString().substring(0, 8).toUpperCase()).append("\n");
        body.append("Purpose: Testing POP3 receive functionality\n");
        
        if (number % 10 == 0) {
            body.append("\n--- LONG CONTENT TEST ---\n");
            body.append("This email contains extra content to test database storage capacity.\n");
            body.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n");
            body.append("Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n");
            body.append("Ut enim ad minim veniam, quis nostrud exercitation.\n");
            body.append("Email ID: ").append(UUID.randomUUID().toString()).append("\n");
            body.append("Timestamp: ").append(LocalDateTime.now().toString()).append("\n");
        }

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(MailConfig.EMAIL_USER));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(SELF_EMAIL));
        message.setSubject(subject, "UTF-8");
        message.setText(body.toString(), "UTF-8");

        String msgId = UUID.randomUUID().toString();
        message.setHeader("Message-ID", "<" + msgId + ">");

        Transport.send(message);

        Email emailRecord = new Email(Email.EmailType.SENT);
        emailRecord.setMsgId(msgId);
        emailRecord.setFromEmail(MailConfig.EMAIL_USER);
        emailRecord.setToEmail(SELF_EMAIL);
        emailRecord.setSubject(subject);
        emailRecord.setBody(body.toString());
        emailRecord.setTimestamp(LocalDateTime.now());

        EmailCache.getInstance().cacheSentEmail(emailRecord);

        if (SupabaseConfig.isEnabled()) {
            vn.edu.hcmus.mail.supabase.SupabaseSyncService.getInstance().syncSentEmail(emailRecord);
        }
    }
}
