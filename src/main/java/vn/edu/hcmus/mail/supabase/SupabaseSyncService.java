package vn.edu.hcmus.mail.supabase;

import vn.edu.hcmus.mail.config.SupabaseConfig;
import vn.edu.hcmus.mail.model.Email;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupabaseSyncService {
    private static SupabaseSyncService instance;
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private SupabaseSyncService() {
    }

    public static synchronized SupabaseSyncService getInstance() {
        if (instance == null) {
            instance = new SupabaseSyncService();
        }
        return instance;
    }

    public void syncSentEmail(Email email) {
        if (!SupabaseConfig.isEnabled()) {
            System.out.println("[SUPABASE] Not enabled. Skipping sync.");
            return;
        }

        new Thread(() -> {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("msg_id", email.getMsgId());
                data.put("from_email", email.getFromEmail());
                data.put("to_email", email.getToEmail());
                data.put("subject", email.getSubject());
                data.put("body", email.getBody());
                data.put("timestamp", email.getTimestamp().format(DATE_FORMAT));
                data.put("attachments", email.getAttachments());

                postToSupabase(data, "sent_emails");
                System.out.println("[SUPABASE] Synced sent email: " + email.getSubject());
            } catch (Exception e) {
                System.err.println("[SUPABASE] Error syncing sent email: " + e.getMessage());
            }
        }).start();
    }

    public void syncReceivedEmail(Email email) {
        if (!SupabaseConfig.isEnabled()) {
            System.out.println("[SUPABASE] Not enabled. Skipping sync.");
            return;
        }

        new Thread(() -> {
            try {
                System.out.println("[SUPABASE] Syncing received email: " + email.getSubject());
                
                Map<String, Object> data = new HashMap<>();
                data.put("msg_id", email.getMsgId());
                data.put("from_email", email.getFromEmail());
                data.put("to_email", email.getToEmail());
                data.put("subject", email.getSubject());
                data.put("body", email.getBody());
                data.put("timestamp", email.getTimestamp().format(DATE_FORMAT));
                data.put("is_read", email.isRead());

                postToSupabase(data, "received_emails");
                System.out.println("[SUPABASE] Synced received email: " + email.getSubject());
            } catch (Exception e) {
                System.err.println("[SUPABASE] Error syncing received email: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void postToSupabase(Map<String, Object> data, String table) throws Exception {
        System.out.println("[SUPABASE] Posting to: " + SupabaseConfig.getRestUrl() + "/" + table);
        URL url = new URL(SupabaseConfig.getRestUrl() + "/" + table + "?select=*");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Prefer", "return=representation");
        conn.setDoOutput(true);

        String jsonInput = mapToJson(data);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new Exception("HTTP " + responseCode);
        }
    }

    public String fetchFromSupabase(String table) {
        if (!SupabaseConfig.isEnabled()) {
            return "[]";
        }

        try {
            URL url = new URL(SupabaseConfig.getRestUrl() + "/" + table + "?select=*&order=timestamp.desc");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            }
        } catch (Exception e) {
            System.err.println("[SUPABASE] Fetch error: " + e.getMessage());
        }
        return "[]";
    }

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof String) {
                sb.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Boolean || value instanceof Number) {
                sb.append(value);
            } else if (value instanceof List) {
                sb.append(listToJson((List<?>) value));
            } else {
                sb.append("\"").append(escapeJson(value.toString())).append("\"");
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            Object item = list.get(i);
            if (item instanceof String) {
                sb.append("\"").append(escapeJson((String) item)).append("\"");
            } else {
                sb.append(item);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public boolean isConnected() {
        return SupabaseConfig.isEnabled();
    }
}
