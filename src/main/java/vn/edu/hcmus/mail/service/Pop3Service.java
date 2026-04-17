package vn.edu.hcmus.mail.service;

import vn.edu.hcmus.mail.config.MailConfig;
import vn.edu.hcmus.mail.database.EmailCache;
import vn.edu.hcmus.mail.supabase.SupabaseSyncService;
import vn.edu.hcmus.mail.model.Email;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Properties;
import javax.mail.internet.*;

public class Pop3Service {
    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            BodyPart bodyPart = mimeMultipart.getBodyPart(0);
            return bodyPart.getContent().toString();
        }
        return "[Unsupported format]";
    }

    public String receive() {
        StringBuilder sb = new StringBuilder();
        Properties props = new Properties();
        props.put("mail.pop3.host", MailConfig.POP3_HOST);
        props.put("mail.pop3.port", MailConfig.POP3_PORT);
        props.put("mail.pop3.starttls.enable", "true");

        try {
            Session session = Session.getInstance(props);
            Store store = session.getStore(MailConfig.PROTOCOL);
            store.connect(MailConfig.POP3_HOST, MailConfig.EMAIL_USER, MailConfig.APP_PASSWORD);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            sb.append("--- Total emails: ").append(messages.length).append(" ---\n\n");

            int start = Math.max(0, messages.length - 5);
            for (int i = messages.length - 1; i >= start; i--) {
                Message msg = messages[i];
                sb.append("Email ").append(i + 1).append(":\n");
                sb.append("- From: ").append(msg.getFrom()[0]).append("\n");
                sb.append("- Subject: ").append(msg.getSubject()).append("\n");

                saveAttachments(msg);

                sb.append("- Content: \n");
                String bodyContent = "";
                try {
                    bodyContent = getTextFromMessage(msg);
                    sb.append(bodyContent);
                } catch (Exception e) {
                    sb.append("[Cannot display content]");
                }

                sb.append("\n---------------------------\n\n");

                try {
                    String fromEmail = msg.getFrom()[0].toString();
                    InternetAddress addr = new InternetAddress(fromEmail);
                    String msgId;
                    try {
                        msgId = ((MimeMessage) msg).getMessageID();
                    } catch (Exception e) {
                        msgId = String.valueOf(System.currentTimeMillis());
                    }
                    if (msgId == null) msgId = String.valueOf(System.currentTimeMillis());

                    Email emailRecord = new Email(Email.EmailType.RECEIVED);
                    emailRecord.setMsgId(msgId);
                    emailRecord.setFromEmail(addr.getAddress());
                    emailRecord.setToEmail(MailConfig.EMAIL_USER);
                    emailRecord.setSubject(msg.getSubject() != null ? msg.getSubject() : "(No subject)");
                    emailRecord.setBody(bodyContent);
                    emailRecord.setTimestamp(LocalDateTime.now());
                    emailRecord.setRead(false);

                    EmailCache.getInstance().cacheReceivedEmail(emailRecord);
                    System.out.println("[DATABASE] Saved received email to database: " + emailRecord.getSubject());

                    SupabaseSyncService.getInstance().syncReceivedEmail(emailRecord);
                } catch (Exception dbEx) {
                    System.err.println("[DATABASE] Error saving email: " + dbEx.getMessage());
                }
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            sb.append("POP3 Error: ").append(e.getMessage());
        }
        return sb.toString();
    }

    private void saveAttachments(Message message) {
        try {
            if (message.isMimeType("multipart/*")) {
                Multipart multipart = (Multipart) message.getContent();

                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);

                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                        String fileName = bodyPart.getFileName();
                        if (fileName != null) {
                            fileName = MimeUtility.decodeText(fileName);
                            System.out.println("  [Attachment found]: " + fileName);

                            File folder = new File("downloads");
                            if (!folder.exists()) folder.mkdirs();

                            File dest = new File(folder, fileName);

                            try (InputStream is = bodyPart.getInputStream();
                                 FileOutputStream fos = new FileOutputStream(dest)) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    fos.write(buffer, 0, bytesRead);
                                }
                            }
                            System.out.println("  => Saved to downloads folder!");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("  [File save error]: " + e.getMessage());
        }
    }
}
